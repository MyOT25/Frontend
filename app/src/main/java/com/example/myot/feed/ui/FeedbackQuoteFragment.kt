package com.example.myot.feed.ui

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.feed.model.FeedItem
import com.example.myot.feed.adapter.FeedbackQuoteAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog

class FeedbackQuoteFragment(private val dialog: BottomSheetDialog) : Fragment() {

    private lateinit var quoteFeeds: List<FeedItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION") // API 32 이하 호환성 유지 시
        quoteFeeds = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelableArrayList(ARG_QUOTES, FeedItem::class.java) ?: emptyList()
        } else {
            @Suppress("DEPRECATION")
            requireArguments().getParcelableArrayList(ARG_QUOTES) ?: emptyList()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val recyclerView = RecyclerView(requireContext())
        recyclerView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
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