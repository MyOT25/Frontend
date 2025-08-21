package com.example.myot.feed.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myot.databinding.*
import com.example.myot.feed.data.CreateCommentRequest
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myot.R
import com.example.myot.comment.ui.CommentDetailFragment
import com.example.myot.feed.model.CommentItem
import com.example.myot.feed.model.FeedItem
import com.example.myot.feed.adapter.FeedDetailAdapter
import com.example.myot.feed.model.toFeedItem
import com.example.myot.feed.model.toCommentItem
import com.example.myot.retrofit2.RetrofitClient
import com.example.myot.retrofit2.TokenStore
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min

class FeedDetailFragment : Fragment() {

    companion object {
        fun newInstance(feedItem: FeedItem): FeedDetailFragment =
            newInstance(postId = feedItem.id ?: -1L, fallbackFeedItem = feedItem)

        fun newInstance(postId: Long, fallbackFeedItem: FeedItem?): FeedDetailFragment {
            return FeedDetailFragment().apply {
                arguments = Bundle().apply {
                    putLong("postId", postId)
                    putParcelable("fallback", fallbackFeedItem)
                }
            }
        }
    }

    private lateinit var binding: FragmentFeedDetailBinding
    private var feedItemArg: FeedItem? = null
    private var postIdArg: Long = -1L

    private lateinit var adapter: FeedDetailAdapter
    private val commentItems = mutableListOf<CommentItem>()

    // 새로고침 변수
    private var isRefreshing = false
    private var isDragging = false
    private var startY = 0f
    private val triggerDistance = 150f
    private var headerForUi: FeedItem? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.post { openFeedCommentEditor() }
        super.onViewCreated(view, savedInstanceState)
        requireActivity().findViewById<View>(R.id.top_bar).visibility = View.GONE

        postIdArg = arguments?.getLong("postId", -1L) ?: -1L
        feedItemArg = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("fallback", FeedItem::class.java)
        } else {
            @Suppress("DEPRECATION") arguments?.getParcelable("fallback")
        }

        val baseItem = feedItemArg ?: FeedItem(
            id = postIdArg,
            username = "",
            content = "",
            imageUrls = emptyList(),
            date = "",
            community = "",
            commentCount = 0, likeCount = 0, repostCount = 0, quoteCount = 0
        )
        headerForUi = baseItem

        binding.customRefreshView.apply {
            rotation = 0f
            alpha = 0f
            scaleX = 1f
            scaleY = 1f
        }

        adapter = FeedDetailAdapter(
            feedItem = baseItem,
            comments = commentItems,
            onDeleteRequest = { postId -> requestDeletePost(postId) },
            onCommentClick = { openFeedCommentEditor() }
        )
        binding.rvFeedDetail.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFeedDetail.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val raw = TokenStore.loadAccessToken(requireContext())
                val bearer = raw?.trim()
                    ?.removePrefix("Bearer ")
                    ?.trim()
                    ?.removeSurrounding("\"")
                    ?.let { "Bearer $it" }
                    ?: ""

                if (postIdArg <= 0L) {
                    return@launch
                }

                    // 1) 게시글 상세
                val detailRes = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    RetrofitClient.feedService.getPostDetail(bearer, postIdArg)
                }

                var feedForUi: FeedItem = baseItem
                if (detailRes.isSuccessful) {
                    val data = detailRes.body()?.data
                    if (data != null) {
                        val mappedRaw = data.toFeedItem()
                        val resolvedLiked = data.isLiked ?: feedItemArg?.isLiked ?: mappedRaw.isLiked
                        val resolvedLikeCount = data.likeCount ?: feedItemArg?.likeCount ?: mappedRaw.likeCount
                        val resolvedBookmarked = data.isBookmarked ?: feedItemArg?.isBookmarked ?: mappedRaw.isBookmarked
                        val resolvedBookmarkCount = data.bookmarkCount ?: feedItemArg?.bookmarkCount ?: mappedRaw.bookmarkCount
                        val resolvedReposted = data.isRepost ?: feedItemArg?.isReposted ?: mappedRaw.isReposted
                        val resolvedCommented = feedItemArg?.isCommented ?: mappedRaw.isCommented

                        feedForUi = mappedRaw.copy(
                            isLiked = resolvedLiked,
                            likeCount = resolvedLikeCount,
                            isBookmarked = resolvedBookmarked,
                            bookmarkCount = resolvedBookmarkCount,
                            isReposted = resolvedReposted,
                            isCommented = resolvedCommented
                        )
                        headerForUi = feedForUi
                    } else {
                        Toast.makeText(requireContext(), "상세 데이터가 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "상세 불러오기 실패 (${detailRes.code()})", Toast.LENGTH_SHORT).show()
                }

                // 2) 댓글 목록 (success 배열 기반)
                var commentsForUi: List<CommentItem> = emptyList()
                val commentsRes = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    RetrofitClient.feedService.getPostComments(bearer, postIdArg)
                }
                if (commentsRes.isSuccessful) {
                    val list = commentsRes.body()?.success ?: emptyList()
                    commentsForUi = list.map { it.toCommentItem() }
                    // 댓글 수 동기화
                    feedForUi = feedForUi.copy(commentCount = commentsForUi.size)
                    headerForUi = feedForUi
                } else {
                    Toast.makeText(requireContext(), "댓글 불러오기 실패 (${commentsRes.code()})", Toast.LENGTH_SHORT).show()
                }

                adapter = FeedDetailAdapter(
                    feedItem = feedForUi,
                    comments = commentsForUi.toMutableList(),
                    onDeleteRequest = { postId -> requestDeletePost(postId) },
                    onCommentClick = { openFeedCommentEditor() }
                )

                binding.rvFeedDetail.adapter = adapter
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // 새로고침 기능
        binding.nestedScrollView.setOnTouchListener { _, event ->
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
                            return@setOnTouchListener true
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

    }

    private fun requestDeletePost(postId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val raw = com.example.myot.retrofit2.TokenStore.loadAccessToken(requireContext()) ?: return@launch
                val bearer = raw.trim()
                    .removePrefix("Bearer ")
                    .trim()
                    .removeSurrounding("\"")
                    .let { "Bearer $it" }

                val res = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    com.example.myot.retrofit2.RetrofitClient.feedService.deletePost(bearer, postId)
                }

                if (res.isSuccessful) {
                    parentFragmentManager.setFragmentResult(
                        "post_deleted",
                        android.os.Bundle().apply { putLong("postId", postId) }
                    )
                    parentFragmentManager.popBackStack()
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
        val v = layoutInflater.inflate(com.example.myot.R.layout.toast_simple, null)
        v.findViewById<TextView>(com.example.myot.R.id.tv_toast).text = message

        Toast(requireContext()).apply {
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 64.dp)
            view = v
        }.show()
    }

    private fun openFeedCommentEditor() {
        val act = requireActivity() as com.example.myot.MainActivity
        act.showCommentBar(
            scrollable = binding.nestedScrollView,
            hint = "댓글을 입력하세요",
            allowAnonymous = false,
            onSend = { text, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val raw = TokenStore.loadAccessToken(requireContext())
                        val bearer = raw?.trim()
                            ?.removePrefix("Bearer ")
                            ?.trim()
                            ?.removeSurrounding("\"")
                            ?.let { "Bearer $it" }
                            ?: ""

                        val postId = postIdArg
                        if (postId <= 0L) {
                            Toast.makeText(requireContext(), "잘못된 게시물입니다.", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        val res = withContext(kotlinx.coroutines.Dispatchers.IO) {
                            RetrofitClient.feedService.createComment(
                                token = bearer,
                                postId = postId,
                                body = CreateCommentRequest(content = text)
                            )
                        }

                        if (res.isSuccessful && res.body()?.success != null) {
                            // 작성 성공 후, 최신 댓글 목록 재요청 (이름/프로필/내용까지 채워진 데이터 반영)
                            val listRes = withContext(kotlinx.coroutines.Dispatchers.IO) {
                                RetrofitClient.feedService.getPostComments(bearer, postId)
                            }
                            if (listRes.isSuccessful) {
                                val freshComments = listRes.body()?.success?.map { it.toCommentItem() } ?: emptyList()

                                // 헤더 댓글 수 반영
                                val newHeader = (headerForUi ?: FeedItem(
                                    id = postId,
                                    username = "",
                                    content = "",
                                    imageUrls = emptyList(),
                                    date = "",
                                    community = "",
                                    commentCount = 0,
                                    likeCount = 0,
                                    repostCount = 0,
                                    quoteCount = 0
                                )).copy(
                                    commentCount = freshComments.size
                                )
                                headerForUi = newHeader

                                // 어댑터 갱신
                                adapter = FeedDetailAdapter(
                                    feedItem = newHeader,
                                    comments = freshComments.toMutableList(),
                                    onDeleteRequest = { pid -> requestDeletePost(pid) },
                                    onCommentClick = { openFeedCommentEditor() }
                                )
                                binding.rvFeedDetail.adapter = adapter

                                // 입력창 닫기
                                act.hideKeyboardAndClearFocus()
                                act.hideCommentBar()
                            } else {
                                Toast.makeText(requireContext(), "댓글 새로고침 실패 (${listRes.code()})", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(requireContext(), "댓글 등록 실패 (${res.code()})", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "네트워크 오류: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    override fun onPause() {
        super.onPause()
        (activity as? com.example.myot.MainActivity)?.apply {
            hideKeyboardAndClearFocus()
            hideCommentBar()
        }
    }

    override fun onDestroyView() {
        (activity as? com.example.myot.MainActivity)?.apply {
            hideKeyboardAndClearFocus()
            hideCommentBar()
        }

        super.onDestroyView()

        val nextFragment = parentFragmentManager.findFragmentById(R.id.fragment_container_view)
        if (nextFragment !is com.example.myot.profile.ProfileFragment &&
            nextFragment !is com.example.myot.comment.ui.CommentDetailFragment
        ) {
            requireActivity().findViewById<View>(R.id.top_bar).visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.top_bar).visibility = View.GONE
    }

}
