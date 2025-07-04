package com.example.myot

import android.graphics.Rect
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup.LayoutParams
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var isExpanded = false

    override fun onCreateView(inflater: LayoutInflater,
          container: ViewGroup?,
          savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 커뮤니티 추가/확장/버튼 기능
        // 테스트 데이터
        val rawItems = listOf(
            CommunityItem(R.drawable.ic_home_add_community, true),
            CommunityItem(R.drawable.ic_home_no_community, false),
            CommunityItem(R.drawable.ic_home_no_community, false),
            CommunityItem(R.drawable.ic_home_no_community, false),
            CommunityItem(R.drawable.ic_home_no_community, false),
            CommunityItem(R.drawable.ic_home_no_community, false),
            CommunityItem(R.drawable.ic_home_no_community, false),
            CommunityItem(R.drawable.ic_home_no_community, false)
        )

        val spanCount = 5
        val colSpacingF = resources.getDimension(R.dimen.rv_column_spacing)
        val rowSpacingF = resources.getDimension(R.dimen.rv_row_spacing)

        val deco = object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View,
                parent: RecyclerView, state: RecyclerView.State
            ) {
                val pos = parent.getChildAdapterPosition(view)
                if (pos == RecyclerView.NO_POSITION) return

                val col = pos % spanCount
                val row = pos / spanCount

                // 1) 가로 간격: 항상 col>0
                if (col > 0) {
                    outRect.left = kotlin.math.round(colSpacingF).toInt()
                }

                // 2) 세로 간격: 펼침 모드 && row>0
                if (isExpanded && row > 0) {
                    outRect.top = kotlin.math.round(rowSpacingF).toInt()
                }
            }
        }
        // RecyclerView 초기 세팅
        binding.rvCommunities.apply {
            adapter = CommunityAdapter(rawItems) { }
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            layoutParams = layoutParams.apply {
                height = resources.getDimensionPixelSize(R.dimen.rv_collapsed_height)
            }
            removeItemDecoration(deco)
            addItemDecoration(deco)
        }

        val expandedItems = mutableListOf<CommunityItem>()
        expandedItems += rawItems.take(spanCount)
        var idx = spanCount
        while (idx < rawItems.size) {
            expandedItems += CommunityItem(0, false, isPlaceholder = true)
            val end = (idx + spanCount - 1).coerceAtMost(rawItems.size)
            expandedItems += rawItems.subList(idx, end)
            idx += (spanCount - 1)
        }

        // 어댑터 생성
        val collapseAdapter = CommunityAdapter(rawItems) { item ->
            /* TODO: 클릭 처리 */
        }
        val expandAdapter = CommunityAdapter(expandedItems) { item ->
            /* TODO: 클릭 처리 */
        }

        // 확장 토글
        binding.ivExpand.setOnClickListener {
            isExpanded = !isExpanded
            binding.ivExpand.animate()
                .rotation(if(isExpanded) 90f else 0f)
                .setDuration(200).start()

            binding.rvCommunities.apply {
                if (isExpanded) {
                    adapter = expandAdapter
                    layoutManager = GridLayoutManager(context, spanCount)
                    layoutParams = layoutParams.apply {
                        height = LayoutParams.WRAP_CONTENT
                    }
                } else {
                    adapter = collapseAdapter
                    layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                    layoutParams = layoutParams.apply {
                        height = resources.getDimensionPixelSize(R.dimen.rv_collapsed_height)
                    }
                }
                // Decoration 계산이 바뀌었음을 알려주기
                invalidateItemDecorations()
                requestLayout()
            }
        }

        // 피드 더미 데이터 생성
        val dummyFeeds = listOf(
            FeedItem(
                username = "유저1", content = "뮤지컬 <레미제라블> 보고 왔어요. '민중의 노래' 장면은 언제 봐도 소름... 같이 본 친구도 감동받았대요.".repeat(3),
                imageUrls = listOf(), date = "2025/06/24 01:10", community = "뮤지컬 후기",
                commentCount = 0, likeCount = 1, repostCount = 0, bookmarkCount = 0,
                quotedFeed = FeedItem(
                    username = "유저6",
                    content = "이건 인용된 텍스트 피드예요! 뮤지컬 <지킬 앤 하이드>에서 '지금 이 순간' 장면, 진짜 전설적이죠.",
                    imageUrls = listOf("https://picsum.photos/300/200?random=2", "https://picsum.photos/300/200?random=3", "https://picsum.photos/300/200?random=3","https://picsum.photos/300/200?random=3"),
                    date = "2024/06/14 22:00",
                    community = "레전드 넘버", commentCount = 0, likeCount = 0, repostCount = 0, bookmarkCount = 0
                )
            ),
            FeedItem(
                username = "유저11", content = "이거 인용했어요",
                imageUrls = listOf(), date = "2025/06/24 01:10", community = "인용 피드",
                commentCount = 0, likeCount = 1, repostCount = 0, bookmarkCount = 0,
                quotedFeed = FeedItem(
                    username = "유저12",
                    content = "이건 인용된 텍스트 피드예요",
                    date = "2024/06/14 22:00",
                    community = "인용된 피드", commentCount = 0, likeCount = 0, repostCount = 0, bookmarkCount = 0
                )
            ),
            FeedItem(
                username = "유저2", content = "뮤지컬 <헤드윅>은 진짜 미쳤다... 배우의 에너지가 넘침. 특히 'Midnight Radio' 장면 눈물ㅠㅠ",
                imageUrls = listOf("https://picsum.photos/300/200?random=1"), date = "2025/06/24 02:09", community = "창작 뮤지컬",
                commentCount = 7, likeCount = 55, repostCount = 4, bookmarkCount = 20
            ),
            FeedItem(
                username = "유저3", content = "오늘은 대학로에서 <빨래> 관람했어요. 소극장이라 배우들과 가까워서 몰입감 장난 아님!",
                imageUrls = listOf("https://picsum.photos/300/200?random=2", "https://picsum.photos/300/200?random=3"), date = "2025/06/20 20:30", community = "소극장 뮤지컬",
                commentCount = 2, likeCount = 33, repostCount = 1, bookmarkCount = 7,
                quotedFeed = FeedItem(
                    username = "유저6",
                    content = "이건 인용된 텍스트 피드예요! 뮤지컬 <지킬 앤 하이드>에서 '지금 이 순간' 장면, 진짜 전설적이죠.",
                    imageUrls = listOf("https://picsum.photos/300/200?random=2", "https://picsum.photos/300/200?random=3", "https://picsum.photos/300/200?random=3"),
                    date = "2024/06/14 22:00",
                    community = "레전드 넘버", commentCount = 0, likeCount = 0, repostCount = 0, bookmarkCount = 0
                )
            ),
            FeedItem(
                username = "유저4", content = "뮤지컬 <위키드> 내한공연 보신 분? 글린다랑 엘파바 완전 찰떡이었음... 무대 세트도 대박이에요.",
                imageUrls = listOf("https://picsum.photos/300/200?random=4", "https://picsum.photos/300/200?random=5", "https://picsum.photos/300/200?random=6"), date = "2025/02/19 16:20", community = "해외 뮤지컬",
                commentCount = 9, likeCount = 61, repostCount = 5, bookmarkCount = 18,
                quotedFeed = FeedItem(
                    username = "유저6",
                    content = "이건 인용된 텍스트 피드예요! 뮤지컬 <지킬 앤 하이드>에서 '지금 이 순간' 장면, 진짜 전설적이죠.".repeat(7),
                    date = "2024/06/14 22:00",
                    community = "레전드 넘버", commentCount = 0, likeCount = 0, repostCount = 0, bookmarkCount = 0
                )
            ),
            FeedItem(
                username = "유저5", content = "레베카의 '나는 나만의 것' 듣고 완전 입덕... 이건 진짜 한번쯤 꼭 봐야 해요. 내 최애 넘버 1위.".repeat(6),
                imageUrls = listOf("https://picsum.photos/300/200?random=7", "https://picsum.photos/300/200?random=8", "https://picsum.photos/300/200?random=9", "https://picsum.photos/300/200?random=10"), date = "2024/06/15 10:00", community = "명작 뮤지컬",
                commentCount = 10, likeCount = 78, repostCount = 6, bookmarkCount = 25
            )


        )

        // 피드 리사이클러뷰 초기화
        binding.rvFeeds.apply {
            adapter = FeedAdapter(dummyFeeds)
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun onResume() {
        super.onResume()

        val topBar = requireActivity().findViewById<View>(R.id.top_bar)
        val ivLogo = topBar.findViewById<ImageView>(R.id.iv_logo)
        val tvCommunityName = topBar.findViewById<TextView>(R.id.tv_community_name)
        val ivClose = topBar.findViewById<ImageView>(R.id.iv_close)

        ivLogo.visibility = View.VISIBLE
        tvCommunityName.visibility = View.GONE
        ivClose.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
