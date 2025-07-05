package com.example.myot

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myot.databinding.FragmentCmReviewBinding

class CmReviewFragment : Fragment() {

    private var _binding: FragmentCmReviewBinding? = null
    private val binding get() = _binding!!

    private lateinit var filterAdapter: ReviewFilterAdapter
    private lateinit var reviewAdapter: CmReviewAdapter

    private var isExpanded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCmReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setExpandBtnClickListener()
        // 후기 필터링 더미 데이터
        val castingList = listOf(
            ReviewFilterItem("공주1", listOf("김김김", "박박박", "최최최")),
            ReviewFilterItem("공주2", listOf("이이이", "김김김")),
            ReviewFilterItem("공주3", listOf("정정정", "최최최", "하하하", "홍홍홍")),
        )
        filterAdapter = ReviewFilterAdapter(castingList)
        binding.rvFilter.apply {
            adapter = filterAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // 후기 더미 데이터
        val reviewList = listOf(
            ReviewItem("익명", 4.5, listOf("https://picsum.photos/300/200?random=1"), "세종문화회관 대극장", listOf("김김김", "이이이", "김김김", "홍홍홍"), "A열 12번","2024.09.18. 14:00", "텍스트텍스트텍스트텍스트텍스트텍스트텍스트텍스트텍스트텍스트텍스트텍스트텍스트텍스트텍스트텍스트텍스트텍스트텍스트텍스트텍스트", "2024.09.08", true, 70, false),
            ReviewItem("익명", 4.5, listOf("https://picsum.photos/300/200?random=1", "https://picsum.photos/300/200?random=1"), "링크아트센터드림 드림1관", listOf("김김김", "이이이"), "A열 12번","2024.09.18. 14:00", "텍스트텍스트텍스트텍스트텍스트텍스트텍스트", "2024.09.08", false, 12, true),
            ReviewItem("유저1", 4.5, listOf(), "블루스퀘어 신한카드홀", listOf("김김김", "이이이", "김김김"), "A열 12번", "2024.09.18. 14:00", "텍스트텍스트텍스트텍스트텍스트텍스트텍스트", "2024.09.08", true, 35, true),
        )

        reviewAdapter = CmReviewAdapter(this, reviewList)
        binding.rvReviews.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = reviewAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setExpandBtnClickListener() {

        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.constTotalReview)

        binding.tvCastExpanded.visibility = View.GONE

        binding.ivExpand.setOnClickListener {
            isExpanded = !isExpanded
            if (isExpanded) {
                binding.ivExpand.animate()
                    .rotation(180f)
                    .setDuration(200).start()

                binding.tvCastExpanded.visibility = View.VISIBLE

                constraintSet.clear(binding.ivExpand.id, ConstraintSet.LEFT)
                constraintSet.clear(binding.ivExpand.id, ConstraintSet.TOP)
                constraintSet.connect(binding.ivExpand.id, ConstraintSet.LEFT, binding.tvCastExpanded.id, ConstraintSet.RIGHT)
                constraintSet.connect(binding.ivExpand.id, ConstraintSet.BOTTOM, binding.tvCastExpanded.id, ConstraintSet.BOTTOM)
                constraintSet.applyTo(binding.constTotalReview)
            } else {
                binding.ivExpand.animate()
                    .rotation(0f)
                    .setDuration(200).start()

                constraintSet.clear(binding.ivExpand.id, ConstraintSet.LEFT)
                constraintSet.clear(binding.ivExpand.id, ConstraintSet.BOTTOM)
                constraintSet.connect(binding.ivExpand.id, ConstraintSet.LEFT, binding.tvCastShort.id, ConstraintSet.RIGHT)
                constraintSet.connect(binding.ivExpand.id, ConstraintSet.TOP, binding.tvCastShort.id, ConstraintSet.TOP)
                constraintSet.applyTo(binding.constTotalReview)

                binding.tvCastExpanded.visibility = View.GONE
            }
        }
    }
}