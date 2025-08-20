package com.example.myot.question.ui

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myot.MainActivity
import com.example.myot.R
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

    private var headerItem: QuestionItem? = null
    private var headerImages: List<String> = emptyList()

    private lateinit var likeHandler: (Long, Boolean) -> Unit

    private var isDeleting = false

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

        (activity as? MainActivity)?.showCommentBar(
            scrollable = binding.rvQuestionDetail,
            hint = "댓글을 입력하세요",
            onSend = { text, isAnonymous ->
                viewLifecycleOwner.lifecycleScope.launch {
                    // 댓글 등록
                    repository.createComment(detailItem.id, text, isAnonymous)
                        .onSuccess {
                            // 내가 댓글 쓴 상태 반영
                            commentedSet.add(detailItem.id)

                            // 최신 댓글 목록 다시 로드
                            val updatedAnswers = repository.fetchAnswers(detailItem.id).getOrElse { emptyList() }

                            // 헤더의 댓글 수 +1
                            val newHeader = (headerItem ?: detailItem).copy(
                                commentCount = ((headerItem?.commentCount ?: detailItem.commentCount ?: 0) + 1)
                            )
                            headerItem = newHeader

                            adapter = QuestionDetailAdapter(
                                item = newHeader,
                                imageUrls = headerImages,
                                answers = updatedAnswers,
                                onQuestionLikeClick = likeHandler,
                                getQuestionLiked = { id -> likedSet.contains(id) },
                                getQuestionLikeCount = { id -> likeCountMap[id] ?: 0 },
                                onAnswerLikeClick = { aId, liked -> handleAnswerLikeClick(aId, liked) },
                                getAnswerLiked = { aId -> answerLikedSet.contains(aId) },
                                getAnswerLikeCount = { aId -> answerLikeCountMap[aId] ?: 0 },
                                getQuestionCommented = { id -> commentedSet.contains(id) },
                                onDeleteClick = { qid -> confirmAndDelete(qid) }
                            )
                            binding.rvQuestionDetail.adapter = adapter
                            adapter.notifyHeaderChanged()

                            binding.rvQuestionDetail.scrollToPosition(updatedAnswers.size)

                            (activity as? MainActivity)?.apply {
                                hideKeyboardAndClearFocus()
                                hideCommentBar()
                            }
                        }
                        .onFailure {
                            Toast.makeText(requireContext(), "댓글 등록 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            },
            allowAnonymous = true
        )

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
            getQuestionCommented = { id -> commentedSet.contains(id) },
            onDeleteClick = { qid -> confirmAndDelete(qid) }
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
                // 저장해두고 계속 재사용
                headerItem = header.copy(commentCount = detailItem.commentCount)
                headerImages = images

                // 1차 바인딩 (답글 비우고)
                adapter = QuestionDetailAdapter(
                    item = headerItem!!,
                    imageUrls = headerImages,
                    answers = emptyList(),
                    onQuestionLikeClick = likeHandler,
                    getQuestionLiked = { id -> likedSet.contains(id) },
                    getQuestionLikeCount = { id -> likeCountMap[id] ?: 0 },
                    onAnswerLikeClick = { aId, liked -> handleAnswerLikeClick(aId, liked) },
                    getAnswerLiked = { aId -> answerLikedSet.contains(aId) },
                    getAnswerLikeCount = { aId -> answerLikeCountMap[aId] ?: 0 },
                    getQuestionCommented = { id -> commentedSet.contains(id) },
                    onDeleteClick = { qid -> confirmAndDelete(qid) }
                )
                binding.rvQuestionDetail.adapter = adapter

                // 질문 좋아요 수 동기화
                likeCountMap[header.id] = repository.getLikeCountViaList(header.id)
                    .getOrElse { likeCountMap[header.id] ?: 0 }
                adapter.notifyHeaderChanged()

                // 답글 목록 로드
                val answers = repository.fetchAnswers(header.id).getOrElse { emptyList() }
                for (a in answers) {
                    val likedByMe = repository.getCommentLikedByMe(header.id, a.id).getOrElse { false }
                    if (likedByMe) answerLikedSet.add(a.id) else answerLikedSet.remove(a.id)

                    val c = repository.getCommentLikeCount(header.id, a.id).getOrElse { 0 }
                    answerLikeCountMap[a.id] = c
                }

                adapter = QuestionDetailAdapter(
                    item = headerItem!!,
                    imageUrls = headerImages,
                    answers = answers,
                    onQuestionLikeClick = likeHandler,
                    getQuestionLiked = { id -> likedSet.contains(id) },
                    getQuestionLikeCount = { id -> likeCountMap[id] ?: 0 },
                    onAnswerLikeClick = { aId, liked -> handleAnswerLikeClick(aId, liked) },
                    getAnswerLiked = { aId -> answerLikedSet.contains(aId) },
                    getAnswerLikeCount = { aId -> answerLikeCountMap[aId] ?: 0 },
                    getQuestionCommented = { id -> commentedSet.contains(id) },
                    onDeleteClick = { qid -> confirmAndDelete(qid) }
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

    private fun handleAnswerLikeClick(commentId: Long, isLikedNow: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            val hasToken = AuthStore.accessToken != null
            val currentlyLiked = answerLikedSet.contains(commentId)

            if (!hasToken) {
                return@launch
            }

            if (!currentlyLiked) {
                repository.likeComment(detailItem.id, commentId)
                    .onSuccess { answerLikedSet.add(commentId) }
                    .onFailure { Toast.makeText(requireContext(),"댓글 좋아요 실패: ${it.message}",Toast.LENGTH_SHORT).show(); return@launch }
            } else {
                repository.unlikeComment(detailItem.id, commentId)
                    .onSuccess { answerLikedSet.remove(commentId) }
                    .onFailure { Toast.makeText(requireContext(),"댓글 좋아요 취소 실패: ${it.message}",Toast.LENGTH_SHORT).show(); return@launch }
            }

            val count = repository.getCommentLikeCount(detailItem.id, commentId).getOrElse { 0 }
            answerLikeCountMap[commentId] = count
            adapter.notifyDataSetChanged()
        }
    }

    private fun confirmAndDelete(questionId: Long) {
        if (isDeleting) return
        isDeleting = true

        viewLifecycleOwner.lifecycleScope.launch {
            repository.deleteQuestion(questionId)
                .onSuccess {
                    (activity as? MainActivity)?.hideCommentBar()
                    (activity as? MainActivity)?.openQuestionTab()
                }
                .onFailure {
                    showToast("본인이 작성한 질문만 삭제할 수 있어요.")
                    isDeleting = false
                }
        }
    }

    override fun onDestroyView() {
        (activity as? MainActivity)?.hideCommentBar()
        super.onDestroyView()
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

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    private fun showToast(message: String) {
        val v = layoutInflater.inflate(R.layout.toast_simple, null)
        v.findViewById<TextView>(R.id.tv_toast).text = message

        Toast(requireContext()).apply {
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 64.dp)
            view = v
        }.show()
    }

}