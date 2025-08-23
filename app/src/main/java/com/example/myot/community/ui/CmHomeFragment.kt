package com.example.myot.community.ui

import android.icu.util.UniversalTimeScale.toLong
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myot.community.model.CommunityMode
import com.example.myot.community.model.CommunityViewModel
import com.example.myot.databinding.FragmentCmHomeBinding
import com.example.myot.feed.adapter.FeedAdapter
import com.example.myot.feed.model.FeedItem
import com.example.myot.feed.model.toFeedItem
import com.example.myot.retrofit2.AuthStore
import com.example.myot.retrofit2.CommunityService
import com.example.myot.retrofit2.RetrofitClient
import kotlinx.coroutines.launch

class CmHomeFragment : Fragment() {

    private var _binding: FragmentCmHomeBinding? = null
    private val binding get() = _binding!!

    // CommunityFragment 범위의 ViewModel 유지
    private val viewModel: CommunityViewModel by viewModels({ requireParentFragment() })

    private lateinit var adapter: FeedAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCmHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) 기존 버튼 노출 로직 유지
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.communityMode.collect { mode ->
                    binding.btnCmHomeEdit.visibility =
                        if (mode == CommunityMode.MEMBER) View.VISIBLE else View.GONE
                }
            }
        }

        // 2) 리사이클러뷰: 어댑터를 "빈 리스트"로 먼저 붙임 (더미 제거)
        adapter = FeedAdapter(emptyList())
        binding.rvHomes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CmHomeFragment.adapter
        }

        // 3) 커뮤니티 id 받아서 API 호출
        viewModel.community.observe(viewLifecycleOwner) { community ->
            if (community != null) {
                fetchCommunityFeed(community.communityId.toLong())
            }
        }
    }


    private fun fetchCommunityFeed(communityId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val service = RetrofitClient.communityService

                val token = AuthStore.bearerOrThrow() // 혹은 TokenStore.bearerOrThrow()
                val resp = service.getCommunityFeed(token, communityId)
                // 인증이 필요 없으면: val resp = service.getCommunityFeed(communityId)

                if (resp.isSuccessful) {
                    val body = resp.body()
                    val items = (body?.feed ?: emptyList()).map { it.toFeedItem() }
                    binding.rvHomes.adapter = FeedAdapter(items)
                } else {
                    // TODO: 에러 처리 (토스트/로그 등)
                }
            } catch (e: Exception) {
                // TODO: 네트워크 예외 처리
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}