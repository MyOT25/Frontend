package com.example.myot

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class FeedbackPagerAdapter(
    fa: FragmentActivity,
    private val likeUsers: List<String>,
    private val repostUsers: List<String>,
    private val quoteFeeds: List<FeedItem>
) : FragmentStateAdapter(fa) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FeedbackListFragment.newInstance(likeUsers)
            1 -> FeedbackListFragment.newInstance(repostUsers)
            2 -> FeedbackQuoteFragment.newInstance(quoteFeeds)
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
