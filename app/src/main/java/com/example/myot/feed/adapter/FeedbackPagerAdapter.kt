package com.example.myot.feed.adapter

import android.util.SparseArray
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
    private val onFeedClick: () -> Unit = {}
) : FragmentStateAdapter(fa) {

    private val fragmentMap = SparseArray<Fragment>()

    private var pendingLikeUsers: List<FeedbackUserUi> = emptyList()
    private var pendingRepostUsers: List<FeedbackUserUi> = emptyList()
    private var pendingQuoteFeeds: List<FeedItem> = emptyList() // ★ 인용 버퍼

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        val f: Fragment = when (position) {
            0 -> FeedbackListFragment.newInstance()
            1 -> FeedbackListFragment.newInstance()
            2 -> FeedbackQuoteFragment.newInstance(emptyList(), dialog, onFeedClick) // 초기 빈상태
            else -> throw IllegalArgumentException("Invalid position")
        }
        fragmentMap.put(position, f)

        // 생성 직후 버퍼 주입
        (f as? FeedbackListFragment)?.let { listFrag ->
            when (position) {
                0 -> if (pendingLikeUsers.isNotEmpty()) listFrag.submitUsers(pendingLikeUsers)
                1 -> if (pendingRepostUsers.isNotEmpty()) listFrag.submitUsers(pendingRepostUsers)
            }
        }
        (f as? FeedbackQuoteFragment)?.let { quoteFrag ->
            if (pendingQuoteFeeds.isNotEmpty()) quoteFrag.submit(pendingQuoteFeeds)
        }
        return f
    }

    fun submitLikeUsers(list: List<FeedbackUserUi>) {
        pendingLikeUsers = list
        (fragmentMap.get(0) as? FeedbackListFragment)?.submitUsers(list)
    }

    fun submitRepostUsers(list: List<FeedbackUserUi>) {
        pendingRepostUsers = list
        (fragmentMap.get(1) as? FeedbackListFragment)?.submitUsers(list)
    }

    fun submitQuoteFeeds(items: List<FeedItem>) {
        pendingQuoteFeeds = items
        (fragmentMap.get(2) as? FeedbackQuoteFragment)?.submit(items)
    }
}