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
import com.example.myot.MainActivity
import com.example.myot.databinding.FragmentQuestionDetailBinding
import com.example.myot.question.adapter.QuestionDetailAdapter
import com.example.myot.question.data.QuestionRepository
import com.example.myot.question.model.QuestionItem
import com.example.myot.retrofit2.AuthStore
import com.example.myot.retrofit2.RetrofitClient
import kotlinx.coroutines.launch

class QuestionDetailFragment : Fragment() {

    private var _binding: FragmentQuestionDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: QuestionDetailAdapter
    private lateinit var repository: QuestionRepository

    private val likedSet = mutableSetOf<Long>()
    private val likeCountMap = mutableMapOf<Long, Int>()

    private val answerLikedSet = mutableSetOf<Long>()
    private val answerLikeCountMap = mutableMapOf<Long, Int>()

    private val commentedSet = mutableSetOf<Long>()

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

        repository = QuestionRepository(
            service = RetrofitClient.questionService,
            contentResolver = requireContext().contentResolver
        )

        val canComment = AuthStore.accessToken != null
        binding.etComment.isEnabled = canComment
        binding.btnSendComment.isEnabled = canComment

        binding.btnSendComment.setOnClickListener {
            val text = binding.etComment.text?.toString()?.trim().orEmpty()
            if (text.isBlank()) return@setOnClickListener

            // TODO: 댓글 등록 POST /api/questions/{id}/comments 연동
            // 성공 시:
            // binding.etComment.text?.clear()
            // 목록 리프레시 or 하단에 추가
        }

        val likeHandler: (Long, Boolean) -> Unit = { questionId, _ ->
            viewLifecycleOwner.lifecycleScope.launch {
                val hasToken = AuthStore.accessToken != null
                val currentlyLiked = likedSet.contains(questionId)

                if (!hasToken) {
                    if (currentlyLiked) {
                        likedSet.remove(questionId)
                        likeCountMap[questionId] = (likeCountMap[questionId] ?: 1).coerceAtLeast(1) - 1
                    } else {
                        likedSet.add(questionId)
                        likeCountMap[questionId] = (likeCountMap[questionId] ?: 0) + 1
                    }
                    adapter.notifyDataSetChanged()
                    return@launch
                }

                if (!currentlyLiked) {
                    repository.like(questionId).onSuccess { likedSet.add(questionId) }
                        .onFailure { Toast.makeText(requireContext(),"좋아요 실패: ${it.message}",Toast.LENGTH_SHORT).show(); return@launch }
                } else {
                    repository.unlike(questionId).onSuccess { likedSet.remove(questionId) }
                        .onFailure { Toast.makeText(requireContext(),"취소 실패: ${it.message}",Toast.LENGTH_SHORT).show(); return@launch }
                }

                val count = repository.getLikeCountViaList(questionId).getOrElse { likeCountMap[questionId] ?: 0 }
                likeCountMap[questionId] = count
                adapter.notifyHeaderChanged()
            }
        }

        detailItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("question", QuestionItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("question")
        } ?: error("QuestionItem is missing")

        adapter = QuestionDetailAdapter(
            item = detailItem,
            imageUrls = emptyList(),
            answers = emptyList(),
            onQuestionLikeClick = likeHandler,
            getQuestionLiked = { id -> likedSet.contains(id) },
            getQuestionLikeCount = { id -> likeCountMap[id] ?: 0 },
            onAnswerLikeClick = { aId, liked -> handleAnswerLikeClick(aId, liked) },
            getAnswerLiked = { aId -> answerLikedSet.contains(aId) },
            getAnswerLikeCount = { aId -> answerLikeCountMap[aId] ?: 0 },
            getQuestionCommented = { id -> commentedSet.contains(id) }
        )
        binding.rvQuestionDetail.layoutManager = LinearLayoutManager(requireContext())
        binding.rvQuestionDetail.adapter = adapter

        (binding.rvQuestionDetail.itemAnimator as? androidx.recyclerview.widget.SimpleItemAnimator)?.apply {
            supportsChangeAnimations = false
            changeDuration = 0
            moveDuration = 0
            addDuration = 0
            removeDuration = 0
        }

        likeCountMap[detailItem.id] = detailItem.likeCount ?: 0
        adapter.notifyHeaderChanged()

        viewLifecycleOwner.lifecycleScope.launch {
            val detailRes = repository.fetchQuestionDetail(detailItem.id)
            detailRes.onSuccess { (header, images) ->
                // 헤더/이미지 적용
                adapter = QuestionDetailAdapter(
                    item = header.copy(commentCount = detailItem.commentCount),
                    imageUrls = images,
                    answers = emptyList(),
                    onQuestionLikeClick = likeHandler,
                    getQuestionLiked = { id -> likedSet.contains(id) },
                    getQuestionLikeCount = { id -> likeCountMap[id] ?: 0 },
                    onAnswerLikeClick = { aId, liked -> handleAnswerLikeClick(aId, liked) },
                    getAnswerLiked = { aId -> answerLikedSet.contains(aId) },
                    getAnswerLikeCount = { aId -> answerLikeCountMap[aId] ?: 0 },
                    getQuestionCommented = { id -> commentedSet.contains(id) }
                )
                binding.rvQuestionDetail.adapter = adapter

                // 질문 좋아요 수 동기화
                likeCountMap[header.id] = repository.getLikeCountViaList(header.id).getOrElse { likeCountMap[header.id] ?: 0 }
                adapter.notifyHeaderChanged()

                // 답변 목록 로드
                val answers = repository.fetchAnswers(header.id).getOrElse { emptyList() }

                // 각 답변의 좋아요 수 동기화 (간단하게 순차로)
                for (a in answers) {
                    val c = repository.getAnswerLikeCount(a.id).getOrElse { 0 }
                    answerLikeCountMap[a.id] = c
                }

                // 답변 적용
                adapter = QuestionDetailAdapter(
                    item = header.copy(commentCount = detailItem.commentCount),
                    imageUrls = images,
                    answers = answers,
                    onQuestionLikeClick = likeHandler,
                    getQuestionLiked = { id -> likedSet.contains(id) },
                    getQuestionLikeCount = { id -> likeCountMap[id] ?: 0 },
                    onAnswerLikeClick = { aId, liked -> handleAnswerLikeClick(aId, liked) },
                    getAnswerLiked = { aId -> answerLikedSet.contains(aId) },
                    getAnswerLikeCount = { aId -> answerLikeCountMap[aId] ?: 0 },
                    getQuestionCommented = { id -> commentedSet.contains(id) }
                )
                binding.rvQuestionDetail.adapter = adapter
            }.onFailure {
                Toast.makeText(requireContext(), "상세 불러오기 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repository.fetchMyInteraction(detailItem.id)
                .onSuccess { me ->
                    if (me.hasLiked) likedSet.add(detailItem.id) else likedSet.remove(detailItem.id)
                    if (me.hasCommented) commentedSet.add(detailItem.id) else commentedSet.remove(detailItem.id)
                    adapter.notifyHeaderChanged()
                }
                .onFailure {  }
        }

        binding.btnBack.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }
    }

    private fun handleAnswerLikeClick(answerId: Long, isLikedNow: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            val hasToken = AuthStore.accessToken != null
            val currentlyLiked = answerLikedSet.contains(answerId)

            if (!hasToken) {
                if (currentlyLiked) {
                    answerLikedSet.remove(answerId)
                    answerLikeCountMap[answerId] = (answerLikeCountMap[answerId] ?: 1).coerceAtLeast(1) - 1
                } else {
                    answerLikedSet.add(answerId)
                    answerLikeCountMap[answerId] = (answerLikeCountMap[answerId] ?: 0) + 1
                }
                adapter.notifyDataSetChanged()
                return@launch
            }

            if (!currentlyLiked) {
                repository.likeAnswer(answerId).onSuccess { answerLikedSet.add(answerId) }
                    .onFailure { Toast.makeText(requireContext(),"답변 좋아요 실패: ${it.message}",Toast.LENGTH_SHORT).show(); return@launch }
            } else {
                repository.unlikeAnswer(answerId).onSuccess { answerLikedSet.remove(answerId) }
                    .onFailure { Toast.makeText(requireContext(),"답변 좋아요 취소 실패: ${it.message}",Toast.LENGTH_SHORT).show(); return@launch }
            }

            val count = repository.getAnswerLikeCount(answerId).getOrElse { 0 }
            answerLikeCountMap[answerId] = count
            adapter.notifyDataSetChanged()
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