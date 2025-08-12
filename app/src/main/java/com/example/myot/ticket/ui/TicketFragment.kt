package com.example.myot.ticket.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myot.R
import com.example.myot.databinding.FragmentTicketBinding
import com.example.myot.ticket.book.ui.BookListFragment
import com.example.myot.ticket.model.TicketToday
import com.example.myot.ticket.ui.adapter.TicketTodayAdapter

class TicketFragment : Fragment() {

    private var _binding: FragmentTicketBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTicketBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTodayTicket()
        setTicketBook()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 오늘의 관극 세팅
    private fun setTodayTicket() {
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

        binding.rvToday.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = TicketTodayAdapter(tickets)
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
}
