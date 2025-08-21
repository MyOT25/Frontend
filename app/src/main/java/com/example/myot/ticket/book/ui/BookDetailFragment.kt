package com.example.myot.ticket.book.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.myot.R
import com.example.myot.databinding.FragmentBookDetailBinding
import com.example.myot.ticket.book.model.BookViewModel
import com.example.myot.ticket.book.model.RoleWithActors
import com.example.myot.ticket.book.model.SeatHighlightInfo
import com.example.myot.ticket.book.ui.adapter.TBRoleAdapter
import org.json.JSONObject
import org.json.JSONArray

class BookDetailFragment : Fragment() {

    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookViewModel by activityViewModels()

    private var musicalId: Int = -1
    private var title: String? = null
    private var season: String? = null

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_SEASON = "season"
        private const val ARG_MUSICAL_ID = "musicalId"

        // Fragment 생성을 위한 factory method
        fun newInstance(musicalId: Int): BookDetailFragment {
            return BookDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_SEASON, season)
                    putInt(ARG_MUSICAL_ID, musicalId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(ARG_TITLE)
            season = it.getString(ARG_SEASON)
            musicalId = it.getInt(ARG_MUSICAL_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.top_bar).visibility = View.GONE
        requireActivity().findViewById<View>(R.id.bottom_navigation_view).visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        requireActivity().findViewById<View>(R.id.top_bar).visibility = View.VISIBLE
        requireActivity().findViewById<View>(R.id.bottom_navigation_view).visibility = View.VISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // musicalId가 유효한지 확인
        if (musicalId != -1) {
            setActors()
            setSeatView()
        } else {
            Toast.makeText(requireContext(), "유효하지 않은 musical입니다.", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
    }

    private fun setSeatView() {
        viewModel.enableSeatAssetsMode(true)
        viewModel.fetchSeatData(requireContext(), musicalId)

        viewModel.seatData.observe(viewLifecycleOwner) { data ->
            // 하이라이트 좌석 (API 성공 시 서버 데이터, 실패 시 ViewModel에서 assets 폴백)
            binding.seatView.setHighlightedSeats(data.seats)

            // theaterId에 맞는 좌석맵 JSON 불러서 세팅
            val assetName = seatMapAssetFor(data.theaterId)
            runCatching {
                val json = requireContext().assets.open(assetName).bufferedReader().use { it.readText() }
                binding.seatView.setSeatData(org.json.JSONObject(json))
            }.onFailure {
                it.printStackTrace()
                // 좌석맵이 없거나 에러면 기본 맵으로 폴백
                val fallback = "sejong_big.json"
                kotlin.runCatching {
                    val json = requireContext().assets.open(fallback).bufferedReader().use { it.readText() }
                    binding.seatView.setSeatData(JSONObject(json))
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrBlank()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** theaterId → 좌석맵 에셋 파일명 매핑 */
    private fun seatMapAssetFor(theaterId: Int): String = when (theaterId) {
        1 -> "sejong_big.json"
        else -> "sejong_big.json" // 기본 폴백
    }


    @SuppressLint("SetTextI18n")
    private fun setActors() {
        binding.tvBookDetailSeason.text = season
        binding.tvBookDetailTitle.text = title

        viewModel.fetchTicketBookDetail(musicalId)

        viewModel.bookDetails.observe(viewLifecycleOwner) { data ->
            binding.tvBookDetailTitle.text = data.musical.name
            binding.tvBookDetailTotalCount.text = "총 ${data.musical.performanceCount}회 중"
            binding.tvBookDetailCount.text = "${data.musical.myViewingCount}회"

            // ✅ 역할별로 그룹핑
            val groupedRoles = data.castings
                .groupBy { it.role }
                .map { (role, castings) -> RoleWithActors(role, castings) }

            val adapter = TBRoleAdapter(groupedRoles) { position, progress ->
                // 인디케이터 제어
                val viewHolder = binding.rvCasting.findViewHolderForAdapterPosition(position)
                        as? TBRoleAdapter.TBRoleViewHolder ?: return@TBRoleAdapter

                val indicatorView = viewHolder.itemView.findViewById<View>(R.id.view_scroll_indicator)
                val containerWidth = viewHolder.itemView.width
                val indicatorWidth = indicatorView.width
                val maxTranslation = containerWidth - indicatorWidth

                indicatorView.translationX = progress * maxTranslation
            }

            binding.rvCasting.layoutManager = LinearLayoutManager(requireContext())
            binding.rvCasting.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}