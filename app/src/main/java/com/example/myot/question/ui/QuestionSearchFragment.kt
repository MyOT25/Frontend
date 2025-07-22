package com.example.myot.question.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myot.R
import com.example.myot.databinding.FragmentQuestionSearchBinding
import com.example.myot.question.adapter.QuestionAdapter
import com.example.myot.question.model.QuestionItem

class QuestionSearchFragment : Fragment() {

    private var _binding: FragmentQuestionSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestionSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etSearchInput.requestFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etSearchInput, InputMethodManager.SHOW_IMPLICIT)

        setupDummyHashtags()
        binding.tvCancel.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        val dummyList = listOf(
            QuestionItem(
                title = "추천 뮤지컬 있을까요?",
                time = "2025/07/14 17:57",
                content = "요즘 스트레스 많아서 공연 보고 싶어요. 감동적인 뮤지컬 추천 좀 부탁드려요! #뮤지컬 #추천",
                likeCount = 42,
                commentCount = 9,
                imageUrls = listOf(
                    "https://picsum.photos/300/200?random=1"
                )
            ),
            QuestionItem(
                title = "레미제라블 처음 보면 어때요?",
                time = "2025/07/15 17:50",
                content = "뮤지컬 입문인데 레미제라블이 유명하던데 처음 보기에도 괜찮을까요? #레미제라블 #입문",
                likeCount = 31,
                commentCount = 5,
                imageUrls = listOf(
                    "https://picsum.photos/300/200?random=2",
                    "https://picsum.photos/300/200?random=12"
                )
            )
        )
        val adapter = QuestionAdapter(dummyList) { item ->
            val fragment = QuestionDetailFragment.newInstance(item)

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, fragment)
                .addToBackStack(null)
                .commit()
        }
        binding.rvSearchResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSearchResults.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupDummyHashtags() {
        val dummyTags = listOf(
            "#좋아요 많은 질문", "#댓글 많은", "#검색량 많은 키워드",
            "#좋아요 많은 질문 2222", "#검색량 많은 키워드22"
        )

        binding.flexHashtags.removeAllViews()

        dummyTags.forEach { tag ->
            val chip = layoutInflater.inflate(R.layout.item_question_hashtag, binding.flexHashtags, false) as TextView
            chip.text = tag
            binding.flexHashtags.addView(chip)
        }
    }
}