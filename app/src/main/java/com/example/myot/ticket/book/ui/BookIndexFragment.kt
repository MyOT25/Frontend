package com.example.myot.ticket.book.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myot.R
import com.example.myot.databinding.FragmentBookIndexBinding
import com.example.myot.ticket.book.model.BookIndex
import com.example.myot.ticket.book.ui.adapter.BookIndexAdapter

class BookIndexFragment : Fragment() {

    private var _binding: FragmentBookIndexBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBookIndexBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBookIndex()
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


    private fun setBookIndex() {
        val seasons = listOf(
            BookIndex(
                "2024-2025",
                "ht"
            ),
            BookIndex(
                "2026",
                "ht"
            )
        )

        binding.rvSeasons.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = BookIndexAdapter(seasons) { selectedSeason ->
                val detailFragment = BookDetailFragment()

                // 데이터 전달
                val bundle = Bundle().apply {
                    putString("season", selectedSeason.season)
                }
                detailFragment.arguments = bundle

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_view, detailFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
       _binding = null
    }
}