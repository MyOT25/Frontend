package com.example.myot.ticket.ui

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.myot.R
import com.example.myot.databinding.FragmentRecordBinding
import com.example.myot.ticket.model.Actor
import com.example.myot.ticket.model.Musical
import com.example.myot.ticket.model.Role
import com.example.myot.ticket.model.SeatStructure
import com.example.myot.ticket.model.TicketViewModel
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.gson.Gson
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class RecordFragment : Fragment() {

    private var _binding: FragmentRecordBinding? = null
    private val binding get() = _binding!!

    private lateinit var selectedMusical: Musical

    private val viewModel: TicketViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 기록할 뮤지컬 선정
        lifecycleScope.launchWhenStarted {
            viewModel.searchedMusical.collect { musical ->
                musical?.let {
                    selectedMusical = it
                    updateUI(it)
                }
            }
        }
    }

    // 화면 세팅
    private fun updateUI(musical: Musical) {
        binding.tvRecordInfoTitle.text = musical.name
        binding.tvRecordInfoTheater.text = musical.theater
        binding.tvRecordInfoPeriod.text = convertDate(musical.period)
        binding.tvRecordInfoRatingAvg.text = musical.avgRating.toString()
        Glide.with(this).load(musical.poster).into(binding.ivRecordInfoPoster)

        setEditTexts(musical.seatStructure)
        setCasting(musical)
        setupRecordButton()
    }

    // 서버에서 오는 뮤지컬 "기간" 날짜 변환
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

    // 입력 칸 설정
    private fun setEditTexts(structInfo: SeatStructure) {
        // 일시
        binding.inputYear.unitText.text = "년"
        binding.inputMonth.unitText.text = "월"
        binding.inputDay.unitText.text = "일"
        binding.inputTime.unitText.text = ""

        binding.inputYear.editText.hint = "YYYY"
        binding.inputMonth.editText.hint = "MM"
        binding.inputDay.editText.hint = "DD"
        binding.inputTime.editText.hint = "HH:MM"

        setupInputActivation(binding.inputYear.container, binding.inputYear.editText)
        setupInputActivation(binding.inputMonth.container, binding.inputMonth.editText)
        setupInputActivation(binding.inputDay.container, binding.inputDay.editText)
        setupInputActivation(binding.inputTime.container, binding.inputTime.editText)


        // 좌석
        binding.inputFloor.root.visibility = if (structInfo.hasFloor) View.VISIBLE else View.GONE
        binding.inputZone.root.visibility = if (structInfo.hasZone) View.VISIBLE else View.GONE
        binding.inputRow.root.visibility = if (structInfo.hasColumn) View.VISIBLE else View.GONE
        binding.inputNumber.root.visibility = if (structInfo.hasRowNumber) View.VISIBLE else View.GONE

        binding.inputFloor.editText.hint = "F"
        binding.inputZone.editText.hint = "Z"
        binding.inputRow.editText.hint = "R"
        binding.inputNumber.editText.hint = "N"

        binding.inputFloor.unitText.text = "층"
        binding.inputZone.unitText.text = "구역"
        binding.inputRow.unitText.text = "열"
        binding.inputNumber.unitText.text = "번"

        setupInputActivation(binding.inputFloor.container, binding.inputFloor.editText)
        setupInputActivation(binding.inputZone.container, binding.inputZone.editText)
        setupInputActivation(binding.inputRow.container, binding.inputRow.editText)
        setupInputActivation(binding.inputNumber.container, binding.inputNumber.editText)

        // 후기
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

    private fun setCasting(musical: Musical) {
        // 1) 캐스팅 로드
        viewModel.loadCast(musical.id)

        // 2) 역할/배우 UI 생성
        viewModel.roles.observe(viewLifecycleOwner) { roles ->
            binding.layoutCastingContainer.removeAllViews()

            roles.forEach { role ->
                // 배역 컨테이너(세로): [배역명 Text] + [ChipGroup]
                val roleContainer = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(0, 5, 0, 5)
                    layoutParams = when (parent) {
                        is LinearLayout -> LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        is com.google.android.flexbox.FlexboxLayout -> com.google.android.flexbox.FlexboxLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        else -> ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }
                }

                val roleTitle = TextView(requireContext()).apply {
                    text = role.role
                    textSize = 10f
                    setTypeface(ResourcesCompat.getFont(context, R.font.roboto_regular))
                    setPadding(0, 0, 0, 0)
                }
                roleContainer.addView(roleTitle)

                val chipGroup = ChipGroup(requireContext()).apply {
                    isSingleSelection = false // 선택은 직접 관리(역할당 1명은 ViewModel에서 강제)
                    isSingleLine = false
                    chipSpacingHorizontal = 24
                    chipSpacingVertical = 16
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }

                role.actors.forEach { actor ->
                    val chip = Chip(requireContext()).apply {
                        text = actor.name
                        isCheckable = false
                        isClickable = true
                        tag = actor.actorId

                        // 초기 스타일 (선택 상태 반영)
                        val selected = viewModel.selectedActors.value?.contains(actor.actorId) == true
                        applyActorChipStyle(this, selected)
                        setOnClickListener {
                            viewModel.selectActor(role.role, actor.actorId)
                        }
                    }
                    chipGroup.addView(chip)
                }

                roleContainer.addView(chipGroup)
                binding.layoutCastingContainer.addView(roleContainer)
            }

            // 역할 UI를 그린 직후 현재 선택 상태로 한 번 더 스타일 정리
            updateCastingSelectionUI()
        }

        // 3) 선택 목록이 바뀔 때마다 스타일 갱신
        viewModel.selectedActors.observe(viewLifecycleOwner) {
            updateCastingSelectionUI()
        }
    }

    // 배우 선택 버튼 디자인 적용
    private fun applyActorChipStyle(chip: Chip, selected: Boolean) {
        val ctx = chip.context
        val dm = resources.displayMetrics
        fun dp(value: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, dm)

        chip.chipCornerRadius = dp(50f)
        chip.textSize = 12f

        if (selected) {
            // 선택됨: 흰 배경 + 보라 테두리 + 검정 글씨
            chip.chipBackgroundColor = ColorStateList.valueOf(
                ContextCompat.getColor(ctx, android.R.color.white)
            )
            chip.chipStrokeColor = ColorStateList.valueOf(
                ContextCompat.getColor(ctx, R.color.point_purple)
            )
            chip.typeface = ResourcesCompat.getFont(context, R.font.roboto_semibold)
            chip.chipStrokeWidth = dp(2f)
            chip.setTextColor(ContextCompat.getColor(ctx, R.color.gray1))
            chip.typeface = Typeface.create(chip.typeface, Typeface.BOLD)
        } else {
            // 비선택: 흰 배경 + 회색 테두리 + 회색 글씨
            chip.chipBackgroundColor = ColorStateList.valueOf(
                ContextCompat.getColor(ctx, android.R.color.white)
            )
            chip.chipStrokeColor = ColorStateList.valueOf(
                ContextCompat.getColor(ctx, R.color.gray3)
            )
            chip.typeface = ResourcesCompat.getFont(context, R.font.roboto_regular)
            chip.chipStrokeWidth = dp(1f)
            chip.setTextColor(ContextCompat.getColor(ctx, R.color.gray3))
            chip.typeface = Typeface.create(chip.typeface, Typeface.NORMAL)
        }

        // 머티리얼 기본 효과가 간섭하지 않도록 추가 정리(있어도 되고 없어도 됨)
        chip.isCheckedIconVisible = false
        chip.isChipIconVisible = false
        chip.rippleColor = ColorStateList.valueOf(Color.TRANSPARENT)
    }


    // 선택 시 viewModel에서 selectedActors 변경
    private fun updateCastingSelectionUI() {
        val selectedIds = viewModel.selectedActors.value ?: mutableListOf()

        // layoutCastingContainer 안에는 여러 개의 roleContainer(LinearLayout)가 들어있음
        for (i in 0 until binding.layoutCastingContainer.childCount) {
            val roleContainer = binding.layoutCastingContainer.getChildAt(i) as? LinearLayout ?: continue
            // roleContainer의 0번째: TextView(배역명), 1번째: ChipGroup(배우들)
            val chipGroup = roleContainer.getChildAt(1) as? ChipGroup ?: continue

            for (j in 0 until chipGroup.childCount) {
                val chip = chipGroup.getChildAt(j) as? Chip ?: continue
                val actorId = chip.tag as? Int ?: continue
                val isSelected = selectedIds.contains(actorId)
                applyActorChipStyle(chip, isSelected)
            }
        }
    }

    // 값 입력 여부에 따라 activated 상태 변경
    private fun setupInputActivation(container: ViewGroup, editText: EditText) {
        editText.addTextChangedListener {
            container.isActivated = !it.isNullOrEmpty()
        }
    }

    private fun getFormattedDate(): String {
        val year = binding.inputYear.editText.text.toString().trim()
        val month = binding.inputMonth.editText.text.toString().trim()
        val day = binding.inputDay.editText.text.toString().trim()

        // 두 자리 보장: padStart(2, '0')
        val formattedMonth = month.padStart(2, '0')
        val formattedDay = day.padStart(2, '0')

        return "$year-$formattedMonth-$formattedDay"
    }

    // 좌석 문자열 만들기
    private fun getFormattedSeat(): String {
        val floor = binding.inputFloor.editText.text.toString().trim()
        val zone = binding.inputZone.editText.text.toString().trim()
        val rowNumber = binding.inputRow.editText.text.toString().trim()
        val seatIndex = binding.inputNumber.editText.text.toString().trim()

        val result = "{\"theaterId\":${selectedMusical.id},\"floor\":$floor,\"zone\":$zone,\"blockNumber\":1,\"rowNumber\":$rowNumber,\"seatIndex\":$seatIndex}"

        // 최종 문자열 조합 JSON
        return result
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

    private fun setupRecordButton() {
        val button = binding.btnRecord // 기록하기 버튼
        val ratingBar = binding.ratingbarRecord

        fun validateInputs(): Boolean {
            val year = binding.inputYear.editText.text.toString().trim()
            val month = binding.inputMonth.editText.text.toString().trim()
            val day = binding.inputDay.editText.text.toString().trim()
            val time = binding.inputTime.editText.text.toString().trim()

            val floor = binding.inputFloor.editText.text.toString().trim()
            val zone = binding.inputZone.editText.text.toString().trim()
            val row = binding.inputRow.editText.text.toString().trim()
            val seat = binding.inputNumber.editText.text.toString().trim()

            val isSeatValid =
                (!selectedMusical.seatStructure.hasFloor || floor.isNotEmpty()) &&
                        (!selectedMusical.seatStructure.hasZone || zone.isNotEmpty()) &&
                        (!selectedMusical.seatStructure.hasColumn || row.isNotEmpty()) &&
                        (!selectedMusical.seatStructure.hasRowNumber || seat.isNotEmpty())

            val content = binding.etRecordContent.text.toString().trim()
            val rating = ratingBar.rating

            // 모든 배역에 대해 한 명씩 배우 선택했는지 확인
            val allRolesSelected = viewModel.roles.value?.let { roles ->
                val selected = viewModel.selectedActors.value ?: mutableListOf()
                roles.all { role ->
                    role.actors.any { selected.contains(it.actorId) }
                }
            } ?: false

            return year.isNotEmpty() && month.isNotEmpty() && day.isNotEmpty() && time.isNotEmpty() &&
                    isSeatValid &&
                    content.isNotEmpty() &&
                    rating >= 0f &&
                    allRolesSelected
        }

        // 버튼 상태 갱신
        fun updateButtonState() {
            if (validateInputs()) {
                button.isEnabled = true
                button.setImageResource(R.drawable.ic_record_activate) // 활성화 이미지
            } else {
                button.isEnabled = false
                button.setImageResource(R.drawable.ic_record_deactivate) // 비활성화 이미지
            }
        }

        // 입력값 변경 감지 → 상태 갱신
        binding.inputYear.editText.addTextChangedListener { updateButtonState() }
        binding.inputMonth.editText.addTextChangedListener { updateButtonState() }
        binding.inputDay.editText.addTextChangedListener { updateButtonState() }
        binding.inputTime.editText.addTextChangedListener { updateButtonState() }

        binding.inputFloor.editText.addTextChangedListener { updateButtonState() }
        binding.inputZone.editText.addTextChangedListener { updateButtonState() }
        binding.inputRow.editText.addTextChangedListener { updateButtonState() }
        binding.inputNumber.editText.addTextChangedListener { updateButtonState() }

        ratingBar.setOnRatingBarChangeListener { _, _, _ -> updateButtonState() }

        viewModel.selectedActors.observe(viewLifecycleOwner) {
            updateButtonState()
        }
        viewModel.roles.observe(viewLifecycleOwner) {
            updateButtonState()
        }

        // 버튼 클릭 → 서버 업로드
        button.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener

            val musicalId = selectedMusical.id.toString()
            val watchDate = getFormattedDate()
            val watchTime = binding.inputTime.editText.text.toString().trim()
            val seat = getFormattedSeat()
            val content = binding.etRecordContent.text.toString()
            val rating = ratingBar.rating.toString()

            // 선택한 배우들 직렬화 (서버 요구: [1,2,3])
            val selectedIds = viewModel.selectedActors.value ?: mutableListOf()
            val castsJson = Gson().toJson(selectedIds)

             viewModel.uploadViewingRecord(
                musicalId = musicalId,
                watchDate = watchDate,
                watchTime = watchTime,
                seat = seat,
                casts = castsJson,
                content = content,
                rating = rating,
                imageFiles = null // 이미지 업로드 없으면 null
            )

            viewModel.recordSaved.observe(viewLifecycleOwner) { success ->
                if (success) {
                    // 업로드 성공 → RecordActivity 종료 + RESULT_OK 반환
                    requireActivity().setResult(Activity.RESULT_OK)
                    requireActivity().finish()
                } else {
                    // 실패 → 토스트 메시지 등 표시
                    Toast.makeText(requireContext(), "업로드에 실패했습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}