package com.example.myot.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
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
import com.example.myot.MainActivity
import com.example.myot.R
import com.example.myot.community.ui.CommunityFragment
import com.example.myot.databinding.FragmentHomeBinding
import com.example.myot.feed.adapter.FeedAdapter
import com.example.myot.feed.model.FeedItem
import com.example.myot.retrofit2.CommunityService
import com.example.myot.retrofit2.RetrofitClient
import com.example.myot.retrofit2.TokenStore
import com.example.myot.write.WriteFeedActivity
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
                    bookmarkCount = 0,
                    isBookmarked = false
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


        // 피드 리사이클러뷰 초기화
        binding.rvFeeds.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = FeedAdapter(
                items = feedList,
                onDeleteRequest = { postId -> requestDeletePost(postId) },
                onItemClick = { item ->
                    val fragment = com.example.myot.feed.ui.FeedDetailFragment.newInstance(
                        postId = item.id ?: -1L,
                        fallbackFeedItem = item
                    )
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container_view, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            )
        }

        fetchHomeFeed()

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

    private fun formatDate(iso: String?): String {
        if (iso.isNullOrBlank()) return ""
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
            "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy/MM/dd HH:mm"
        )
        for (p in patterns) {
            try {
                val inFmt = java.text.SimpleDateFormat(p, java.util.Locale.getDefault()).apply {
                    if (p.contains("'Z'")) timeZone = java.util.TimeZone.getTimeZone("UTC")
                }
                val d = inFmt.parse(iso) ?: continue
                val outFmt = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.getDefault())
                return outFmt.format(d)
            } catch (_: Exception) { }
        }
        return iso
    }

    private fun HomeFeedPost.toFeedItem(): FeedItem {
        val quotedFeed: FeedItem? = null // 서버 스펙상 인용 필드는 없음
        return FeedItem(
            id = this.id?.toLong() ?: -1L,
            username = user?.nickname ?: "",
            content = content ?: "",
            imageUrls = (postImages ?: emptyList()).mapNotNull { it.url }.filter { it.isNullOrBlank().not() },
            date = formatDate(createdAt),
            community = community?.type ?: "",     // 필요시 커뮤니티 표시 규칙에 맞게 변경
            commentCount = commentCount ?: 0,
            likeCount = likeCount ?: 0,
            repostCount = repostCount ?: 0,
            bookmarkCount = bookmarkCount ?: 0,
            isLiked = postLikes ?: false,
            isBookmarked = postBookmarks ?: false,
            profileImageUrl = user?.profileImage,
            communityCoverUrl = community?.coverImage,
            userHandle = null
        )
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun renderCommunityStrip(list: List<CommunityUi>) {
        val container = binding.lineCommunities
        container.removeAllViews()

        fun addIcon(item: CommunityUi?) {
            val root = layoutInflater.inflate(R.layout.item_community, container, false)
            val params = LinearLayout.LayoutParams(dp(85), dp(85)).apply {
                marginStart = dp(2)
                marginEnd   = dp(-5)
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

    private fun fetchHomeFeed() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val raw = TokenStore.loadAccessToken(requireContext())
                if (raw.isNullOrBlank()) {
                    Toast.makeText(requireContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val cleaned = raw.trim().removePrefix("Bearer ").trim().removeSurrounding("\"")
                val bearer = "Bearer $cleaned"

                val res = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    com.example.myot.retrofit2.RetrofitClient.homeFeedService.getHomeFeed(bearer)
                }

                if (!res.isSuccessful) {
                    Toast.makeText(requireContext(), "홈 피드 불러오기 실패 (${res.code()})", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val envelope = res.body()
                if (envelope?.resultType != "SUCCESS") {
                    feedList.clear()
                    binding.rvFeeds.adapter?.notifyDataSetChanged()
                    return@launch
                }

                val posts = envelope.data?.posts ?: emptyList()
                val mapped = posts.map { it.toFeedItem() }

                feedList.clear()
                feedList.addAll(mapped)
                binding.rvFeeds.adapter?.notifyDataSetChanged()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestDeletePost(postId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val raw = TokenStore.loadAccessToken(requireContext()) ?: return@launch
                val cleaned = raw.trim().removePrefix("Bearer ").trim().removeSurrounding("\"")
                val bearer = "Bearer $cleaned"

                val res = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    RetrofitClient.feedService.deletePost(bearer, postId)
                }

                val ok = res.isSuccessful
                if (ok) {
                    // 목록이라면 리스트에서 제거
                    val idx = feedList.indexOfFirst { it.id == postId }
                    if (idx >= 0) {
                        feedList.removeAt(idx)
                        binding.rvFeeds.adapter?.notifyItemRemoved(idx)
                    }
                } else {
                    showToast("본인이 작성한 게시글만 삭제할 수 있어요.")
                }
            } catch (_: Exception) {
                showToast("본인이 작성한 게시글만 삭제할 수 있어요.")
            }
        }
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    private fun showToast(message: String) {
        val v = layoutInflater.inflate(R.layout.toast_simple, null)
        v.findViewById<TextView>(R.id.tv_toast).text = message

        Toast(requireContext()).apply {
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 64.dp)
            view = v
        }.show()
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