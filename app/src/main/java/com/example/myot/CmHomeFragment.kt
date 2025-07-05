package com.example.myot

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myot.databinding.FragmentCmHomeBinding
import com.example.myot.databinding.FragmentCmMediaBinding

class CmHomeFragment : Fragment() {

    private var _binding: FragmentCmHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CommunityViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCmHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.communityMode.observe(viewLifecycleOwner) { mode ->
            binding.btnCmHomeEdit.visibility = if (mode == CommunityMode.MEMBER) View.VISIBLE else View.GONE
        }

        // 피드 더미 데이터 생성
        val dummyFeeds = listOf(
            FeedItem(
                username = "유저1", content = "뮤지컬 <레미제라블> 보고 왔어요. '민중의 노래' 장면은 언제 봐도 소름... 같이 본 친구도 감동받았대요.".repeat(3),
                imageUrls = listOf(), date = "2025/06/24 01:10", community = "뮤지컬 후기",
                commentCount = 0, likeCount = 1, repostCount = 0, bookmarkCount = 0
            ),
            FeedItem(
                username = "유저2", content = "뮤지컬 <헤드윅>은 진짜 미쳤다... 배우의 에너지가 넘침. 특히 'Midnight Radio' 장면 눈물ㅠㅠ",
                imageUrls = listOf("https://picsum.photos/300/200?random=1"), date = "2025/06/24 02:09", community = "창작 뮤지컬",
                commentCount = 7, likeCount = 55, repostCount = 4, bookmarkCount = 20
            ),
            FeedItem(
                username = "유저3", content = "오늘은 대학로에서 <빨래> 관람했어요. 소극장이라 배우들과 가까워서 몰입감 장난 아님!",
                imageUrls = listOf("https://picsum.photos/300/200?random=2", "https://picsum.photos/300/200?random=3"), date = "2025/06/20 20:30", community = "소극장 뮤지컬",
                commentCount = 2, likeCount = 33, repostCount = 1, bookmarkCount = 7
            ),
            FeedItem(
                username = "유저4", content = "뮤지컬 <위키드> 내한공연 보신 분? 글린다랑 엘파바 완전 찰떡이었음... 무대 세트도 대박이에요.".repeat(10),
                imageUrls = listOf("https://picsum.photos/300/200?random=4", "https://picsum.photos/300/200?random=5", "https://picsum.photos/300/200?random=6"), date = "2025/02/19 16:20", community = "해외 뮤지컬",
                commentCount = 9, likeCount = 61, repostCount = 5, bookmarkCount = 18
            ),
            FeedItem(
                username = "유저5", content = "레베카의 '나는 나만의 것' 듣고 완전 입덕... 이건 진짜 한번쯤 꼭 봐야 해요. 내 최애 넘버 1위.",
                imageUrls = listOf("https://picsum.photos/300/200?random=7", "https://picsum.photos/300/200?random=8", "https://picsum.photos/300/200?random=9", "https://picsum.photos/300/200?random=10"), date = "2024/06/15 10:00", community = "명작 뮤지컬",
                commentCount = 10, likeCount = 78, repostCount = 6, bookmarkCount = 25
            )
        )

        // 피드 리사이클러뷰 초기화
        binding.rvHomes.apply {
            adapter = FeedAdapter(dummyFeeds)
            layoutManager = LinearLayoutManager(requireContext())
        }
    }
}