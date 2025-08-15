package com.example.myot.profile

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myot.R
import com.example.myot.databinding.FragmentProfileBinding
import com.google.android.material.tabs.TabLayoutMediator

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_USER_ID = "arg_user_id"
        fun newInstance(userId: Long) = ProfileFragment().apply {
            arguments = Bundle().apply { putLong(ARG_USER_ID, userId) }
        }
    }

    private val userId: Long by lazy {
        arguments?.getLong("arg_user_id")
            ?: error("ProfileFragment requires userId. Use ProfileFragment.newInstance(userId)")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        val fragments = listOf(
            ProfileTabFragment.newInstance(ProfileFeedTab.ALL, userId),
            ProfileTabFragment.newInstance(ProfileFeedTab.REPOST, userId),
            ProfileTabFragment.newInstance(ProfileFeedTab.QUOTE, userId),
            ProfileTabFragment.newInstance(ProfileFeedTab.MEDIA, userId)
        )
        val titles = listOf("전체", "재게시", "인용", "미디어")

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = fragments.size
            override fun createFragment(position: Int) = fragments[position]
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, pos ->
            tab.text = titles[pos]
        }.attach()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().findViewById<View>(R.id.top_bar).visibility = View.GONE

        binding.tabLayout.post {
            setTabTextStyle(binding.tabLayout, binding.tabLayout.selectedTabPosition, true)
        }
        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab) { setTabTextStyle(binding.tabLayout, tab.position, true) }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab) { setTabTextStyle(binding.tabLayout, tab.position, false) }
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<View>(R.id.top_bar).visibility = View.VISIBLE
        _binding = null
    }

    private fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun setTabTextStyle(tabLayout: com.google.android.material.tabs.TabLayout, position: Int, isSelected: Boolean) {
        val tabStrip = tabLayout.getChildAt(0) as? ViewGroup ?: return
        val tabView = tabStrip.getChildAt(position) as? ViewGroup ?: return
        for (i in 0 until tabView.childCount) {
            val child = tabView.getChildAt(i)
            if (child is android.widget.TextView) {
                child.setTypeface(null, if (isSelected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
            }
        }
    }
}