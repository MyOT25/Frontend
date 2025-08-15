package com.example.myot.ticket.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.myot.R
import com.example.myot.databinding.FragmentRecordBinding
import com.example.myot.retrofit2.RetrofitClient
import com.example.myot.ticket.model.Musical
import com.example.myot.ticket.model.RecordRequest
import com.example.myot.ticket.model.SeatStructure
import com.example.myot.ticket.model.TicketViewModel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class RecordFragment : Fragment() {

    private var _binding: FragmentRecordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TicketViewModel by activityViewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TicketViewModel(RetrofitClient.ticketService) as T
            }
        }
    }

    private var selectedMusical: Musical? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.searchedMusical.observe(viewLifecycleOwner) { musical ->
            musical?.let {
                selectedMusical = it
                binding.tvRecordTitle.text = it.name
                binding.tvRecordTheater.text = it.theater
                binding.tvRecordPeriod.text = convertDate(it.period)
                binding.tvRecordRatingAvg.text = it.avgRating.toString()
                setEditTexts(it.seatStructure)
                Glide.with(this).load(it.poster).into(binding.ivRecordPoster)
            }
        }

        val seatJson = """
    {
        "locationId": 1,
        "row": "A",
        "column": 1,
        "seatType": "VIP"
    }
""".trimIndent()

        val seatRequestBody = seatJson
            .toRequestBody("application/json".toMediaType())

        val seatPart = MultipartBody.Part.createFormData(
            "seat",  // form-data 필드명
            null,
            seatRequestBody
        )

//        binding.btnRecord.setOnClickListener {
//            val date = getFormattedDate()
//            selectedMusical?.let { musical ->
//
//            }
//        }

        viewModel.recordSaved.observe(viewLifecycleOwner) { success ->
            if (success == true) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.RecordFragmentContainerView, TicketFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun convertDate(input: String): String {

        // 시작과 끝 날짜 분리
        val parts = input.split(" ~ ")

        // 서버에서 오는 날짜 포맷 지정
        val serverFormatter = DateTimeFormatter.ofPattern(
            "EEE MMM dd yyyy HH:mm:ss 'GMT'Z (zzzz)",
            Locale.ENGLISH
        )

        // 원하는 출력 포맷
        val outputFormatter = DateTimeFormatter.ofPattern("yy.MM.dd")

        val startDate = LocalDateTime.parse(parts[0], serverFormatter)
        val endDate = LocalDateTime.parse(parts[1], serverFormatter)

        val result = "${startDate.format(outputFormatter)} ~ ${endDate.format(outputFormatter)}"
        return result
    }

    private fun setEditTexts(structInfo: SeatStructure) {
        // 일시
        binding.inputYear.unitText.text = "년"
        binding.inputMonth.unitText.text = "월"
        binding.inputDay.unitText.text = "일"
        binding.inputTime.unitText.text = " "

        binding.inputYear.unitText.hint = "YYYY"
        binding.inputMonth.unitText.hint = "MM"
        binding.inputDay.unitText.hint = "DD"
        binding.inputTime.unitText.hint = "HH:MM"

        setupInputActivation(binding.inputYear.container, binding.inputYear.editText)
        setupInputActivation(binding.inputMonth.container, binding.inputMonth.editText)
        setupInputActivation(binding.inputDay.container, binding.inputDay.editText)
        setupInputActivation(binding.inputTime.container, binding.inputTime.editText)


        // 좌석
        binding.inputFloor.root.visibility = if (structInfo.hasFloor) View.VISIBLE else View.GONE
        binding.inputZone.root.visibility = if (structInfo.hasZone) View.VISIBLE else View.GONE
        binding.inputRow.root.visibility = if (structInfo.hasColumn) View.VISIBLE else View.GONE
        binding.inputNumber.root.visibility =
            if (structInfo.hasRowNumber) View.VISIBLE else View.GONE

        binding.inputFloor.unitText.hint = "F"
        binding.inputZone.unitText.hint = "Z"
        binding.inputRow.unitText.hint = "R"
        binding.inputNumber.unitText.hint = "N"

        binding.inputFloor.unitText.text = "층"
        binding.inputZone.unitText.text = "구역"
        binding.inputRow.unitText.text = "열"
        binding.inputNumber.unitText.text = "번"

        setupInputActivation(binding.inputFloor.container, binding.inputFloor.editText)
        setupInputActivation(binding.inputZone.container, binding.inputZone.editText)
        setupInputActivation(binding.inputRow.container, binding.inputRow.editText)
        setupInputActivation(binding.inputNumber.container, binding.inputNumber.editText)

        binding.etRecordContent.setOnFocusChangeListener { view, hasFocus ->
            val editText = view as EditText
            if (hasFocus) {
                // 포커스 생기면 높이 늘리기
                editText.minLines = 8
                editText.maxLines = 8
            } else {
                // 포커스 잃으면 다시 줄이기
                editText.minLines = 1
                editText.maxLines = 1
            }
        }
    }

    private fun setupInputActivation(container: ViewGroup, editText: EditText) {
        // 값 입력 여부에 따라 activated 상태 변경
        editText.addTextChangedListener {
            container.isActivated = !it.isNullOrEmpty()
        }
    }

    // 날짜 문자열 만들기
    private fun getFormattedDate(): String {
        val year = binding.inputYear.editText.text.toString().trim()
        val month = binding.inputMonth.editText.text.toString().trim()
        val day = binding.inputDay.editText.text.toString().trim()

        // 두 자리 보장: padStart(2, '0')
        val formattedMonth = month.padStart(2, '0')
        val formattedDay = day.padStart(2, '0')

        // 최종 문자열 조합 (YYYY-MM-DD)
        return "$year-$formattedMonth-$formattedDay"
    }

    // 좌석 문자열 만들기
    private fun getFormattedSeat(): String {
        val year = binding.inputYear.editText.text.toString().trim()
        val month = binding.inputMonth.editText.text.toString().trim()
        val day = binding.inputDay.editText.text.toString().trim()

        // 두 자리 보장: padStart(2, '0')
        val formattedMonth = month.padStart(2, '0')
        val formattedDay = day.padStart(2, '0')

        // 최종 문자열 조합 (YYYY-MM-DD)
        return "$year-$formattedMonth-$formattedDay"
    }

    private fun updateButtonState() {
        val allFilled = true // 실제 EditText 값 검사 로직 작성
        if (allFilled) {
            binding.btnRecord.setImageResource(R.drawable.ic_record_activate)
            binding.btnRecord.isEnabled = true
        } else {
            binding.btnRecord.setImageResource(R.drawable.ic_record_deactivate)
            binding.btnRecord.isEnabled = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}