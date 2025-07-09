package com.example.myot.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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
        val recyclerView = RecyclerView(requireContext())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = FeedbackAdapter(userList)
        return recyclerView
    }
}
