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
import android.widget.HorizontalScrollView
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
import com.example.myot.community.model.SeatPayload
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

private const val DEFAULT_BLOCK = 1
private const val DEFAULT_FLOOR = 1
private const val DEFAULT_ZONE = "A"
private const val DEFAULT_ROW_NUMBER = 1
private const val DEFAULT_SEAT_INDEX = 1

data class ActorTag(val id: Int, val locked: Boolean)

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

        viewModel.fetchLatestViewing()

        viewModel.ticketToday.observe(viewLifecycleOwner) { tickets ->
            val data = viewModel.ticketToday.value?.get(0)
            val actors = data?.cast
            binding.tvRecordInfoCasting.text = actors
        }

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
        viewModel.loadCast(musical.id)

        viewModel.roles.observe(viewLifecycleOwner) { roles ->
            binding.layoutCastingContainer.removeAllViews()

            val roleContainerLpFactory: () -> ViewGroup.LayoutParams =
                when (binding.layoutCastingContainer) {
                    is LinearLayout -> {
                        { LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ) }
                    }
                    is FlexboxLayout -> {
                        { FlexboxLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ) }
                    }
                    else -> {
                        { ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ) }
                    }
                }

            val chipGroupLp = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            // mandatory(importance<0) 수집 & 보장
            val mandatoryIds = roles.flatMap { it.actors }
                .filter { (it.importance ?: 0) < 0 }
                .map { it.actorId }
                .toSet()
            viewModel.setMandatoryActors(mandatoryIds)

            roles.forEach { role ->
                val roleContainer = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = roleContainerLpFactory()
                    setPadding(0, dp(5), 0, 0)
                }

                val roleTitle = TextView(requireContext()).apply {
                    text = role.role
                    setTextColor(ContextCompat.getColor(context, R.color.gray1))
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                    typeface = ResourcesCompat.getFont(context, R.font.roboto_semibold)
                    layoutParams = ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = dp(0) }
                }
                roleContainer.addView(roleTitle)

                val scroller = HorizontalScrollView(requireContext()).apply {
                    isHorizontalScrollBarEnabled = false
                    overScrollMode = View.OVER_SCROLL_NEVER
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }

                val chipGroup = ChipGroup(requireContext()).apply {
                    isSingleSelection = false
                    isSingleLine = true
                    chipSpacingHorizontal = dp(8)
                    chipSpacingVertical = 0
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }

                role.actors.forEach { actor ->
                    val locked = (actor.importance ?: 0) < 0
                    val chip = Chip(requireContext()).apply {
                        text = actor.name
                        isCheckable = false
                        tag = ActorTag(actor.actorId, locked)

                        val initiallySelected =
                            locked || (viewModel.selectedActors.value?.contains(actor.actorId) == true)
                        applyActorChipStyle(this, initiallySelected, locked)

                        if (locked) {
                            isClickable = false
                            isEnabled = true  // 색 빠짐 방지
                        } else {
                            setOnClickListener {
                                viewModel.selectActor(role.role, actor.actorId)
                            }
                        }
                    }
                    chipGroup.addView(chip)
                }

                scroller.addView(chipGroup)
                roleContainer.addView(scroller)
                binding.layoutCastingContainer.addView(roleContainer)
            }

            updateCastingSelectionUI()
            // 레이아웃 갱신 보장
            binding.layoutCastingContainer.requestLayout()
            binding.layoutCastingContainer.invalidate()
        }

        viewModel.selectedActors.observe(viewLifecycleOwner) { updateCastingSelectionUI() }
    }

    private fun applyActorChipStyle(chip: Chip, selected: Boolean, locked: Boolean = false) {
        val ctx = chip.context
        fun dpF(v: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, resources.displayMetrics)

        chip.chipCornerRadius = dpF(50f)
        chip.textSize = 12f
        chip.isCheckedIconVisible = false
        chip.isChipIconVisible = false
        chip.rippleColor = ColorStateList.valueOf(Color.TRANSPARENT)

        when {
            locked -> {
                chip.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(ctx, android.R.color.white))
                chip.chipStrokeColor = ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.point_purple))
                chip.chipStrokeWidth = dpF(2f)
                chip.setTextColor(ContextCompat.getColor(ctx, R.color.gray1))
                chip.typeface = Typeface.create(ResourcesCompat.getFont(ctx, R.font.roboto_semibold), Typeface.BOLD)
            }
            selected -> {
                chip.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(ctx, android.R.color.white))
                chip.chipStrokeColor = ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.point_purple))
                chip.chipStrokeWidth = dpF(2f)
                chip.setTextColor(ContextCompat.getColor(ctx, R.color.gray1))
                chip.typeface = Typeface.create(ResourcesCompat.getFont(ctx, R.font.roboto_semibold), Typeface.BOLD)
            }
            else -> {
                chip.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(ctx, android.R.color.white))
                chip.chipStrokeColor = ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.gray3))
                chip.chipStrokeWidth = dpF(1f)
                chip.setTextColor(ContextCompat.getColor(ctx, R.color.gray3))
                chip.typeface = ResourcesCompat.getFont(ctx, R.font.roboto_regular)
            }
        }
    }

    private fun ViewGroup.findFirstChipGroup(): ChipGroup? {
        for (i in 0 until childCount) {
            when (val v = getChildAt(i)) {
                is ChipGroup -> return v
                is ViewGroup -> v.findFirstChipGroup()?.let { return it } // 깊이 우선 탐색
            }
        }
        return null
    }


    private fun updateCastingSelectionUI() {
        val selectedIds = viewModel.selectedActors.value ?: emptyList()

        for (i in 0 until binding.layoutCastingContainer.childCount) {
            val roleContainer = binding.layoutCastingContainer.getChildAt(i) as? ViewGroup ?: continue
            val chipGroup = roleContainer.findFirstChipGroup() ?: continue

            for (j in 0 until chipGroup.childCount) {
                val chip = chipGroup.getChildAt(j) as? Chip ?: continue
                val tag = chip.tag as? ActorTag ?: continue
                val isSelected = tag.locked || selectedIds.contains(tag.id)
                applyActorChipStyle(chip, isSelected, tag.locked)
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
    private fun getFormattedSeat(structInfo: SeatStructure): String {
        val floorStr = binding.inputFloor.editText?.text?.toString()?.trim().orEmpty()
        val zoneStr = binding.inputZone.editText?.text?.toString()?.trim().orEmpty()
        val rowStr = binding.inputRow.editText?.text?.toString()?.trim().orEmpty()
        val seatIdxStr = binding.inputNumber.editText?.text?.toString()?.trim().orEmpty()

        val floor = if (structInfo.hasFloor)
            floorStr.toIntOrNull() ?: DEFAULT_FLOOR
        else DEFAULT_FLOOR

        val zone = if (structInfo.hasZone)
            zoneStr.ifBlank { DEFAULT_ZONE }
        else DEFAULT_ZONE

        // 주의: 네 레이아웃에서 inputRow는 structInfo.hasColumn을 따라가므로 그대로 매핑
        val rowNumber = if (structInfo.hasColumn)
            rowStr.toIntOrNull() ?: DEFAULT_ROW_NUMBER
        else DEFAULT_ROW_NUMBER

        val seatIndex = if (structInfo.hasRowNumber)
            seatIdxStr.toIntOrNull() ?: DEFAULT_SEAT_INDEX
        else DEFAULT_SEAT_INDEX

        val payload = SeatPayload(
            theaterId = selectedMusical.id,
            floor = floor,
            zone = zone,
            blockNumber = DEFAULT_BLOCK,
            rowNumber = rowNumber,
            seatIndex = seatIndex
        )

        // 안전하게 JSON 생성 (직접 문자열 조립 X)
        return Gson().toJson(payload)
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
            val seat = getFormattedSeat(selectedMusical.seatStructure)
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

    private fun dp(v: Int): Int =
        (resources.displayMetrics.density * v).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}