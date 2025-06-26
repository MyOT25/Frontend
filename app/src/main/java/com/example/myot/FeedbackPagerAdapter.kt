package com.example.myot

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class FeedbackPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val feedbackMap: Map<String, List<String>>
) : FragmentStateAdapter(fragmentActivity) {

    private val keys = listOf("like", "repost", "quote")

    override fun getItemCount(): Int = keys.size

    override fun createFragment(position: Int): Fragment {
        val key = keys[position]
        return FeedbackListFragment.newInstance(feedbackMap[key] ?: emptyList())
    }
}
