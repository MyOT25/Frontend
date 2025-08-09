package com.example.myot.question.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myot.databinding.FragmentQuestionDetailBinding
import com.example.myot.feed.model.CommentItem
import com.example.myot.question.adapter.QuestionDetailAdapter
import com.example.myot.question.data.QuestionRepository
import com.example.myot.question.model.QuestionItem
import com.example.myot.retrofit2.RetrofitClient
import kotlinx.coroutines.launch

class QuestionDetailFragment : Fragment() {

    private var _binding: FragmentQuestionDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: QuestionDetailAdapter
    private val repository by lazy { QuestionRepository(RetrofitClient.questionService) }

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

        detailItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("question", QuestionItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("question")
        } ?: error("QuestionItem is missing")

        adapter = QuestionDetailAdapter(detailItem, imageUrls = emptyList(), comments = emptyList())
        binding.rvQuestionDetail.layoutManager = LinearLayoutManager(requireContext())
        binding.rvQuestionDetail.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            val res = repository.fetchQuestionDetail(detailItem.id)
            res.onSuccess { (header, images) ->
                adapter = QuestionDetailAdapter(header, images, comments = emptyList())
                binding.rvQuestionDetail.adapter = adapter
            }.onFailure {
                Toast.makeText(requireContext(), "상세 불러오기 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            var header = detailItem
            var images: List<String> = emptyList()
            val detailRes = repository.fetchQuestionDetail(detailItem.id)
            detailRes.onSuccess { (h, imgs) ->
                header = h
                images = imgs
                adapter = QuestionDetailAdapter(header, images, comments = emptyList())
                binding.rvQuestionDetail.adapter = adapter
            }.onFailure {
                Toast.makeText(requireContext(), "상세 불러오기 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }

            repository.fetchAnswerComments(detailItem.id)
                .onSuccess { comments ->
                    adapter = QuestionDetailAdapter(header, images, comments)
                    binding.rvQuestionDetail.adapter = adapter
                }
                .onFailure {
                    Toast.makeText(requireContext(), "답변 목록 불러오기 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        binding.btnBack.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }
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