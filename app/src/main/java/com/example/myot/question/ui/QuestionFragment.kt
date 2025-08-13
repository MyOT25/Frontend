package com.example.myot.question.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myot.R
import com.example.myot.write.WriteQuestionActivity
import com.example.myot.databinding.FragmentQuestionBinding
import com.example.myot.question.adapter.QuestionAdapter
import com.example.myot.question.model.QuestionItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.jvm.java
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.myot.retrofit2.RetrofitClient
import com.example.myot.question.data.QuestionRepository
import com.example.myot.retrofit2.AuthStore

class QuestionFragment : Fragment() {

    private lateinit var binding: FragmentQuestionBinding
    private lateinit var adapter: QuestionAdapter
    private lateinit var repository: QuestionRepository
    private val questionList = mutableListOf<QuestionItem>()

    private val likedSet = mutableSetOf<Long>()
    private val likeCountMap = mutableMapOf<Long, Int>()
    private val commentedSet = mutableSetOf<Long>()

    private val writeResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult

            val id = data.getLongExtra("createdQuestionId", -System.currentTimeMillis())
            val title = data.getStringExtra("title") ?: "제목 없음"
            val content = data.getStringExtra("content") ?: ""
            val author = data.getStringExtra("authorName") ?: "나"
            val createdAt = data.getStringExtra("createdAt")
                ?: SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date())
            val tags = data.getStringArrayListExtra("tags")?.toList() ?: emptyList()
            val isAnonymous = data.getBooleanExtra("isAnonymous", false)

            val newItem = QuestionItem(
                id = id,
                title = title,
                content = content,
                username = author,
                profileImage = null,
                createdAt = createdAt,
                tags = tags,
                isAnonymous = isAnonymous,
                thumbnailUrl = null,
                likeCount = 0,
                commentCount = 0
            )

            questionList.add(0, newItem)
            adapter.notifyItemInserted(0)
            binding.rvFeeds.scrollToPosition(0)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentQuestionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = QuestionRepository(
            service = RetrofitClient.questionService,
            contentResolver = requireContext().contentResolver
        )

        adapter = QuestionAdapter(
            items = questionList,
            onItemClick = { item ->
                val detailFragment = QuestionDetailFragment.newInstance(item)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_view, detailFragment)
                    .addToBackStack(null)
                    .commit()
            },
            onLikeClick = { questionId, _  ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val currentlyLiked = likedSet.contains(questionId)
                    val hasToken = AuthStore.bearerOrNull() != null
                    if (!hasToken) return@launch

                    if (!currentlyLiked) {
                        val likeRes = repository.like(questionId)
                        likeRes.onSuccess {
                            likedSet.add(questionId)
                        }.onFailure {
                            Toast.makeText(requireContext(), "좋아요 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                    } else {
                        val unlikeRes = repository.unlike(questionId)
                        unlikeRes.onSuccess {
                            likedSet.remove(questionId)
                        }.onFailure {
                            Toast.makeText(requireContext(), "취소 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                    }

                    val newCount = repository.getLikeCountViaList(questionId).getOrElse {
                        questionList.firstOrNull { it.id == questionId }?.likeCount ?: 0
                    }
                    val idx = questionList.indexOfFirst { it.id == questionId }
                    if (idx != -1) {
                        val old = questionList[idx]
                        questionList[idx] = old.copy(likeCount = newCount)
                        adapter.notifyItemChanged(idx)
                    }
                }
            },
            getLiked = { id -> likedSet.contains(id) },
            getLikeCount = { id ->
                questionList.firstOrNull { it.id == id }?.likeCount ?: 0
            },
            getQuestionCommented = { id -> commentedSet.contains(id) }
        )
        binding.rvFeeds.adapter = adapter
        binding.rvFeeds.layoutManager = LinearLayoutManager(requireContext())

        (binding.rvFeeds.itemAnimator as? androidx.recyclerview.widget.SimpleItemAnimator)?.apply {
            supportsChangeAnimations = false
            changeDuration = 0
            moveDuration = 0
            addDuration = 0
            removeDuration = 0
        }

        loadQuestions(page = 1, limit = 20)

        binding.btnSortEdit.setOnClickListener { showSortPopup(it) }

        // 질문 검색
        binding.questionBar.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, QuestionSearchFragment())
                .addToBackStack(null)
                .commit()
        }

        // 글쓰기 버튼 스크롤 시 투명도 처리
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val restoreFabAlphaRunnable = Runnable {
            binding.btnEdit.animate().alpha(1f).setDuration(200).start()
        }

        // 질문 글쓰기
        binding.btnEdit.setOnClickListener {
            val intent = Intent(requireContext(), WriteQuestionActivity::class.java)
            writeResultLauncher.launch(intent)
        }

        binding.nestedScrollView.setOnScrollChangeListener { v, _, scrollY, _, oldScrollY ->
            val scrollView = v as NestedScrollView
            val view = scrollView.getChildAt(0)
            val diff = view.bottom - (scrollView.height + scrollY)

            if (diff <= 200) {
                // 스크롤이 끝까지 내려갔을 때
                handler.removeCallbacks(restoreFabAlphaRunnable)
                binding.btnEdit.animate().alpha(0f).setDuration(200).start()
            } else if (scrollY != oldScrollY) {
                // 스크롤 중인 경우
                binding.btnEdit.alpha = 0.3f
                handler.removeCallbacks(restoreFabAlphaRunnable)
                handler.postDelayed(restoreFabAlphaRunnable, 300)
            }
        }
    }

    private fun loadQuestions(page: Int = 1, limit: Int = 20) {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.fetchQuestions(page, limit).onSuccess { items ->
                questionList.clear()
                questionList.addAll(items)
                adapter.notifyDataSetChanged()

                val hasToken = AuthStore.bearerOrNull() != null
                if (hasToken) {
                    items.forEach { q ->
                        launch {
                            repository.fetchMyInteraction(q.id).onSuccess { me ->
                                if (me.hasLiked) likedSet.add(q.id) else likedSet.remove(q.id)
                                if (me.hasCommented) commentedSet.add(q.id) else commentedSet.remove(q.id)
                                val idx = questionList.indexOfFirst { it.id == q.id }
                                if (idx != -1) adapter.notifyItemChanged(idx)
                            }
                        }
                    }
                }
            }.onFailure {
                Toast.makeText(requireContext(), "불러오기 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun showSortPopup(anchor: View) {
        val context = anchor.context
        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(R.layout.menu_popup_sort, null)

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupWidth = popupView.measuredWidth

        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        val anchorX = location[0]
        val anchorY = location[1]

        val rootView = (anchor.rootView as? ViewGroup) ?: return
        val dimView = View(context).apply {
            setBackgroundColor(0x22000000.toInt())
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        rootView.addView(dimView)
        popupWindow.setOnDismissListener { rootView.removeView(dimView) }

        popupWindow.setBackgroundDrawable(null)
        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true
        popupWindow.elevation = 20f

        val offsetX = anchor.width - popupWidth - 10
        val offsetY = anchor.height + 10

        popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, anchorX + offsetX, anchorY + offsetY)

        // 정렬 항목 리스너
        popupView.findViewById<View>(R.id.btn_sort_popular).setOnClickListener {
            popupWindow.dismiss()
            Toast.makeText(context, "인기 순 클릭", Toast.LENGTH_SHORT).show()
        }
        popupView.findViewById<View>(R.id.btn_sort_recent).setOnClickListener {
            popupWindow.dismiss()
            Toast.makeText(context, "최신 순 클릭", Toast.LENGTH_SHORT).show()
        }
        popupView.findViewById<View>(R.id.btn_sort_old).setOnClickListener {
            popupWindow.dismiss()
            Toast.makeText(context, "오래된 순 클릭", Toast.LENGTH_SHORT).show()
        }
    }
}