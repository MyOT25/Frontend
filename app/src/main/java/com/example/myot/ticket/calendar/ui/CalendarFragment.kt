package com.example.myot.ticket.calendar.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.myot.R
import com.example.myot.databinding.FragmentCalendarBinding
import com.example.myot.ticket.calendar.model.CalendarViewModel
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.collectLatest

class CalendarFragment : Fragment(R.layout.fragment_calendar) {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private val vm: CalendarViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentCalendarBinding.bind(view)

        binding.ticketCalendar.listener = object : TicketCalendarView.Listener {
            override fun onClickDay(date: java.time.LocalDate, hasRecord: Boolean) {
                if (hasRecord) {
                    // TODO: 상세 화면으로 이동
                    // findNavController().navigate(...)
                }
            }
        }

        binding.btnPrevMonth.setOnClickListener { vm.prevMonth() }
        binding.btnNextMonth.setOnClickListener { vm.nextMonth() }

        // 최초 로드
        vm.loadCurrentMonth()

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.ui.collectLatest { state ->
                binding.tvMonth.text =
                    state.yearMonth.format(DateTimeFormatter.ofPattern("M월"))

                binding.ticketCalendar.setMonth(
                    state.yearMonth.year, state.yearMonth.monthValue
                )
                binding.ticketCalendar.setRecords(state.records)

                // 필요 시 로딩/에러 UI 처리
                // binding.progress.isVisible = state.isLoading
            }
        }

        binding.btnCalendarBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.top_bar).visibility = View.GONE
        requireActivity().findViewById<View>(R.id.bottom_navigation_view).visibility = View.VISIBLE
    }

    override fun onPause() {
        super.onPause()
        requireActivity().findViewById<View>(R.id.top_bar).visibility = View.VISIBLE
        requireActivity().findViewById<View>(R.id.bottom_navigation_view).visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}