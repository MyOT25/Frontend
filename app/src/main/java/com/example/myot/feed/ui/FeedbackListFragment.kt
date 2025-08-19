package com.example.myot.feed.ui

import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.feed.adapter.FeedbackAdapter
import com.example.myot.feed.model.FeedbackUserUi

class FeedbackListFragment : Fragment() {

    private val userList = mutableListOf<FeedbackUserUi>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FeedbackAdapter

    companion object {
        fun newInstance(): FeedbackListFragment = FeedbackListFragment()
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        val root = FrameLayout(requireContext())
        adapter = FeedbackAdapter(userList)
        recyclerView = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@FeedbackListFragment.adapter
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        root.addView(recyclerView)
        return root
    }

    fun submitUsers(newUsers: List<FeedbackUserUi>) {
        userList.clear()
        userList.addAll(newUsers)
        if (this::adapter.isInitialized) adapter.notifyDataSetChanged()
    }
}