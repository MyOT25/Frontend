package com.example.myot.community.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myot.community.ui.CmHighlightFragment
import com.example.myot.community.ui.CmHomeFragment
import com.example.myot.community.ui.CmMediaFragment
import com.example.myot.community.ui.CmReviewFragment
import com.example.myot.memorybook.CmMemoryFragment

class CommunityTabAdapter(
    fragment: Fragment,
    private val tabTitles: List<String>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = tabTitles.size

    override fun createFragment(position: Int): Fragment {
        // 각 탭에 해당하는 Fragment 반환
        return when (tabTitles[position]) {
            "하이라이트" -> CmHighlightFragment()
            "전체" -> CmHomeFragment()
            "후기" -> CmReviewFragment()
            "미디어" -> CmMediaFragment()
            "메모리북" -> CmMemoryFragment()
            else -> CmHomeFragment()
        }
    }
}
