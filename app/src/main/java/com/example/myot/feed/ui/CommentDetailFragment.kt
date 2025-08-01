package com.example.myot.comment.ui

import android.R.attr.startY
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Looper
import android.os.Handler
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myot.R
import com.example.myot.databinding.FragmentCommentDetailBinding
import com.example.myot.feed.adapter.CommentDetailAdapter
import com.example.myot.feed.model.FeedItem
import com.example.myot.feed.model.CommentItem
import com.example.myot.feed.ui.FeedDetailFragment
import kotlin.math.min

class CommentDetailFragment : Fragment() {

    private var _binding: FragmentCommentDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CommentDetailAdapter
    private lateinit var feedItem: FeedItem
    private lateinit var comment: CommentItem

    private var isRefreshing = false
    private var isDragging = false
    private var startY = 0f
    private val triggerDistance = 150f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            feedItem = it.getParcelable(ARG_FEED_ITEM)!!
            comment = it.getParcelable(ARG_COMMENT_ITEM)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCommentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupRefresh()

        // 새로고침 초기화
        binding.customRefreshView.apply {
            rotation = 0f
            alpha = 0f
            scaleX = 1f
            scaleY = 1f
        }

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

    private fun setupRecyclerView() {
        adapter = CommentDetailAdapter(comment, feedItem, replies = listOf())
        binding.rvCommentDetail.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCommentDetail.adapter = adapter
    }

    private fun setupRefresh() {
        binding.customRefreshView.isVisible = true
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val nextFragment = parentFragmentManager.findFragmentById(R.id.fragment_container_view)

        if (nextFragment !is com.example.myot.profile.ProfileFragment &&
            nextFragment !is FeedDetailFragment
        ) {
            requireActivity().findViewById<View>(R.id.top_bar).visibility = View.VISIBLE
        }
    }

    companion object {
        private const val ARG_FEED_ITEM = "feed_item"
        private const val ARG_COMMENT_ITEM = "comment_item"

        fun newInstance(commentItem: CommentItem, feedItem: FeedItem): CommentDetailFragment {
            val fragment = CommentDetailFragment()
            val args = Bundle().apply {
                putParcelable(ARG_FEED_ITEM, feedItem)
                putParcelable(ARG_COMMENT_ITEM, commentItem)
            }
            fragment.arguments = args
            return fragment
        }
    }
}