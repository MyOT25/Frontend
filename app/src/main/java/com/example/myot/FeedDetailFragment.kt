package com.example.myot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myot.databinding.*
import androidx.recyclerview.widget.LinearLayoutManager

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
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var feedItem: FeedItem

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 안전하게 getParcelable 처리
        feedItem = requireArguments().getParcelable("feedItem")!!

        // 탑바에서 커뮤니티 이름 표시, 로고 숨김
        val topBar = requireActivity().findViewById<View>(R.id.top_bar)
        val ivLogo = topBar.findViewById<ImageView>(R.id.iv_logo)
        val tvCommunityName = topBar.findViewById<TextView>(R.id.tv_community_name)

        ivLogo.visibility = View.GONE
        tvCommunityName.visibility = View.VISIBLE
        tvCommunityName.text = feedItem.community

        // 피드 상단 고정 뷰 설정
        val feedView = createFeedView(feedItem)
        binding.containerFeed.removeAllViews()
        binding.containerFeed.addView(feedView)

        // 댓글 RecyclerView 설정
        val commentList = listOf(
            "재밌어요", "잘 봤어요!", "동의합니다", "질문 있어요", "감사합니다"
        )
        commentAdapter = CommentAdapter(commentList)
        binding.rvComments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvComments.adapter = commentAdapter
    }

    private fun createFeedView(item: FeedItem): View {
        return when (item.imageUrls.size) {
            0 -> ItemFeedTextOnlyBinding.inflate(layoutInflater).apply {
                TextOnlyViewHolder(this).bind(item, isDetail = true)
            }.root
            1 -> ItemFeedImage1Binding.inflate(layoutInflater).apply {
                ImageViewHolder(this).bind(item, isDetail = true)
            }.root
            2 -> ItemFeedImage2Binding.inflate(layoutInflater).apply {
                ImageViewHolder(this).bind(item, isDetail = true)
            }.root
            3 -> ItemFeedImage3Binding.inflate(layoutInflater).apply {
                ImageViewHolder(this).bind(item, isDetail = true)
            }.root
            else -> ItemFeedImage4Binding.inflate(layoutInflater).apply {
                ImageViewHolder(this).bind(item, isDetail = true)
            }.root
        }
    }
}
