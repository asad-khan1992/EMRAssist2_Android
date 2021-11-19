package com.emrassist.audio.ui.activity.dictation_list

import android.os.Bundle
import com.emrassist.audio.R
import com.emrassist.audio.ui.activity.dictation_list.adapter.DictationPagerAdapter
import com.emrassist.audio.ui.activity.dictation_list.fragment.pending.PendingListFragment
import com.emrassist.audio.ui.activity.dictation_list.fragment.processing.DictationListOnlineFragment
import com.emrassist.audio.ui.activity.dictation_list.model.DictationListPagerItem
import com.emrassist.audio.ui.base.BaseActivity
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_dictation_list.*
import java.util.*

@AndroidEntryPoint
class DictationListActivity : BaseActivity() {
    private var pagerAdapter: DictationPagerAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dictation_list)
        setUpAdapter()
    }

    private fun setUpAdapter() {
        pagerAdapter =
            DictationPagerAdapter(this, getListOfFragments(), supportFragmentManager, lifecycle);
        view_pager_2.adapter = pagerAdapter


        TabLayoutMediator(tab_layout, view_pager_2) { tab, position ->
            tab.text = pagerAdapter?.getTitle(position)
        }.attach()

    }

    private fun getListOfFragments(): ArrayList<DictationListPagerItem> {
        val tempList: ArrayList<DictationListPagerItem> = ArrayList()
//        tempList.add(DictationListPagerItem("Library", AllListFragment()))
        tempList.add(DictationListPagerItem("Uploading", PendingListFragment()))
//        tempList.add(DictationListPagerItem("Pending", PendingListFragment()))
        tempList.add(
            DictationListPagerItem(
                "Pending",
                DictationListOnlineFragment.newInstance(DictationListOnlineFragment.LIST_TYPE_PENDING)
            )
        )
        tempList.add(
            DictationListPagerItem(
                "Processing",
                DictationListOnlineFragment.newInstance(DictationListOnlineFragment.LIST_TYPE_PROCESSING)
            )
        )
        tempList.add(
            DictationListPagerItem(
                "Done",
                DictationListOnlineFragment.newInstance(DictationListOnlineFragment.LIST_TYPE_DONE)
            )
        )
        return tempList
    }
}