package com.emrassist.audio.ui.activity.dictation_list.fragment.library

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.emrassist.audio.R
import com.emrassist.audio.model.RecordedItem
import com.emrassist.audio.ui.activity.dictation_list.adapter.DictationListDatabaseAdapter
import com.emrassist.audio.ui.activity.dictation_list.view_model.DictationListViewModel
import com.emrassist.audio.utils.AlertOP
import com.emrassist.audio.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_dictation_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@AndroidEntryPoint
class AllListFragment : Fragment(R.layout.fragment_dictation_list) {
    var adapter: DictationListDatabaseAdapter? = null
    val viewModel: DictationListViewModel by viewModels()

    fun newInstance(): AllListFragment {
        return AllListFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpObservers()
        setListeners()

    }

    private fun setUpObservers() {
        viewModel.dataStateRecording.observe(viewLifecycleOwner, Observer {
            swipe_refresh_layout?.isRefreshing = false
            if (it != null && it.isNotEmpty()) {
                text_no_records.visibility = View.GONE
                recycler_view.visibility = View.VISIBLE
                setupAdapter(it)
            } else {
                recycler_view.adapter = null
                recycler_view.visibility = View.GONE
                text_no_records.visibility = View.VISIBLE
            }
        })
    }

    private fun setupAdapter(list: ArrayList<RecordedItem>) {
        if (adapter == null || recycler_view.adapter == null) {
            adapter = DictationListDatabaseAdapter(requireContext(), list, false, recycler_view)
            recycler_view.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            recycler_view.adapter = adapter;
            adapter?.setOnItemClickListener(object :
                DictationListDatabaseAdapter.OnItemClickListener {
                override fun onClick(position: Int, item: RecordedItem) {
                    Utils.openPlayerActivity(item, requireContext())
                }
            })
            adapter?.setOnItemDeleteClickListener(object :
                DictationListDatabaseAdapter.OnItemDeleteClickListener {
                override fun onClick(position: Int, item: RecordedItem) {
                    showDeleteItemDialog(position, item)
                }
            })
        }

    }

    private fun showDeleteItemDialog(position: Int, item: RecordedItem) {
        AlertOP.showAlert(
            requireContext(),
            message = "Are you sure you want to delete this recording",
            pBtnText = "Yes",
            nBtnText = "Cancel",
            cancelable = false,
            onPositiveClick = {
                viewModel.deleteRecoding(item)
                if ((adapter?.filteredList?.size == 0)) {
                    recycler_view.visibility = View.GONE
                    text_no_records.visibility = View.VISIBLE
                }
            },
            onNegativeClick = {
                adapter?.restoreItem(position, item)
            })
    }

    private fun setListeners() {
        swipe_refresh_layout?.setOnRefreshListener {
            getListOfPendingAudios()
        }
    }

    override fun onResume() {
        super.onResume()

        swipe_refresh_layout?.post {
            getListOfPendingAudios()
        }
    }

    private fun getListOfPendingAudios() {
        swipe_refresh_layout?.isRefreshing = true
        viewModel.getListOfRecordingsFromDB()

    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onItemUploadedSuccessfully(item: RecordedItem) {
        Log.d(this.javaClass.simpleName, "onItemUploadedSuccessfully: ${item.fileName}")
        swipe_refresh_layout?.postDelayed({
            getListOfPendingAudios()
        }, 500)
    }
}