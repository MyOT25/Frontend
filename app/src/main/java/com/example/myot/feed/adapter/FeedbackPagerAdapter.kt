package com.example.myot.feed.adapter

import android.util.SparseArray
import androidx.core.util.set
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myot.feed.model.FeedItem
import com.example.myot.feed.model.FeedbackUserUi
import com.example.myot.feed.ui.FeedbackListFragment
import com.example.myot.feed.ui.FeedbackQuoteFragment
import com.google.android.material.bottomsheet.BottomSheetDialog

class FeedbackPagerAdapter(
    fa: FragmentActivity,
    private val dialog: BottomSheetDialog,
    private val onFeedClick: () -> Unit
) : FragmentStateAdapter(fa) {

    private val fragmentMap = SparseArray<Fragment>()

    private var pendingLikeUsers: List<FeedbackUserUi> = emptyList()
    private var pendingRepostUsers: List<FeedbackUserUi> = emptyList()

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        val f: Fragment = when (position) {
            0 -> FeedbackListFragment.newInstance()
            1 -> FeedbackListFragment.newInstance()
            2 -> FeedbackQuoteFragment.newInstance(emptyList(), dialog, onFeedClick)
            else -> throw IllegalArgumentException("Invalid position")
        }
        fragmentMap[position] = f

        (f as? FeedbackListFragment)?.let { listFrag ->
            when (position) {
                0 -> if (pendingLikeUsers.isNotEmpty()) listFrag.submitUsers(pendingLikeUsers)
                1 -> if (pendingRepostUsers.isNotEmpty()) listFrag.submitUsers(pendingRepostUsers)
            }
        }
        return f
    }

    fun submitLikeUsers(list: List<FeedbackUserUi>) {
        pendingLikeUsers = list
        (fragmentMap[0] as? FeedbackListFragment)?.submitUsers(list)
    }

    fun submitRepostUsers(list: List<FeedbackUserUi>) {
        pendingRepostUsers = list
        (fragmentMap[1] as? FeedbackListFragment)?.submitUsers(list)
    }

    fun submitQuotes(feeds: List<FeedItem>) {
        // 필요 시 구현(현재는 사용 안 함)
    }
}