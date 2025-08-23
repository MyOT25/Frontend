package com.example.myot.community.ui

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
import com.example.myot.databinding.FragmentCmHighlightBinding
import com.example.myot.feed.adapter.FeedAdapter
import com.example.myot.feed.model.FeedItem
import com.example.myot.feed.model.toFeedItem
import com.example.myot.retrofit2.AuthStore
import com.example.myot.retrofit2.RetrofitClient
import kotlinx.coroutines.launch

class CmHighlightFragment : Fragment() {

    private var _binding: FragmentCmHighlightBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CommunityViewModel by viewModels({ requireParentFragment() })

    private lateinit var adapter: FeedAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCmHighlightBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 기존 멤버/버튼 노출 로직 유지
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.communityMode.collect { mode ->
                    binding.btnCmHighlightEdit.visibility =
                        if (mode == CommunityMode.MEMBER) View.VISIBLE else View.GONE
                }
            }
        }

        // RecyclerView 초기화 (더미 제거)
        adapter = FeedAdapter(emptyList())
        binding.rvHighlights.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CmHighlightFragment.adapter
        }

        // 부모 ViewModel에서 커뮤니티 id 받아서 동일 API 호출
        viewModel.community.observe(viewLifecycleOwner) { community ->
            community?.let { fetchCommunityFeed(it.communityId.toLong()) } // Int -> Long
        }
    }

    private fun fetchCommunityFeed(communityId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val token = AuthStore.bearerOrThrow()   // "Bearer xxx" 형태여야 함
                val resp = RetrofitClient.communityService.getCommunityFeed(
                    token = token,
                    communityId = communityId,
                    cursor = null,
                    size = 20
                )
                if (resp.isSuccessful) {
                    val items: List<FeedItem> = (resp.body()?.feed ?: emptyList()).map { it.toFeedItem() }
                    adapter = FeedAdapter(items)
                    binding.rvHighlights.adapter = adapter
                } else {
                    // TODO: 에러 처리(토스트/로그) - UI 구조는 변경하지 않음
                }
            } catch (_: Exception) {
                // TODO: 네트워크 예외 처리
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}