package com.example.myot.feed.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myot.databinding.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myot.R
import com.example.myot.comment.ui.CommentDetailFragment
import com.example.myot.feed.model.CommentItem
import com.example.myot.feed.model.FeedItem
import com.example.myot.feed.adapter.FeedDetailAdapter
import kotlin.math.min

class FeedDetailFragment : Fragment() {

    companion object {
        fun newInstance(feedItem: FeedItem): FeedDetailFragment {
            val fragment = FeedDetailFragment()
            val bundle = Bundle()
            bundle.putParcelable("feedItem", feedItem)
            fragment.arguments = bundle
            return fragment
        }
    }

    private lateinit var binding: FragmentFeedDetailBinding
    private lateinit var feedItem: FeedItem

    // 새로고침 변수
    private var isRefreshing = false
    private var isDragging = false
    private var startY = 0f
    private val triggerDistance = 150f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().findViewById<View>(R.id.top_bar).visibility = View.GONE

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

        // 벡엔드 연동시 제거
        feedItem = requireArguments().getParcelable("feedItem")!!

        // 더미 데이터
        val commentList = listOf(
            CommentItem(
                username = "뮤덕이",
                userid = "@user1",
                content = "민중의 노래 듣고 울컥했어요...",
                date = "2025/06/26 22:10",
                commentCount = 2, likeCount = 5, repostCount = 1, quoteCount = 0
            ),
            CommentItem(
                username = "레미광팬",
                userid = "@user2",
                content = "@user1 레미제라블 다섯 번 봤는데도 또 보고 싶네요!",
                date = "2025/06/26 21:45",
                commentCount = 3, likeCount = 12, repostCount = 2, quoteCount = 1
            ),
            CommentItem(
                username = "감성러버",
                userid = "@user3",
                content = "친구랑 본 첫 뮤지컬이었는데, 너무 인상 깊었어요.",
                date = "2025/06/26 21:02",
                commentCount = 1, likeCount = 3, repostCount = 0, quoteCount = 2
            ),
            CommentItem(
                username = "장발장",
                userid = "@user4",
                content = "엔딩 장면에서 전율... 눈물 나더라구요.",
                date = "2025/06/26 19:30",
                commentCount = 5, likeCount = 9, repostCount = 3, quoteCount = 0
            ),
            CommentItem(
                username = "후기남",
                userid = "@user5",
                content = "레미제라블 처음 봤는데 왜 이제 봤을까요? 다음엔 가족이랑 다시 볼래요.",
                date = "2025/06/26 18:50",
                commentCount = 4, likeCount = 6, repostCount = 1, quoteCount = 1
            ),
            CommentItem(
                username = "연뮤러",
                userid = "@user6",
                content = "민중의 노래 부르는데 관객들 반응도 장난 아니었음.".repeat(10),
                date = "2025/06/26 17:42",
                commentCount = 0, likeCount = 7, repostCount = 1, quoteCount = 0
            ),
            CommentItem(
                username = "소감왕",
                userid = "@user7",
                content = "자리에 앉자마자 몰입감 최고였고 무대 세트 진짜 예술이었음!!",
                date = "2025/06/26 17:10",
                commentCount = 1, likeCount = 4, repostCount = 1, quoteCount = 2
            ),
            CommentItem(
                userid = "@user8",
                username = "극장덕후",
                content = "이번 무대 조명 진짜 찢었어요. 감탄만 나옴.",
                date = "2025/06/26 16:30",
                commentCount = 2, likeCount = 10, repostCount = 0, quoteCount = 3
            ),
            CommentItem(
                username = "노래듣는중",
                userid = "@user9",
                content = "지금도 '민중의 노래' 계속 듣고 있음ㅋㅋ",
                date = "2025/06/26 15:50",
                commentCount = 0, likeCount = 2, repostCount = 0, quoteCount = 0
            ),
            CommentItem(
                username = "현장감",
                userid = "@user10",
                content = "@user9 현장에서 느낀 감동은 영상으로 못 담음. 무조건 실관람 ㄱㄱ",
                date = "2025/06/26 15:00",
                commentCount = 3, likeCount = 5, repostCount = 2, quoteCount = 2
            ),
            CommentItem(
                username = "긴글유저",
                userid = "@user11",
                content = "이번 공연은 정말 최고였습니다. 배우들의 표정 하나하나에서 감정이 살아 있었고, 특히 마지막 장면에서는 관객 전체가 숨을 멈춘 듯한 느낌이었습니다. 이 감동을 어떻게 말로 표현할 수 있을지 모르겠어요. 기회가 된다면 무조건 꼭!! 다시 보고 싶은 공연이에요.",
                date = "2025/06/26 14:30",
                commentCount = 6, likeCount = 20, repostCount = 5, quoteCount = 4
            ),
            CommentItem(
                username = "긴글러",
                userid = "@user12",
                content = "뮤지컬을 많이 본 편은 아니지만 이번 <레미제라블> 공연은 특별했습니다. 무대 전환과 조명의 활용, 배우들의 감정 표현까지 어느 하나도 허투루 넘길 수 없었고, 모든 장면에서 연출의 치밀함이 느껴졌습니다. 이런 작품을 직접 볼 수 있어 정말 감사했어요. 나중에 시간이 되면 다른 배우들의 연극도 보고 싶어요!",
                date = "2025/06/26 13:45",
                commentCount = 7, likeCount = 18, repostCount = 4, quoteCount = 3
            )
        )

        val adapter = FeedDetailAdapter(feedItem, commentList)
        binding.rvFeedDetail.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFeedDetail.adapter = adapter

    }

    override fun onDestroyView() {
        super.onDestroyView()

        val nextFragment = parentFragmentManager.findFragmentById(R.id.fragment_container_view)

        if (nextFragment !is com.example.myot.profile.ProfileFragment &&
            nextFragment !is CommentDetailFragment
        ) {
            requireActivity().findViewById<View>(R.id.top_bar).visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.top_bar).visibility = View.GONE
    }
}
