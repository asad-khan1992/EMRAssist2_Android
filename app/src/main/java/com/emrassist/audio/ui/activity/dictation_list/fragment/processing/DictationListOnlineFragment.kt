package com.emrassist.audio.ui.activity.dictation_list.fragment.processing

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.emrassist.audio.R
import com.emrassist.audio.interfaces.OnLoadMoreListener
import com.emrassist.audio.model.RecordedItem
import com.emrassist.audio.retrofit.DataState
import com.emrassist.audio.ui.activity.dictation_list.adapter.DictationListAdapter
import com.emrassist.audio.ui.activity.dictation_list.view_model.DictationListViewModel
import com.emrassist.audio.ui.base.item.SessionExpired
import com.emrassist.audio.utils.AlertOP
import com.emrassist.audio.utils.RecyclerViewLoadMoreScroll
import com.emrassist.audio.utils.Utils
import com.google.gson.JsonParseException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_dictation_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

@AndroidEntryPoint
class DictationListOnlineFragment : Fragment(R.layout.fragment_dictation_list) {
    private var currentPage: Int = 1
    private var lastPage: Int = 1
    private var loadMore: Boolean = false
    private var listType: String = ""
    var adapter: DictationListAdapter? = null
    val viewModel: DictationListViewModel by viewModels()
    var audioList: ArrayList<RecordedItem> = ArrayList()
    companion object {
        private var params: String = "param"
        val LIST_TYPE_PENDING: String = "pending"
        val LIST_TYPE_PROCESSING: String = "processing"
        val LIST_TYPE_DONE: String = "done"
        fun newInstance(_listType: String): DictationListOnlineFragment {
            val fragment = DictationListOnlineFragment()
            val args = Bundle()
            args.putString(params, _listType)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listType = arguments?.getString(params, LIST_TYPE_PENDING) ?: LIST_TYPE_PENDING
        EventBus.getDefault().register(this)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpObservers()
        setListeners()

    }

    private fun setUpObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer {
            when (it) {
                is DataState.Success -> {
                    showLoader(false)
                    if (it.data.success != 0) {
                        if (it.data.responseObject != null && it.data.responseObject?.data != null && !it.data.responseObject?.data?.listOfAudios.isNullOrEmpty()) {
                            val tempListData =
                                it.data.responseObject?.data?.listOfAudios?.filter { item ->
                                    if (listType.equals(LIST_TYPE_PENDING, true))
                                        item.status.equals("0", true)
                                    else if (listType.equals(LIST_TYPE_PROCESSING, true))
                                        item.status.equals("1", true)
                                    else
                                        item.status.equals("2", true)
                                } as ArrayList<RecordedItem>
                            currentPage = it.data.responseObject?.data?.currentPage!!
                            lastPage = it.data.responseObject?.data?.lastPage!!

                            if (tempListData.size > 0) {
                                text_no_records.visibility = View.GONE
                                recycler_view.visibility = View.VISIBLE
                                audioList.clear()
                                audioList.addAll(tempListData)
                                setupAdapter(tempListData)
                                return@Observer
                            }
                        }
                    } else {
                        EventBus.getDefault().post(SessionExpired(true))
                    }
                    recycler_view.visibility = View.GONE
                    text_no_records.visibility = View.VISIBLE

                }
                is DataState.Loading -> {
                    showLoader(true)
                }
                is DataState.ErrorException -> {
                    showLoader(false)
                    recycler_view.visibility = View.GONE
                    text_no_records.visibility = View.VISIBLE
                    if (it is JsonParseException) {

                    } else {
                        AlertOP.showAlert(
                            requireContext(),
                            message = it.exception.localizedMessage
                                ?: "Some thing went wrong. Please try again later.",
                            pBtnText = "Ok"

                        )
                    }
                }
            }
        })
    }

    private fun showLoader(b: Boolean) {
        swipe_refresh_layout?.isRefreshing = b
    }

    private fun setupAdapter(list: ArrayList<RecordedItem>) {
        if (adapter == null || recycler_view.adapter == null) {
            adapter = DictationListAdapter(requireContext(), list)
            recycler_view.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            recycler_view.adapter = adapter;
            adapter?.setOnItemClickListener(object :
                DictationListAdapter.OnItemClickListener {
                override fun onClick(position: Int, item: RecordedItem) {
                    Utils.openPlayerActivity(item, requireContext())
                }
            })
            var scrollListener =
                RecyclerViewLoadMoreScroll(recycler_view.layoutManager as LinearLayoutManager)
            scrollListener.setOnLoadMoreListener(object :
                OnLoadMoreListener {
                override fun onLoadMore() {
                    loadMoreData()
                }
            })
            recycler_view.addOnScrollListener(scrollListener)
        } else {
            if (!loadMore)
                adapter?.setList(list)
            else {
                adapter?.removeLoadingView()
                adapter?.addAll(list)
                loadMore = false
            }
        }
    }

    fun loadMoreData() {
        if (currentPage < lastPage) {
            loadMore = true
            adapter?.addLoadingView()
            currentPage++
            getListOfAudios()
        }
    }


    private fun setListeners() {
        swipe_refresh_layout?.setOnRefreshListener {
            getListOfAudios()
        }
        swipe_refresh_layout?.post {
            swipe_refresh_layout?.isRefreshing = true
            getListOfAudios()
        }

        setSearch()
    }

    private fun setSearch() {
      //  val etSearch: EditText = mView!!.findViewById(R.id.et_search)


        et_search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length > 0)
                    adapter!!.filter.filter(s.toString())
                else
                    setupAdapter(audioList)
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun getListOfAudios() {
        if (isVisible)
            viewModel.getListOfRecordings(currentPage)

    }

    override fun onResume() {
        super.onResume()
        getListOfAudios()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onItemUploadedSuccessfully(item: RecordedItem) {
        Log.d(this.javaClass.simpleName, "onItemUploadedSuccessfully: ${item.fileName}")
        swipe_refresh_layout?.postDelayed({
            getListOfAudios()
        }, 500)
    }
}