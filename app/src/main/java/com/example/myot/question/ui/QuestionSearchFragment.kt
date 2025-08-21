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
        requireActivity().findViewById<View>(R.id.top_bar).visibility = View.GONE

        binding.etSearchInput.requestFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etSearchInput, InputMethodManager.SHOW_IMPLICIT)

        setupDummyHashtags()
        binding.tvCancel.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<View>(R.id.top_bar).visibility = View.VISIBLE
    }

    private fun setupDummyHashtags() {
        val dummyTags = listOf(
            "#뮤지컬추천", "#대학로", "#지킬앤하이드",
            "#캐스팅", "#웃는남자후기", "#세종문화회관", "#뮤지컬후기"
        )

        binding.flexHashtags.removeAllViews()

        dummyTags.forEach { tag ->
            val chip = layoutInflater.inflate(R.layout.item_question_hashtag, binding.flexHashtags, false) as TextView
            chip.text = tag
            binding.flexHashtags.addView(chip)
        }
    }
}