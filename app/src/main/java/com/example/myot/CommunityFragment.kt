package com.example.myot

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myot.databinding.FragmentCommunityBinding
import com.google.android.material.tabs.TabLayoutMediator

class CommunityFragment : Fragment() {

    private lateinit var itemType: String   // "ACTOR" or "MUSICAL"

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemType = arguments?.getString(ARG_ITEM_TYPE) ?: "MUSICAL"   // 기본값 "작품"으로 설정
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val viewPager = binding.vpTab
        val tabLayout = binding.tlMenu
        val tabTitles = when (itemType) {
            "ACTOR" -> listOf("하이라이트", "전체", "미디어", "메모리북")
            "MUSICAL" -> listOf("하이라이트", "전체", "후기", "미디어", "메모리북")
            else -> listOf("하이라이트", "전체", "미디어", "메모리북")
        }

        val adapter = CommunityTabAdapter(this, tabTitles)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    companion object {
        private const val ARG_ITEM_TYPE = "item_type"
        fun newInstance(type: String): CommunityFragment {
            val fragment = CommunityFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_ITEM_TYPE, type)
            }
            return fragment
        }
    }
}