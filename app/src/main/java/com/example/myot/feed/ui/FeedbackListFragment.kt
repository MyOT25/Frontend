package com.example.myot.feed.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.feed.adapter.FeedbackAdapter

class FeedbackListFragment : Fragment() {

    private lateinit var userList: List<String>

    companion object {
        fun newInstance(data: List<String>): FeedbackListFragment {
            val fragment = FeedbackListFragment()
            val args = Bundle()
            args.putStringArrayList("data", ArrayList(data))
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userList = arguments?.getStringArrayList("data") ?: emptyList()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = FrameLayout(requireContext())
        val recyclerView = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = FeedbackAdapter(userList)
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        root.addView(recyclerView)

        return root
    }

}
