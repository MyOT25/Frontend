package com.example.myot.ticket.calendar.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myot.databinding.FragmentCalendarBinding
import com.example.myot.ticket.calendar.model.CalendarEntry
import com.example.myot.ticket.calendar.model.CalendarViewModel
import kotlinx.coroutines.launch

class CalendarFragment : Fragment() {
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CalendarViewModel by viewModels()
    private lateinit var calendarAdapter: CalendarAdapter

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
        private var selectedDate: String? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCalendar()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupCalendar() {
        calendarAdapter = CalendarAdapter { date, dayData ->
            onDayClicked(date, dayData)
        }

        binding.recyclerViewCalendar.apply {
            layoutManager = GridLayoutManager(context, 7)
            adapter = calendarAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnPrevMonth.setOnClickListener {
            viewModel.navigateMonth(-1)
        }

        binding.btnNextMonth.setOnClickListener {
            viewModel.navigateMonth(1)
        }

        binding.btnCalendarBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun observeViewModel() {
        // 현재 월 표시
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentMonth.collect {
                    binding.tvMonth.text = viewModel.getMonthTitle()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.calendarDays.collect { days ->
                    calendarAdapter.updateDays(days)
                }
            }
        }

        // 로딩 상태
//        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
//            binding.progressBar.isVisible = isLoading
//        }

        // 에러 메시지
        viewModel.error.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                viewModel.clearErrorMessage()
            }
        }

        // 성공 메시지
        viewModel.successMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                viewModel.clearSuccessMessage()
            }
        }
    }

    private fun onDayClicked(date: String, dayData: List<CalendarEntry>) {
        if (date.isEmpty()) return

        val year = viewModel.currentYear.value ?: return
        val month = viewModel.currentMonth.value ?: return

        selectedDate = String.format("%d-%02d-%s", year, month + 1, date.padStart(2, '0'))

        // 이미지 선택 다이얼로그 표시 또는 기록 보기
        if (dayData.isEmpty()) {
            openImagePicker()
        } else {
            //showDayDetail(selectedDate!!, dayData)
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(Intent.createChooser(intent, "사진 선택"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedDate?.let { date ->
                val imageUris = mutableListOf<Uri>()

                data?.let {
                    if (it.clipData != null) {
                        // 여러 이미지 선택
                        val clipData = it.clipData!!
                        for (i in 0 until minOf(clipData.itemCount, 3)) {
                            imageUris.add(clipData.getItemAt(i).uri)
                        }
                    } else if (it.data != null) {
                        // 단일 이미지 선택
                        imageUris.add(it.data!!)
                    }
                }

                if (imageUris.isNotEmpty()) {
                    //showRecordDialog(date, imageUris)
                }
            }
        }
    }

//    private fun showRecordDialog(date: String, imageUris: List<Uri>) {
//        val dialog = RecordDialog.newInstance(date, imageUris) { memo ->
//            viewModel.saveRecord(date, imageUris, memo)
//        }
//        dialog.show(parentFragmentManager, "RecordDialog")
//    }
//
//    private fun showDayDetail(date: String, dayData: List<CalendarEntry>) {
//        val dialog = DayDetailDialog.newInstance(date, dayData) { action, record ->
//            when (action) {
//                DayDetailDialog.Action.UPDATE -> viewModel.updateRecord(record)
//                DayDetailDialog.Action.DELETE -> viewModel.deleteRecord(record)
//                DayDetailDialog.Action.ADD_NEW -> openImagePicker()
//            }
//        }
//        dialog.show(parentFragmentManager, "DayDetailDialog")
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}