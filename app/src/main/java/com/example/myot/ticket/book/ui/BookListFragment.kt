package com.example.myot.ticket.book.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myot.R
import com.example.myot.databinding.FragmentBookListBinding
import com.example.myot.ticket.book.model.BookViewModel
import com.example.myot.ticket.book.ui.adapter.BookListAdapter

class BookListFragment : Fragment() {

    private var _binding: FragmentBookListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBookListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBookList()
        setBackButton()
    }

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

    // 티켓북 세팅
    private fun setBookList() {
        viewModel.setBookCover()

        binding.rvBookList.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = BookListAdapter(lifecycleOwner = viewLifecycleOwner, viewModel.bookCovers) { selectedBook ->
                val detailFragment = BookIndexFragment()

                // 데이터 전달
                val bundle = Bundle().apply {
                    putInt("musicalId", selectedBook.musical_id)
                }
                detailFragment.arguments = bundle

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_view, detailFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun setBackButton() {
        binding.btnBackBook.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}