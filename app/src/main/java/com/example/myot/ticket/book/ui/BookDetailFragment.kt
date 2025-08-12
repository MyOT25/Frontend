package com.example.myot.ticket.book.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myot.R
import com.example.myot.databinding.FragmentBookDetailBinding
import com.example.myot.ticket.book.model.TBActor
import com.example.myot.ticket.book.model.TBRole
import com.example.myot.ticket.book.ui.adapter.TBRoleAdapter

class BookDetailFragment : Fragment() {

    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!

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
        val roles = listOf(
            TBRole(
                "홍련",
                listOf(
                    TBActor(
                        "김이후",
                        "www",
                        15,
                        60
                    ),
                    TBActor(
                        "한재아",
                        "www",
                        15,
                        60
                    ),
                    TBActor(
                        "홍나현",
                        "www",
                        15,
                        60
                    )
                )
            ),
            TBRole(
                "바리",
                listOf(
                    TBActor(
                        "김경민",
                        "www",
                        15,
                        60
                    ),
                    TBActor(
                        "이아름솔",
                        "www",
                        15,
                        60
                    ),
                    TBActor(
                        "이지연",
                        "www",
                        15,
                        60
                    )
                )
            ),
            TBRole(
                    "강림",
            listOf(
                TBActor(
                    "고상호",
                    "www",
                    15,
                    60
                ),
                TBActor(
                    "신창주",
                    "www",
                    15,
                    60
                ),
                TBActor(
                    "이종영",
                    "www",
                    15,
                    60
                )
            )
        )
        )

        val adapter = TBRoleAdapter(roles) { position, progress ->
            // ViewHolder에서 인디케이터 제어
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}