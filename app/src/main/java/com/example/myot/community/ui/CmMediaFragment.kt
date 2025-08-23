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
import com.example.myot.databinding.FragmentCmMediaBinding
import com.example.myot.feed.adapter.FeedAdapter
import com.example.myot.feed.model.FeedItem
import com.example.myot.feed.model.toFeedItem
import com.example.myot.retrofit2.AuthStore
import com.example.myot.retrofit2.RetrofitClient
import kotlinx.coroutines.launch

class CmMediaFragment : Fragment() {

    private var _binding: FragmentCmMediaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CommunityViewModel by viewModels({ requireParentFragment() })

    private lateinit var adapter: FeedAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCmMediaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 멤버/버튼 노출 로직 유지
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.communityMode.collect { mode ->
                    binding.btnCmMediaEdit.visibility =
                        if (mode == CommunityMode.MEMBER) View.VISIBLE else View.GONE
                }
            }
        }

        // RecyclerView 초기화 (빈 리스트로 시작)
        adapter = FeedAdapter(emptyList())
        binding.rvMedias.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CmMediaFragment.adapter
        }

        // 커뮤니티 id 받아서 같은 API 호출 → 이미지가 있는 피드만 필터링
        viewModel.community.observe(viewLifecycleOwner) { community ->
            community?.let { fetchMediaFeeds(it.communityId.toLong()) } // Int → Long
        }
    }

    private fun fetchMediaFeeds(communityId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val token = AuthStore.bearerOrThrow() // "Bearer xxx" 형태
                val resp = RetrofitClient.communityService.getCommunityFeed(
                    token = token,
                    communityId = communityId,
                    cursor = null,
                    size = 20
                )
                if (resp.isSuccessful) {
                    val allItems: List<FeedItem> =
                        (resp.body()?.feed ?: emptyList()).map { it.toFeedItem() }

                    val mediaItems = allItems.filter { it.imageUrls.isNotEmpty() }

                    adapter = FeedAdapter(mediaItems)
                    binding.rvMedias.adapter = adapter
                } else {
                    // TODO: 에러 처리 (원하면 토스트/로그만 추가)
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