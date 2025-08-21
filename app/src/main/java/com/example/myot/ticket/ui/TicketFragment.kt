package com.example.myot.ticket.ui

import android.annotation.SuppressLint
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
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.myot.ticket.calendar.model.CalendarViewModel
import com.example.myot.ticket.calendar.ui.CalendarFragment
import com.example.myot.ticket.calendar.ui.TicketCalendarView
import com.example.myot.ticket.model.TicketViewModel
import kotlinx.coroutines.flow.collectLatest
import java.time.format.DateTimeFormatter

class TicketFragment : Fragment() {

    private var _binding: FragmentTicketBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TicketTodayAdapter
    private var startX = 0f
    private var isSwiping = false
    private val swipeThreshold = 150f // px 기준 (이 이상 밀면 실행)

    private val viewModel: TicketViewModel by activityViewModels()
    private val vm: CalendarViewModel by viewModels({ requireActivity() })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTicketBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecentTicket()
        setTicketBook()



        viewModel.recordSaved.observe(viewLifecycleOwner) { saved ->
            if (saved == true) {
                // TODO: UI 갱신 로직
            }
        }

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

        // 이번 달 데이터 표시
        vm.loadCurrentMonth()

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.ui.collectLatest { state ->
                binding.tvTicketMonth.text =
                    state.yearMonth.format(DateTimeFormatter.ofPattern("M월"))
                binding.calendarMini.setMonth(
                    state.yearMonth.year, state.yearMonth.monthValue
                )
                binding.calendarMini.setRecords(state.records)
            }
        }
    }

    // 오늘의 관극 세팅
    @SuppressLint("ClickableViewAccessibility")
    private fun setRecentTicket() {
        // 오늘의 관극 더미 데이터
        val tickets = listOf(
            TicketToday(
                "https://image_url",
                "킹키 부츠", "블루스퀘어",
                "25.06.06 ~ 25.09.10",
                "김호영, 이석훈",
                4.5, 5.0
            ),
            TicketToday(
                "https://image_url",
                "관부연락선", "링크아트센터드림 드림2관",
                "25.08.02 ~ 25.11.01",
                "선유하, 이지연",
                4.8, 4.1
            )
        )

        adapter = TicketTodayAdapter(tickets)

        binding.rvToday.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvToday.adapter = adapter

        binding.rvToday.setOnTouchListener { _, event ->
            val lm = binding.rvToday.layoutManager as LinearLayoutManager
            val lastVisible = lm.findLastCompletelyVisibleItemPosition()

            // 마지막 아이템에서만 동작
            if (lastVisible == adapter.itemCount - 1) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = event.x
                        isSwiping = false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val diffX = startX - event.x
                        if (diffX > 200) { // 오른쪽 → 왼쪽
                            isSwiping = true
                            val alpha = (diffX / swipeThreshold).coerceIn(0f, 1f)
                            binding.layoutAddTicket.apply {
                                visibility = View.VISIBLE
                                translationX = diffX * -0.5f
                                alpha.also { this.alpha = it }
                            }
                        }
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        if (isSwiping) {
                            val diffX = startX - event.x
                            if (diffX > swipeThreshold) {
                                startActivity(Intent(requireContext(), RecordActivity::class.java))
                            }
                            binding.layoutAddTicket.visibility = View.GONE
                        }
                    }
                }
            }
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



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
