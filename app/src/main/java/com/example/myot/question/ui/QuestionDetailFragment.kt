package com.example.myot.question.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myot.databinding.FragmentQuestionDetailBinding
import com.example.myot.feed.model.CommentItem
import com.example.myot.question.adapter.QuestionDetailAdapter
import com.example.myot.question.model.QuestionItem

class QuestionDetailFragment : Fragment() {

    private var _binding: FragmentQuestionDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var detailItem: QuestionItem

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        detailItem = arguments?.getParcelable("question")!!

        // 더미 댓글 리스트
        val commentList = listOf(
            CommentItem(
                username = "답변러1",
                content = "정말 공감되는 질문이에요. 저도 비슷한 생각을 했어요!",
                date = "2025/07/15 09:12",
                commentCount = 0, likeCount = 0, repostCount = 0, quoteCount = 0,
                isAnonymous = true
            ),
            CommentItem(
                username = "고민상담",
                content = "이런 질문이 많아졌으면 좋겠네요 :)",
                date = "2025/07/15 10:03",
                commentCount = 2, likeCount = 4, repostCount = 0, quoteCount = 0
            ),
            CommentItem(
                username = "익명1",
                content = "정말 좋은 질문이에요. 저도 오늘부터 실천해보려구요!".repeat(6),
                date = "2025/07/15 11:20",
                commentCount = 1, likeCount = 1, repostCount = 0, quoteCount = 0,
                isAnonymous = true
            ),
            CommentItem(
                username = "익명2",
                content = "생각할 거리를 주는 글이네요. 감사합니다.",
                date = "2025/07/15 12:45",
                commentCount = 0, likeCount = 3, repostCount = 0, quoteCount = 0,
                isAnonymous = true
            ),
            CommentItem(
                username = "힐링중",
                content = "마음이 따뜻해졌어요. 좋은 글 감사합니다 :)",
                date = "2025/07/15 13:30",
                commentCount = 0, likeCount = 5, repostCount = 0, quoteCount = 0
            ),
            CommentItem(
                username = "익명상담소",
                content = "저도 비슷한 고민 중이었는데 도움이 되네요.",
                date = "2025/07/15 14:12",
                commentCount = 3, likeCount = 2, repostCount = 0, quoteCount = 0,
                isAnonymous = true
            ),
            CommentItem(
                username = "라떼",
                content = "예전엔 이런 고민을 말도 못 했죠. 요즘은 참 좋아요.",
                date = "2025/07/15 15:02",
                commentCount = 0, likeCount = 1, repostCount = 0, quoteCount = 0
            ),
            CommentItem(
                username = "감성충전",
                content = "이 질문을 보고 제 일기를 다시 꺼냈어요.",
                date = "2025/07/15 16:25",
                commentCount = 0, likeCount = 0, repostCount = 0, quoteCount = 0,
                isAnonymous = true
            ),
            CommentItem(
                username = "공감왕",
                content = "100% 공감합니다. 함께 힘내요!",
                date = "2025/07/15 17:40",
                commentCount = 1, likeCount = 6, repostCount = 0, quoteCount = 0,
                isAnonymous = true
            ),
            CommentItem(
                username = "익명10",
                content = "나만 이런 고민하는 줄 알았는데 아니었네요!",
                date = "2025/07/15 18:55",
                commentCount = 0, likeCount = 2, repostCount = 0, quoteCount = 0
            )
        )

        // 어댑터 연결
        val adapter = QuestionDetailAdapter(detailItem, commentList)
        binding.rvQuestionDetail.layoutManager = LinearLayoutManager(requireContext())
        binding.rvQuestionDetail.adapter = adapter

        // 뒤로가기
        binding.btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(item: QuestionItem): QuestionDetailFragment {
            val fragment = QuestionDetailFragment()
            val bundle = Bundle()
            bundle.putParcelable("question", item)
            fragment.arguments = bundle
            return fragment
        }
    }
}