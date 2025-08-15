package com.example.myot.ticket.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import com.example.myot.R
import com.example.myot.databinding.FragmentRecordBinding
import com.example.myot.ticket.model.SeatStructureInfo

class RecordFragment : Fragment() {

    private var _binding: FragmentRecordBinding? = null
    private val binding get() = _binding!!

    private val structInfo = SeatStructureInfo(hasFloor = true, hasZone = true, hasRow = false, hasNumber = true)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        binding.inputRow.root.visibility = if (structInfo.hasRow) View.VISIBLE else View.GONE
        binding.inputNumber.root.visibility = if (structInfo.hasNumber) View.VISIBLE else View.GONE

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}