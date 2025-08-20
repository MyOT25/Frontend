package com.example.myot.feed.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.feed.adapter.FeedbackAdapter
import com.example.myot.feed.model.FeedbackUserUi
import com.example.myot.retrofit2.TokenStore

class FeedbackListFragment : Fragment() {

    private val userList = mutableListOf<FeedbackUserUi>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FeedbackAdapter

    // ★ 아직 뷰/컨텍스트가 준비되기 전에 들어오는 리스트를 잠깐 담아둘 버퍼
    private var pendingUsers: List<FeedbackUserUi>? = null

    companion object {
        fun newInstance(): FeedbackListFragment = FeedbackListFragment()
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        val root = FrameLayout(requireContext())

        adapter = FeedbackAdapter(userList, requireActivity())
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ★ 버퍼에 쌓아둔 데이터가 있으면 이제 안전하게 반영
        pendingUsers?.let {
            applyUsers(it)
            pendingUsers = null
        }
    }

    fun submitUsers(newUsers: List<FeedbackUserUi>) {
        // 아직 프래그먼트가 attach되지 않았거나 adapter 준비 전이면 버퍼에 저장
        if (!isAdded || !this::adapter.isInitialized) {
            pendingUsers = newUsers
            return
        }
        applyUsers(newUsers)
    }

    private fun applyUsers(newUsers: List<FeedbackUserUi>) {
        // ★ 여기서는 requireContext() 안전 — onViewCreated 이후만 호출되도록 보장
        val myId = TokenStore.loadUserId(requireContext())
        val filtered = if (myId != null) {
            newUsers.filter { it.userId != myId } // 내 계정 제거
        } else newUsers

        userList.clear()
        userList.addAll(filtered)
        adapter.notifyDataSetChanged()
    }
}