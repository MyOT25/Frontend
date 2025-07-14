package com.example.myot.question

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myot.databinding.FragmentQuestionDetailBinding

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

        binding.rvQuestionDetail.layoutManager = LinearLayoutManager(requireContext())

        val adapter = QuestionDetailAdapter(detailItem)
        binding.rvQuestionDetail.adapter = adapter

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