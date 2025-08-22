package com.example.myot.write

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myot.R
import com.example.myot.home.MyCommunityItem
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayoutMediator

class PublishBottomSheet(
    private val initialTab: Tab,
    private val isPublicNow: Boolean,
    private val communities: List<CommunityOption>,
    private val selectedCommunityId: Long?,
    private val listener: Listener
) : BottomSheetDialogFragment() {

    interface Listener {
        fun onSelectVisibility(isPublic: Boolean)
        fun onSelectCommunity(option: CommunityOption)
    }

    enum class Tab { COMMUNITY, VISIBILITY }

    override fun onStart() {
        super.onStart()
        dialog?.let { dlg ->
            val bottomSheet = dlg.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)
                val peek = (300 * resources.displayMetrics.density).toInt()
                behavior.peekHeight = peek
                behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
                it.layoutParams.height = peek
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottomsheet_write, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tabLayout = view.findViewById<com.google.android.material.tabs.TabLayout>(R.id.tab_feedback)
        val vp = view.findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.vp_feedback)

        vp.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 2
            override fun createFragment(position: Int): Fragment =
                if (position == 0)
                    CommunityTabFragment.new(communities, selectedCommunityId) {
                        listener.onSelectCommunity(it); dismiss()
                    }
                else
                    VisibilityTabFragment.new(isPublicNow) {
                        listener.onSelectVisibility(it); dismiss()
                    }
        }

        TabLayoutMediator(tabLayout, vp) { tab, pos ->
            tab.text = if (pos == 0) "커뮤니티" else "공개 범위"
        }.attach()

        val boldSpan = android.text.style.StyleSpan(android.graphics.Typeface.BOLD)
        val normalSpan = android.text.style.StyleSpan(android.graphics.Typeface.NORMAL)

        fun updateTabStyle(selectedTab: com.google.android.material.tabs.TabLayout.Tab?) {
            for (i in 0 until tabLayout.tabCount) {
                val t = tabLayout.getTabAt(i)
                val text = t?.text?.toString() ?: continue
                val spannable = android.text.SpannableString(text)
                if (t == selectedTab) {
                    spannable.setSpan(boldSpan, 0, text.length, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                } else {
                    spannable.setSpan(normalSpan, 0, text.length, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                t.text = spannable
            }
        }

        tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab) {
                updateTabStyle(tab)
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
        })

        updateTabStyle(tabLayout.getTabAt(if (initialTab == Tab.COMMUNITY) 0 else 1))
        vp.setCurrentItem(if (initialTab == Tab.COMMUNITY) 0 else 1, false)
    }
}
