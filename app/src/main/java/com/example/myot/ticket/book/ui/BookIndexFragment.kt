package com.example.myot.ticket.book.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myot.R
import com.example.myot.databinding.FragmentBookIndexBinding
import com.example.myot.ticket.book.model.BookIndex
import com.example.myot.ticket.book.model.BookViewModel
import com.example.myot.ticket.book.ui.adapter.BookIndexAdapter

class BookIndexFragment : Fragment() {

    private var _binding: FragmentBookIndexBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookViewModel by activityViewModels()

    private var musicalId: Int = -1

    companion object {
        private const val ARG_MUSICAL_ID = "musicalId"

        // Fragment 생성을 위한 factory method
        fun newInstance(musicalId: Int): BookIndexFragment {
            return BookIndexFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_MUSICAL_ID, musicalId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Arguments에서 musicalId 받기
        arguments?.let { bundle ->
            musicalId = bundle.getInt(ARG_MUSICAL_ID, -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBookIndexBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // musicalId가 유효한지 확인
        if (musicalId != -1) {
            setBookIndex(musicalId)
        } else {
            Toast.makeText(requireContext(), "유효하지 않은 musical입니다.", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }
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


    private fun setBookIndex(musicalId: Int) {

        viewModel.setBookIndex(musicalId)

        viewModel.bookIndexes.observe(viewLifecycleOwner) { bookIndexData ->
            bookIndexData?.let { data ->
                // 데이터가 로드되면 UI 업데이트
                binding.tvBookDetailTitle.text = "${data.series?.count() ?: 0}개의 티켓북"

                binding.rvSeasons.apply {
                    layoutManager = LinearLayoutManager(requireContext())
                    adapter = BookIndexAdapter(lifecycleOwner = viewLifecycleOwner, viewModel.bookIndexes) { selectedSeason ->
                        val detailFragment = BookDetailFragment()

                        // 데이터 전달
                        val bundle = Bundle().apply {
                            putString("title", viewModel.bookIndexes.value!!.title)
                            putString("season", selectedSeason.label)
                            putInt("musicalId", musicalId)
                        }
                        detailFragment.arguments = bundle

                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container_view, detailFragment)
                            .addToBackStack(null)
                            .commit()
                    }
                }
            } ?: run {
                // 데이터가 null인 경우 처리
                binding.tvBookDetailTitle.text = "티켓북을 불러오는 중..."
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
       _binding = null
    }
}