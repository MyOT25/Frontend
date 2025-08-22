package com.example.myot.ticket.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.MotionEvent
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myot.R
import com.example.myot.databinding.FragmentTicketBinding
import com.example.myot.ticket.book.ui.BookListFragment
import com.example.myot.ticket.model.TicketToday
import com.example.myot.ticket.ui.adapter.TicketTodayAdapter
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.myot.ticket.calendar.model.CalendarViewModel
import com.example.myot.ticket.calendar.ui.CalendarFragment
import com.example.myot.ticket.calendar.ui.TicketCalendarView
import com.example.myot.ticket.model.TicketViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class TicketFragment : Fragment() {

    private var _binding: FragmentTicketBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TicketTodayAdapter
    private var startX = 0f
    private var isSwiping = false
    private val startRevealPx = 200f
    private val swipeThreshold = 300f // px 기준 (이 이상 밀면 실행)

    private val viewModel: TicketViewModel by activityViewModels()
    private val vm: CalendarViewModel by viewModels({ requireActivity() })

    private lateinit var recordLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recordLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d("TicketFragment", "RESULT_OK -> refresh current month")

                // 최신 관극 카드 갱신
                viewModel.fetchLatestViewing()

                // 달력 ‘강제’ 갱신 (오늘/현재 월 기준)
                vm.refreshCurrentMonth()
            }
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTicketBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                top = maxOf(v.paddingTop, sys.top),     // 상태바 겹침 방지
                bottom = maxOf(v.paddingBottom, sys.bottom) // 하단 잘림 방지
            )
            insets
        }

        setRecentTicket()
        setTicketBook()

        binding.ivTicketbookCover.setImageResource(R.drawable.ig_community_poster_sample)

        binding.calendarMini.setMode(TicketCalendarView.Mode.MINI)
        binding.calendarMini.listener = object : TicketCalendarView.Listener {
            override fun onClickDay(date: java.time.LocalDate, hasRecord: Boolean) {}
            override fun onClickWholeMini() {
                val calendarFragment = CalendarFragment()

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_view, calendarFragment)
                    .addToBackStack("TicketFragment")
                    .commit()
            }
        }

        binding.layoutAddTicket.apply {
            visibility = View.GONE
            alpha = 0f
            translationX = 0f
            isClickable = false
            isFocusable = false
        }

        //setCalendar()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.ui.collectLatest { state ->
                    binding.tvTicketMonth.text =
                        state.yearMonth.format(DateTimeFormatter.ofPattern("M월"))

                    binding.calendarMini.apply {
                        setMonth(state.yearMonth.year, state.yearMonth.monthValue)
                        setRecords(state.records)
                        invalidate()            // ✅ 강제 redraw
                        // 필요시: requestLayout()
                    }
                }
            }
        }

        // 첫 로드
        vm.loadCurrentMonth()
    }

    // 오늘의 관극 세팅
    @SuppressLint("ClickableViewAccessibility")
    private fun setRecentTicket() {
        adapter = TicketTodayAdapter(emptyList())

        binding.rvToday.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvToday.adapter = adapter

        viewModel.fetchLatestViewing()

        viewModel.ticketToday.observe(viewLifecycleOwner) { tickets ->
            adapter = TicketTodayAdapter(tickets)
            binding.rvToday.adapter = adapter
        }

        binding.rvToday.setOnTouchListener { _, event ->
            val lm = binding.rvToday.layoutManager as? LinearLayoutManager ?: return@setOnTouchListener false
            val lastIndex = (binding.rvToday.adapter?.itemCount ?: 0) - 1
            val atEnd = lm.findLastCompletelyVisibleItemPosition() == lastIndex

            fun lastHolder() =
                binding.rvToday.findViewHolderForAdapterPosition(lastIndex)

            fun resetLastItem() {
                lastHolder()?.itemView?.animate()
                    ?.translationX(0f)
                    ?.setDuration(120L)
                    ?.start()
            }

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    isSwiping = false
                }

                MotionEvent.ACTION_MOVE -> {
                    if (!atEnd) {
                        // 마지막 카드 아니면 모두 리셋
                        binding.layoutAddTicket.visibility = View.GONE
                        resetLastItem()
                        return@setOnTouchListener false
                    }

                    val diffX = startX - event.x // +: 오른→왼
                    if (!isSwiping && kotlin.math.abs(diffX) > 8f) {
                        isSwiping = true
                        // 오버레이는 보이되 투명/원위치에서 시작
                        binding.layoutAddTicket.apply {
                            visibility = View.VISIBLE
                            alpha = 0f
                            translationX = 0f
                        }
                    }

                    if (isSwiping) {
                        // 진행률: 200px부터 보이기 시작, 320px에서 100%
                        val raw = (diffX - startRevealPx) / (swipeThreshold - startRevealPx)
                        val progress = raw.coerceIn(0f, 1f)

                        // ⬇️ 마지막 아이템 자체를 왼쪽으로 살짝 이동
                        val maxShift = (binding.rvToday.width * 0.12f).coerceAtLeast(24f) // 화면폭 12% 정도
                        lastHolder()?.itemView?.translationX = -progress * maxShift

                        // 오버레이는 점점 나타나게만(투명→불투명), 위치는 고정
                        binding.layoutAddTicket.alpha = progress

                        // 되돌리기(왼→오른)면 progress가 0으로 수렴하면서 아이템/오버레이가 자연히 복구
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (!isSwiping) return@setOnTouchListener false

                    val diffX = startX - event.x
                    if (diffX >= swipeThreshold) {
                        // 임계치 넘으면 전환
                        // 전환 중 겹침 방지를 위해 즉시 초기화
                        binding.layoutAddTicket.apply { alpha = 1f; translationX = 0f }
                        lastHolder()?.itemView?.translationX = 0f
                        recordLauncher.launch(Intent(requireContext(), RecordActivity::class.java))
                    } else {
                        // 취소: 아이템/오버레이 모두 부드럽게 원위치
                        resetLastItem()
                        binding.layoutAddTicket.animate()
                            .alpha(0f)
                            .translationX(0f)
                            .setDuration(120L)
                            .withEndAction { binding.layoutAddTicket.visibility = View.GONE }
                            .start()
                    }
                    isSwiping = false
                }
            }
            // false로 두어 RV의 스크롤/클릭도 동작
            false
        }
    }

    // 티켓북 세팅
    private fun setTicketBook() {
        binding.tvTicketbookMore.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, BookListFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onPause() {
        super.onPause()
        binding.layoutAddTicket.apply {
            clearAnimation()
            alpha = 0f
            translationX = 0f
            visibility = View.GONE
        }
        // 혹시 남아 있을지 모를 마지막 아이템 이동값도 복구
        val lastIndex = (binding.rvToday.adapter?.itemCount ?: 0) - 1
        (binding.rvToday.findViewHolderForAdapterPosition(lastIndex))?.itemView?.translationX = 0f

    }

    override fun onResume() {
        super.onResume()
        binding.layoutAddTicket.apply {
            clearAnimation()
            alpha = 0f
            translationX = 0f
            visibility = View.GONE
        }
        // 혹시 남아 있을지 모를 마지막 아이템 이동값도 복구
        val lastIndex = (binding.rvToday.adapter?.itemCount ?: 0) - 1
        (binding.rvToday.findViewHolderForAdapterPosition(lastIndex))?.itemView?.translationX = 0f

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
