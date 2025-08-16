package com.example.myot.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.myot.R
import com.example.myot.community.ui.CommunityFragment
import com.example.myot.databinding.FragmentHomeBinding
import com.example.myot.feed.adapter.FeedAdapter
import com.example.myot.feed.model.FeedItem
import com.example.myot.retrofit2.CommunityService
import com.example.myot.retrofit2.RetrofitClient
import com.example.myot.retrofit2.TokenStore
import com.example.myot.write.WriteFeedActivity
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


    private val feedList = mutableListOf<FeedItem>()
    private lateinit var feedAdapter: FeedAdapter

    // 새로고침 변수
    private var isRefreshing = false
    private var isDragging = false
    private var startY = 0f
    private val triggerDistance = 150f

    private val writeFeedResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult

                val isPublic = data.getBooleanExtra("isPublic", true)
                val content = data.getStringExtra("content") ?: ""
                val imageUrls = data.getStringArrayListExtra("imageUrls") ?: arrayListOf()

                val newFeed = FeedItem(
                    username = "마이오티",
                    content = content,
                    imageUrls = imageUrls,
                    date = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date()),
                    community = if (isPublic) "전체 공개" else "친구 공개", // 임시 처리
                    commentCount = 0,
                    likeCount = 0,
                    repostCount = 0,
                    quoteCount = 0
                )

                feedList.add(0, newFeed)
                binding.rvFeeds.adapter?.notifyDataSetChanged()
                binding.rvFeeds.scrollToPosition(0)
            }
        }

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
                            val pullDistance = min(dy / 2f, 200f)
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

        renderCommunityStrip(emptyList())
        fetchMyCommunitiesWithCovers()

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
        feedList.addAll(dummyFeeds)


        // 피드 리사이클러뷰 초기화
        binding.rvFeeds.apply {
            adapter = FeedAdapter(feedList)
            layoutManager = LinearLayoutManager(requireContext())
        }

        // 글쓰기 버튼 스크롤 시 투명도 처리
        val handler = Handler(Looper.getMainLooper())
        val restoreFabAlphaRunnable = Runnable {
            binding.btnEdit.animate().alpha(1f).setDuration(200).start()
        }

        // 글쓰기 기능
        binding.btnEdit.setOnClickListener {
            val intent = Intent(requireContext(), WriteFeedActivity::class.java)
            writeFeedResultLauncher.launch(intent)
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

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun renderCommunityStrip(list: List<CommunityUi>) {
        val container = binding.lineCommunities
        container.removeAllViews()

        fun addIcon(item: CommunityUi?) {
            val root = layoutInflater.inflate(R.layout.item_community, container, false)
            val params = LinearLayout.LayoutParams(dp(85), dp(85)).apply {
                marginStart = dp(5)
                marginEnd   = dp(-10)
            }
            root.layoutParams = params

            val ivCommunity = root.findViewById<ImageView>(R.id.iv_community)
            val ivPlus = root.findViewById<ImageView>(R.id.iv_plus)
            val ivRing = root.findViewById<ImageView>(R.id.iv_ring)

            if (item == null) {
                // + 버튼
                ivRing.visibility = View.GONE
                ivCommunity.visibility = View.GONE
                ivPlus.visibility = View.VISIBLE

                root.setOnClickListener {
                    // TODO: 커뮤니티 검색/추가 화면 이동
                }
            } else {
                // 커뮤니티 항목
                ivPlus.visibility = View.GONE
                ivRing.visibility = View.VISIBLE

                val url = item.coverImage

                if (url.isNullOrBlank()) {
                    ivCommunity.setImageDrawable(null)
                    ivCommunity.visibility = View.GONE
                } else {
                    ivCommunity.visibility = View.INVISIBLE
                    Glide.with(ivCommunity)
                        .load(url)
                        .circleCrop()
                        .into(object : com.bumptech.glide.request.target.CustomTarget<android.graphics.drawable.Drawable>() {
                            override fun onResourceReady(
                                resource: android.graphics.drawable.Drawable,
                                transition: com.bumptech.glide.request.transition.Transition<in android.graphics.drawable.Drawable>?
                            ) {
                                ivCommunity.setImageDrawable(resource)
                                ivCommunity.visibility = View.VISIBLE
                            }

                            override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                                ivCommunity.setImageDrawable(null)
                                ivCommunity.visibility = View.GONE
                            }

                            override fun onLoadFailed(errorDrawable: android.graphics.drawable.Drawable?) {
                                super.onLoadFailed(errorDrawable)
                                ivCommunity.setImageDrawable(null)
                                ivCommunity.visibility = View.GONE
                            }
                        })
                }

                root.setOnClickListener {
                    val fragment = CommunityFragment.newInstance(item.type.uppercase())
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container_view, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }

            container.addView(root)
        }

        // + 버튼 하나 먼저
        addIcon(null)
        // 커뮤니티들
        list.forEach { addIcon(it) }

        binding.hsCommunities.post {
            binding.hsCommunities.scrollTo(0, 0)
            binding.hsCommunities.isHorizontalScrollBarEnabled = false
        }
    }
    private fun fetchMyCommunitiesWithCovers() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val raw = TokenStore.loadAccessToken(requireContext())
                if (raw.isNullOrBlank()) {
                    renderCommunityStrip(emptyList())
                    Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val cleaned = raw.trim().removePrefix("Bearer ").trim().removeSurrounding("\"")
                val bearer = "Bearer $cleaned"
                val service: CommunityService = RetrofitClient.communityService

                val mineRes = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    service.getMyCommunities(bearer)
                }
                if (!mineRes.isSuccessful) {
                    renderCommunityStrip(emptyList())
                    return@launch
                }
                val body = mineRes.body()
                val mine = if (body?.success == true) body.communities else emptyList()

                val baseUi = mine.map {
                    CommunityUi(
                        id = it.communityId,
                        type = it.type,
                        name = it.communityName,
                        coverImage = null
                    )
                }
                renderCommunityStrip(baseUi)

                if (baseUi.isNotEmpty()) {
                    val withCovers = withContext(kotlinx.coroutines.Dispatchers.IO) {
                        baseUi.map { ui ->
                            try {
                                val detailRes = service.getCommunityDetail(bearer, ui.type, ui.id)
                                val cover = if (detailRes.isSuccessful) {
                                    detailRes.body()?.community?.coverImage
                                } else null
                                ui.copy(coverImage = cover)
                            } catch (_: Exception) {
                                ui
                            }
                        }
                    }
                    renderCommunityStrip(withCovers)
                }
            } catch (e: Exception) {
                renderCommunityStrip(emptyList())
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val topBar = requireActivity().findViewById<View>(R.id.top_bar)
        val ivLogo = topBar.findViewById<ImageView>(R.id.iv_logo)
        val tvCommunityName = topBar.findViewById<TextView>(R.id.tv_community_name)

        ivLogo.visibility = View.VISIBLE
        tvCommunityName.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}