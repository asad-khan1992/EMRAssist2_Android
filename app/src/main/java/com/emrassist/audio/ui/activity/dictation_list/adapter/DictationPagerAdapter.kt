package com.emrassist.audio.ui.activity.dictation_list.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.emrassist.audio.ui.activity.dictation_list.model.DictationListPagerItem

class DictationPagerAdapter(
    private val context: Context,
    private val listOfFragments: ArrayList<DictationListPagerItem>,
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return listOfFragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return listOfFragments[position].fragment
    }

    fun getTitle(position: Int): String {
        return listOfFragments[position].title
    }
}