package com.example.myot.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myot.R
import com.example.myot.community.ui.CommunityFragment
import com.example.myot.databinding.FragmentHomeBinding
import com.example.myot.feed.adapter.FeedAdapter
import com.example.myot.feed.model.FeedItem
import kotlin.math.min

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var isExpanded = false

    // 새로고침 변수
    private var isRefreshing = false
    private var isDragging = false
    private var startY = 0f
    private val triggerDistance = 150f

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // 새로고침 초기화
        binding.customRefreshView.apply {
            rotation = 0f
            alpha = 0f
            scaleX = 1f
            scaleY = 1f
        }

        // 새로고침 작동
        binding.nestedScrollView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (!binding.nestedScrollView.canScrollVertically(-1)) {
                        isDragging = true
                        startY = event.rawY
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    if (isDragging && !isRefreshing) {
                        val dy = event.rawY - startY
                        if (dy > 0) {
                            val pullDistance = min(dy / 2f, 200f)  // 최대 200까지만 내려가게
                            binding.nestedScrollView.translationY = pullDistance
                            binding.customRefreshView.setProgress(pullDistance / triggerDistance)
                            return@setOnTouchListener true  // 스크롤 막기
                        }
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isDragging) {
                        isDragging = false
                        val currentY = binding.nestedScrollView.translationY
                        if (currentY >= triggerDistance) {
                            isRefreshing = true
                            binding.customRefreshView.startLoading()
                            // 새로고침 동작 후 종료 시 reset() 호출
                            Handler(Looper.getMainLooper()).postDelayed({
                                binding.nestedScrollView.animate().translationY(0f).setDuration(300).start()
                                binding.customRefreshView.reset()
                                isRefreshing = false
                            }, 1500)
                        } else {
                            binding.nestedScrollView.animate().translationY(0f).setDuration(300).start()
                            binding.customRefreshView.reset()
                        }
                    }
                }
            }
            false
        }


        // 커뮤니티 리사이클러뷰 초기화
        val communityAdapter = CommunityGroupAdapter()
        binding.rvCommunities.itemAnimator = null
        binding.rvCommunities.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvCommunities.adapter = communityAdapter

        communityAdapter.setTotalItems(8) // 원하는 개수 입력 + 1

        binding.ivExpand.setOnClickListener {
            isExpanded = !isExpanded

            // 회전 애니메이션 처리
            binding.ivExpand.animate()
                .rotation(if (isExpanded) 90f else 0f)
                .setDuration(200)
                .start()

            communityAdapter.isExpanded = isExpanded
            communityAdapter.notifyItemChanged(0)
        }

        /// 커뮤니티 임시 이동 코드
        communityAdapter.onCommunityClick = { clickedIndex ->
            val fragment = CommunityFragment.newInstance("MUSICAL")
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, fragment)
                .addToBackStack(null)
                .commit()
        }

        // 피드 더미 데이터 생성
        val dummyFeeds = listOf(
            FeedItem(
                username = "유저1",
                content = "뮤지컬 <레미제라블> 보고 왔어요. '민중의 노래' 장면은 언제 봐도 소름... 같이 본 친구도 감동받았대요.".repeat(3),
                imageUrls = listOf(),
                date = "2025/06/24 01:10",
                community = "뮤지컬 후기",
                commentCount = 0,
                likeCount = 1,
                repostCount = 0,
                quoteCount = 0,
                quotedFeed = FeedItem(
                    username = "유저6",
                    content = "이건 인용된 텍스트 피드예요! 뮤지컬 <지킬 앤 하이드>에서 '지금 이 순간' 장면, 진짜 전설적이죠.",
                    imageUrls = listOf(
                        "https://picsum.photos/300/200?random=2",
                        "https://picsum.photos/300/200?random=3",
                        "https://picsum.photos/300/200?random=3",
                        "https://picsum.photos/300/200?random=3"
                    ),
                    date = "2024/06/14 22:00",
                    community = "레전드 넘버",
                    commentCount = 0,
                    likeCount = 0,
                    repostCount = 0,
                    quoteCount = 0
                )
            ),
            FeedItem(
                username = "유저11", content = "이거 인용했어요",
                imageUrls = listOf(), date = "2025/06/24 01:10", community = "인용 피드",
                commentCount = 0, likeCount = 1, repostCount = 0, quoteCount = 0,
                quotedFeed = FeedItem(
                    username = "유저12",
                    content = "이건 인용된 텍스트 피드예요",
                    date = "2024/06/14 22:00",
                    community = "인용된 피드",
                    commentCount = 0,
                    likeCount = 0,
                    repostCount = 0,
                    quoteCount = 0
                )
            ),
            FeedItem(
                username = "유저2",
                content = "뮤지컬 <헤드윅>은 진짜 미쳤다... 배우의 에너지가 넘침. 특히 'Midnight Radio' 장면 눈물ㅠㅠ",
                imageUrls = listOf("https://picsum.photos/270/380?random=11"),
                date = "2025/06/24 02:09",
                community = "창작 뮤지컬",
                commentCount = 7,
                likeCount = 55,
                repostCount = 4,
                quoteCount = 20
            ),
            FeedItem(
                username = "유저3", content = "오늘은 대학로에서 <빨래> 관람했어요. 소극장이라 배우들과 가까워서 몰입감 장난 아님!",
                imageUrls = listOf(
                    "https://picsum.photos/300/200?random=2",
                    "https://picsum.photos/300/200?random=3"
                ), date = "2025/06/20 20:30", community = "소극장 뮤지컬",
                commentCount = 2, likeCount = 33, repostCount = 1, quoteCount = 7,
                quotedFeed = FeedItem(
                    username = "유저6",
                    content = "이건 인용된 텍스트 피드예요! 뮤지컬 <지킬 앤 하이드>에서 '지금 이 순간' 장면, 진짜 전설적이죠.",
                    imageUrls = listOf(
                        "https://picsum.photos/300/200?random=2",
                        "https://picsum.photos/300/200?random=3",
                        "https://picsum.photos/300/200?random=3"
                    ),
                    date = "2024/06/14 22:00",
                    community = "레전드 넘버",
                    commentCount = 0,
                    likeCount = 0,
                    repostCount = 0,
                    quoteCount = 0
                )
            ),
            FeedItem(
                username = "유저4",
                content = "이 문장은 안드로이드 앱 개발자들이 레이아웃 테스트나 글자 수 제한 기능을 점검할 때 사용할 수 있도록 만든 예시이며, 띄어쓰기 포함 정확히 160자입니다. 한글이든 영어이든 기호이든 띄어쓰기이든 전부 한 글자로 하여 총 160글자입니다. 이 글은 160자를 테스트하기 위한 글로 이제끝",
                imageUrls = listOf(
                    "https://picsum.photos/300/200?random=4",
                    "https://picsum.photos/300/200?random=5",
                    "https://picsum.photos/300/200?random=6"
                ),
                date = "2025/02/19 16:20",
                community = "해외 뮤지컬",
                commentCount = 9,
                likeCount = 61,
                repostCount = 5,
                quoteCount = 18,
                quotedFeed = FeedItem(
                    username = "유저6",
                    content = "이건 인용된 텍스트 피드예요! 뮤지컬 <지킬 앤 하이드>에서 '지금 이 순간' 장면, 진짜 전설적이죠.".repeat(7),
                    date = "2024/06/14 22:00",
                    community = "레전드 넘버",
                    commentCount = 0,
                    likeCount = 0,
                    repostCount = 0,
                    quoteCount = 0
                )
            ),
            FeedItem(
                username = "유저5",
                content = "레베카의 '나는 나만의 것' 듣고 완전 입덕... 이건 진짜 한번쯤 꼭 봐야 해요. 내 최애 넘버 1위.".repeat(6),
                imageUrls = listOf(
                    "https://picsum.photos/300/200?random=7",
                    "https://picsum.photos/300/200?random=8",
                    "https://picsum.photos/300/200?random=9",
                    "https://picsum.photos/300/200?random=10"
                ),
                date = "2024/06/15 10:00",
                community = "명작 뮤지컬",
                commentCount = 10,
                likeCount = 78,
                repostCount = 6,
                quoteCount = 25
            )


        )

        // 피드 리사이클러뷰 초기화
        binding.rvFeeds.apply {
            adapter = FeedAdapter(dummyFeeds)
            layoutManager = LinearLayoutManager(requireContext())
        }

        // 글쓰기 버튼 스크롤 시 투명도 처리
        val handler = Handler(Looper.getMainLooper())
        val restoreFabAlphaRunnable = Runnable {
            binding.btnEdit.animate().alpha(1f).setDuration(200).start()
        }

        binding.nestedScrollView.setOnScrollChangeListener { v, _, scrollY, _, oldScrollY ->
            val scrollView = v as NestedScrollView
            val view = scrollView.getChildAt(0)
            val diff = view.bottom - (scrollView.height + scrollY)

            if (diff <= 60) {
                // 스크롤이 끝까지 내려갔을 때
                handler.removeCallbacks(restoreFabAlphaRunnable)
                binding.btnEdit.animate().alpha(0f).setDuration(200).start()
            } else if (scrollY != oldScrollY) {
                // 스크롤 중인 경우
                binding.btnEdit.alpha = 0.3f
                handler.removeCallbacks(restoreFabAlphaRunnable)
                handler.postDelayed(restoreFabAlphaRunnable, 300)
            }
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