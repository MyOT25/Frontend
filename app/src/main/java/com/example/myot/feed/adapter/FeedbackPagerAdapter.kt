package com.example.myot.feed.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myot.feed.model.FeedItem
import com.example.myot.feed.ui.FeedbackListFragment
import com.example.myot.feed.ui.FeedbackQuoteFragment
import com.google.android.material.bottomsheet.BottomSheetDialog

class FeedbackPagerAdapter(
    fa: FragmentActivity,
    private val likeUsers: List<String>,
    private val repostUsers: List<String>,
    private val quoteFeeds: List<FeedItem>,
    private val dialog: BottomSheetDialog,
    private val onFeedClick: () -> Unit
) : FragmentStateAdapter(fa) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FeedbackListFragment.newInstance(likeUsers)
            1 -> FeedbackListFragment.newInstance(repostUsers)
            2 -> FeedbackQuoteFragment.newInstance(quoteFeeds, dialog, onFeedClick)
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}