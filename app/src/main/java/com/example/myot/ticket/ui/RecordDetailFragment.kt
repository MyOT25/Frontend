package com.example.myot.ticket.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.example.myot.R
import com.example.myot.databinding.FragmentRecordDetailBinding
import com.example.myot.databinding.ItemRecordBadgeBinding
import com.example.myot.retrofit2.RetrofitClient
import com.example.myot.ticket.model.CastingInfo
import com.example.myot.ticket.model.Musical
import com.example.myot.ticket.model.RecordDetailResponse
import com.example.myot.ticket.model.RecordDetailViewModel
import com.example.myot.ticket.model.RecordUiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class RecordDetailFragment : Fragment() {

    private var _binding: FragmentRecordDetailBinding? = null
    private val binding get() = _binding!!

    private var postId: Int = -1
    private var musicalTitle: String = ""

    private var musical: Musical? = null

    private val viewModel: RecordDetailViewModel by viewModels()

    companion object {
        private const val ARG_POST_ID = "postId"
        private const val ARG_MUSICAL_TITLE = "musicalTitle"
        fun newInstance(postId: Int, musicalTitle: String) = RecordDetailFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_POST_ID, postId)
                putString(ARG_MUSICAL_TITLE, musicalTitle)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postId = requireArguments().getInt(ARG_POST_ID, -1)
        musicalTitle = requireArguments().getString(ARG_MUSICAL_TITLE, "")
        setHeader()

        // 관극 기록 로드
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.state.collectLatest { state ->
                when (state) {
                    is RecordUiState.Loading -> { /* 필요 시 로딩 처리 */ }
                    is RecordUiState.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                    is RecordUiState.Success -> bind(state.data)
                }
            }
        }
        viewModel.load(postId)
        binding.btnCalendarBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
    }

    fun setHeader() {
        viewLifecycleOwner.lifecycleScope.launch {
            // 네트워크 → 파싱 → 매핑까지
            val result: Musical? = try {
                val res = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    RetrofitClient.ticketService.searchMusical(musicalTitle)
                }

                val dto = if (res.isSuccessful) {
                    res.body()?.success?.data?.firstOrNull()
                } else {
                    android.util.Log.w(
                        "RecordDetail",
                        "searchMusical failed: ${res.code()} ${res.errorBody()?.string()}"
                    )
                    null
                }

                when (dto) {
                    is Musical -> dto                                        // 이미 Musical이면 그대로 사용
                    else -> null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

            if (!isAdded || _binding == null) return@launch

            // UI 갱신
            if (result != null) {
                musical = result
                binding.tvDetailInfoTitle.text   = result.name
                binding.tvDetailInfoTheater.text = result.theater
                binding.tvDetailInfoPeriod.text  = convertDate(result.period)
                binding.tvDetailInfoRatingAvg.text = (result.avgRating ?: 0.0).toString()

                Glide.with(binding.root) // View lifecycle에 묶기
                    .load(result.poster)
                    .placeholder(R.drawable.ig_poster_placeholder)
                    .into(binding.ivDetailInfoPoster)
            } else {
                // 데이터 없음 UI
                binding.tvDetailInfoTitle.text = ""
                binding.tvDetailInfoTheater.text = ""
                binding.tvDetailInfoPeriod.text = ""
                binding.tvDetailInfoRatingAvg.text = "0.0"

                Glide.with(binding.root)
                    .load(R.drawable.ig_poster_placeholder)
                    .into(binding.ivDetailInfoPoster)
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

    /** API 응답 바인딩 */
    private fun bind(v: RecordDetailResponse) {
        // 일시
        val zone = ZoneId.systemDefault()
        val dInstant = runCatching { Instant.parse(v.date) }.getOrNull()
        val tInstant = runCatching { Instant.parse(v.time) }.getOrNull()

        val yyyy = DateTimeFormatter.ofPattern("yyyy", Locale.KOREA)
        val m = DateTimeFormatter.ofPattern("M", Locale.KOREA)
        val d = DateTimeFormatter.ofPattern("d", Locale.KOREA)
        val hm = DateTimeFormatter.ofPattern("HH:mm", Locale.KOREA)

        setBadgeOptional(binding.badgeYear, dInstant?.atZone(zone)?.let { "${yyyy.format(it)}년" })
        setBadgeOptional(binding.badgeMonth, dInstant?.atZone(zone)?.let { "${m.format(it)}월" })
        setBadgeOptional(binding.badgeDay, dInstant?.atZone(zone)?.let { "${d.format(it)}일" })
        setBadgeOptional(binding.badgeTime, tInstant?.atZone(zone)?.let { hm.format(it) })

        // 좌석 (null/빈값은 자동 숨김)
        setBadgeOptional(binding.badgeFloor, v.seat?.floor?.let { "${it}층" })
        setBadgeOptional(binding.badgeZone, v.seat?.zone?.takeIf { it.isNotBlank() }?.let { "${it}구역" })
        setBadgeOptional(binding.badgeRow, v.seat?.rowNumber?.takeIf { !it.isNullOrBlank() }?.let { "${it}열" })
        setBadgeOptional(binding.badgeNumber, v.seat?.columnNumber?.takeIf { !it.isNullOrBlank() }?.let { "${it}번" })

        // 후기 & 별점
        binding.tvContent.text = v.content.orEmpty()
        binding.ratingBar.rating = v.rating

        // 출연
        renderCasting(v.casting.orEmpty())
    }

    /** include 된 item_badge의 텍스트뷰를 찾아 값/가시성 세팅 */
    private fun setBadgeOptional(includeView: ItemRecordBadgeBinding, text: String?) {
        val tv = includeView.badge
        if (text.isNullOrBlank()) {
            tv.isVisible = false
        } else {
            tv.text = text
            tv.isVisible = true
        }
    }

    /** 출연 섹션: 역할별로 묶어서 배지로 보여줌 */
    private fun renderCasting(list: List<CastingInfo>) {
        val container = binding.containerCasting
        container.removeAllViews()

        if (list.isEmpty()) {
            // 데이터가 없으면 아무 것도 추가하지 않음(디자인 상 공백)
            return
        }

        // 역할별 그룹화
        val grouped = list.groupBy { it.role.orEmpty() }

        val lpMatchWrap = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val lpWrapWrap = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )

        grouped.forEach { (role, actors) ->
            // 배역명
            val roleTv = TextView(requireContext()).apply {
                text = role.ifBlank { "배역" }
                textSize = 10f
                setTextColor(requireContext().getColor(R.color.gray3))
            }
            container.addView(roleTv, lpWrapWrap)

            // 배우 배지 리스트 (줄바꿈 위해 수직 컨테이너 + 수평 줄 단위로 구성)
            var line = newBadgeLine()
            container.addView(line, lpMatchWrap)

            actors.forEach { a ->
                val badge = newBadge(a.actorName.orEmpty())
                // 한 줄 폭이 너무 길어지면 새 줄 생성 (간단폭 기준)
                line.measure(0, 0)
                val currentCount = line.childCount
                if (currentCount >= 4) { // 대략 4개 정도면 줄바꿈
                    line = newBadgeLine()
                    container.addView(line, lpMatchWrap)
                }
                line.addView(badge, lpWrapWrap)
            }
        }
    }

    private fun newBadge(text: String): TextView =
        TextView(requireContext()).apply {
            this.text = text
            textSize = 12f
            setPadding(dp(12), dp(8), dp(12), dp(8))
            setTextColor(requireContext().getColor(R.color.record_input_text_selector))
            background = requireContext().getDrawable(R.drawable.bg_record_input_selector)
            setLineSpacing(0f, 1f)
        }

    private fun newBadgeLine(): LinearLayout =
        LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(6), 0, dp(6))
            dividerPadding = dp(4)
        }

    private fun dp(px: Int): Int =
        (px * resources.displayMetrics.density).toInt()

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
        super.onDestroyView()
        _binding = null
    }
}