package com.example.myot.feed.ui

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.feed.adapter.FeedbackQuoteAdapter
import com.example.myot.feed.model.FeedItem
import com.google.android.material.bottomsheet.BottomSheetDialog

class FeedbackQuoteFragment(
    private val dialog: BottomSheetDialog,
    private val onFeedClick: () -> Unit
) : Fragment() {

    private var recyclerView: RecyclerView? = null
    private var pendingItems: List<FeedItem>? = null
    private var initialItems: List<FeedItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        initialItems = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelableArrayList(ARG_QUOTES, FeedItem::class.java) ?: emptyList()
        } else {
            requireArguments().getParcelableArrayList(ARG_QUOTES) ?: emptyList()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rv = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = FeedbackQuoteAdapter(initialItems, dialog) { onFeedClick() }
        }
        recyclerView = rv

        // 생성 전에 submit이 들어왔으면 여기서 반영
        pendingItems?.let {
            submit(it)
            pendingItems = null
        }
        return rv
    }

    // ★ 외부(어댑터)에서 호출
    fun submit(items: List<FeedItem>) {
        val rv = recyclerView
        if (rv == null) {
            pendingItems = items
            return
        }
        rv.adapter = FeedbackQuoteAdapter(items, dialog) { onFeedClick() } // 간단히 새 어댑터 교체
    }

    companion object {
        private const val ARG_QUOTES = "arg_quotes"
        fun newInstance(
            quotes: List<FeedItem>,
            dialog: BottomSheetDialog,
            onFeedClick: () -> Unit
        ): FeedbackQuoteFragment {
            return FeedbackQuoteFragment(dialog, onFeedClick).apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_QUOTES, ArrayList(quotes))
                }
            }
        }
    }
}