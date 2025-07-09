package com.example.myot.feed

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog

class FeedbackQuoteFragment(private val dialog: BottomSheetDialog) : Fragment() {

    private lateinit var quoteFeeds: List<FeedItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        quoteFeeds = requireArguments().getParcelableArrayList(ARG_QUOTES)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val recyclerView = RecyclerView(requireContext())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = FeedbackQuoteAdapter(quoteFeeds, dialog)
        return recyclerView
    }

    companion object {
        private const val ARG_QUOTES = "arg_quotes"

        fun newInstance(quotes: List<FeedItem>, dialog: BottomSheetDialog): FeedbackQuoteFragment {
            val fragment = FeedbackQuoteFragment(dialog)
            val bundle = Bundle().apply {
                putParcelableArrayList(ARG_QUOTES, ArrayList(quotes))
            }
            fragment.arguments = bundle
            return fragment
        }
    }
}