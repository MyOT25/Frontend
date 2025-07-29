package com.example.myot.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.feed.adapter.FeedAdapter
import com.example.myot.feed.model.FeedItem

class ProfileTabFragment(private val feedItems: List<FeedItem>) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = requireContext()

        // 상단에 34dp 높이의 안보이는 View
        val topSpacer = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (34 * context.resources.displayMetrics.density).toInt() // 34dp
            )
            visibility = View.INVISIBLE
        }

        // RecyclerView 생성
        val recyclerView = RecyclerView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            layoutManager = LinearLayoutManager(context)
            adapter = FeedAdapter(feedItems)
        }

        // LinearLayout에 두 View 추가
        val containerLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            addView(topSpacer)
            addView(recyclerView)
        }

        return containerLayout
    }
}