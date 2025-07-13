package com.example.myot

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myot.databinding.FragmentQuestionBinding
import com.example.myot.question.QuestionAdapter
import com.example.myot.question.QuestionItem

class QuestionFragment : Fragment() {

    private lateinit var binding: FragmentQuestionBinding
    private lateinit var adapter: QuestionAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentQuestionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 더미 데이터
        val dummyList = listOf(
            QuestionItem(
                title = "추천 뮤지컬 있을까요?",
                time = "3분 전",
                content = "요즘 스트레스 많아서 공연 보고 싶어요. 감동적인 뮤지컬 추천 좀 부탁드려요! #뮤지컬 #추천",
                likeCount = 42,
                commentCount = 9,
                imageUrl = "https://picsum.photos/300/200?random=1"
            ),
            QuestionItem(
                title = "레미제라블 처음 보면 어때요?",
                time = "10분 전",
                content = "뮤지컬 입문인데 레미제라블이 유명하던데 처음 보기에도 괜찮을까요? #레미제라블 #입문",
                likeCount = 31,
                commentCount = 5,
                imageUrl = "https://picsum.photos/300/200?random=2"
            ),
            QuestionItem(
                title = "뮤지컬 앨범 추천해주세요",
                time = "15분 전",
                content = "요즘 뮤지컬 넘버에 푹 빠졌어요. 음원으로 들을 만한 앨범 있을까요? #뮤지컬넘버 #음악추천",
                likeCount = 18,
                commentCount = 3
            ),
            QuestionItem(
                title = "인터미션 있는 뮤지컬 많나요?",
                time = "20분 전",
                content = "러닝타임 긴 뮤지컬은 중간 휴식 있던데 보통 다 그런가요? #공연정보",
                likeCount = 0,
                commentCount = 1
            ),
            QuestionItem(
                title = "소극장 뮤지컬도 재밌을까요?",
                time = "25분 전",
                content = "대극장 말고 대학로나 소극장 공연도 매력 있나요? 처음 가봐서 궁금해요. #소극장",
                likeCount = 7,
                commentCount = 0,
                imageUrl = "https://picsum.photos/300/200?random=5"
            ),
            QuestionItem(
                title = "좌석은 어디가 제일 좋을까요?",
                time = "30분 전",
                content = "뮤지컬 보러 가는데 시야 좋은 자리 추천 좀 해주세요. #좌석팁",
                likeCount = 0,
                commentCount = 0,
                imageUrl = "https://picsum.photos/300/200?random=6"
            ),
            QuestionItem(
                title = "돌아오는 뮤지컬 뭐 있을까요?",
                time = "35분 전",
                content = "코로나 이후 다시 올라오는 공연 중 추천할 만한 거 있을까요? #공연추천",
                likeCount = 15,
                commentCount = 2
            ),
            QuestionItem(
                title = "뮤지컬 자막 잘 보이나요?",
                time = "40분 전",
                content = "외국어 뮤지컬 처음 보는데 자막 위치랑 시야 괜찮은지 궁금해요. #자막",
                likeCount = 4,
                commentCount = 0,
                imageUrl = "https://picsum.photos/300/200?random=8"
            ),
            QuestionItem(
                title = "초등학생이랑 보기 좋은 뮤지컬?",
                time = "45분 전",
                content = "아이랑 같이 볼 수 있는 밝고 재미있는 뮤지컬 있을까요? #가족공연",
                likeCount = 22,
                commentCount = 6
            ),
            QuestionItem(
                title = "출연 배우 기준으로 골라본 적 있나요?",
                time = "50분 전",
                content = "스토리보다 배우 보고 예매해본 적 있으신가요? 저만 그런가요ㅎㅎ #출연진",
                likeCount = 0,
                commentCount = 4,
                imageUrl = "https://picsum.photos/300/200?random=10"
            )
        )

        adapter = QuestionAdapter(dummyList)
        binding.rvFeeds.adapter = adapter
        binding.rvFeeds.layoutManager = LinearLayoutManager(requireContext())

        binding.btnSortEdit.setOnClickListener { showSortPopup(it) }

        // 글쓰기 버튼 스크롤 시 투명도 처리
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val restoreFabAlphaRunnable = Runnable {
            binding.btnEdit.animate().alpha(1f).setDuration(200).start()
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