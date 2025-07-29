package com.example.myot.profile

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.myot.R
import com.example.myot.databinding.FragmentProfileBinding
import com.example.myot.feed.model.FeedItem
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!


    private val allFeeds = listOf(
        FeedItem(
            username = "뮤덕이",
            content = "오늘 <드라큘라> 공연 다녀왔어요! 카리스마 넘치는 연기와 음향, 진짜 압도적이었어요.".repeat(2),
            imageUrls = listOf("https://picsum.photos/300/200?random=101"),
            date = "2025/07/28 18:40",
            community = "공연 후기",
            isReposted = true,
            commentCount = 3,
            likeCount = 12,
            repostCount = 2,
            quoteCount = 4,
            quotedFeed = FeedItem(
                username = "뮤직러버",
                content = "<드라큘라>는 항상 새로운 감동을 주는 것 같아요. 연출이 너무 웅장해요.",
                date = "2025/06/25 10:20",
                community = "관람 팁",
                commentCount = 0,
                likeCount = 5,
                repostCount = 1,
                quoteCount = 1
            )
        ),
        FeedItem(
            username = "연극소년",
            content = "이번 <프랑켄슈타인> 넘버는 정말 미쳤어요. 특히 ‘왜 나를 사랑하지 않죠’는 압권이에요!",
            imageUrls = listOf("https://picsum.photos/300/200?random=102", "https://picsum.photos/300/200?random=103"),
            date = "2025/07/27 20:15",
            community = "명장면 공유",
            isQuoted = true,
            commentCount = 5,
            likeCount = 30,
            repostCount = 3,
            quoteCount = 7,
            quotedFeed = FeedItem(
                username = "뮤덕후",
                content = "<프랑켄슈타인>은 넘버 하나하나가 너무 좋죠. 감정선이 폭발적이에요.",
                date = "2025/06/18 19:30",
                community = "레전드 넘버",
                commentCount = 0,
                likeCount = 8,
                repostCount = 0,
                quoteCount = 2
            )
        ),
        FeedItem(
            username = "토요일은뮤지컬",
            content = "무대 세트가 정말 어마어마했어요. 특히 조명이 감정을 너무 잘 살려줌!",
            imageUrls = emptyList(),
            date = "2025/07/26 14:00",
            community = "무대 연출 분석",
            isReposted = true,
            commentCount = 2,
            likeCount = 18,
            repostCount = 1,
            quoteCount = 3
        ),
        FeedItem(
            username = "공연왕",
            content = "대학로에서 <베르테르> 관람. ‘그녀에게’ 부르는데 눈물 났어요...",
            imageUrls = listOf("https://picsum.photos/300/200?random=104"),
            date = "2025/07/25 19:00",
            community = "감성 후기",
            isReposted = true,
            commentCount = 6,
            likeCount = 27,
            repostCount = 2,
            quoteCount = 5
        ),
        FeedItem(
            username = "무대뒤이야기",
            content = "오늘은 <지킬 앤 하이드> 연습 영상을 다시 보며 감탄. 이건 정말 무대 전설입니다.".repeat(2),
            imageUrls = listOf("https://picsum.photos/300/200?random=105"),
            date = "2025/07/24 22:00",
            community = "연습실 스토리",
            isReposted = true,
            isQuoted = true,
            commentCount = 8,
            likeCount = 50,
            repostCount = 4,
            quoteCount = 12,
            quotedFeed = FeedItem(
                username = "하이드짱",
                content = "<지금 이 순간>은 무조건 전율! 무대가 너무 압도적이라 숨 막힐 정도예요.",
                date = "2025/06/10 13:40",
                community = "넘버 분석",
                commentCount = 1,
                likeCount = 15,
                repostCount = 0,
                quoteCount = 0
            )
        )
    )
    private val likedFeeds = allFeeds.filter { it.isReposted }
    private val quoteFeeds = allFeeds.filter { it.isQuoted }
    private val mediaFeeds = allFeeds.filter { it.imageUrls.isNotEmpty() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout

        val fragments = listOf(
            ProfileTabFragment(allFeeds),
            ProfileTabFragment(likedFeeds),
            ProfileTabFragment(quoteFeeds),
            ProfileTabFragment(mediaFeeds)
        )
        val titles = listOf("전체", "재게시", "인용", "미디어")

        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = fragments.size
            override fun createFragment(position: Int) = fragments[position]
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, pos ->
            tab.text = titles[pos]
        }.attach()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabStrip = binding.tabLayout.getChildAt(0) as ViewGroup
        tabStrip.setPadding(10.dpToPx(), 0, 10.dpToPx(), 0)
        tabStrip.clipToPadding = false

        requireActivity().findViewById<View>(R.id.top_bar).visibility = View.GONE

        binding.tabLayout.post {
            setTabTextStyle(binding.tabLayout, binding.tabLayout.selectedTabPosition, true)
        }

        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab) {
                setTabTextStyle(binding.tabLayout, tab.position, true)
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab) {
                setTabTextStyle(binding.tabLayout, tab.position, false)
            }
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<View>(R.id.top_bar).visibility = View.VISIBLE
        _binding = null
    }

    private fun Int.dpToPx(): Int {
        return (this * Resources.getSystem().displayMetrics.density).toInt()
    }

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