package com.example.myot.home

import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myot.R
import com.example.myot.community.model.JoinLeaveRequest
import com.example.myot.databinding.ActivityAddCommunityBinding
import com.example.myot.retrofit2.AuthStore
import com.example.myot.retrofit2.RetrofitClient
import com.example.myot.retrofit2.TokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddCommunityActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCommunityBinding
    private lateinit var adapter: CommunitySuggestionAdapter           // 추천용
    private lateinit var searchAdapter: CommunitySuggestionAdapter     // 검색결과용

    private var allCommunities: List<CommunityTypeItem> = emptyList()
    private var currentQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCommunityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener { finish() }

        // 추천 리스트
        adapter = CommunitySuggestionAdapter(onClick = ::onCommunityClick)
        binding.rvCommunitySuggestions.apply {
            layoutManager = LinearLayoutManager(
                this@AddCommunityActivity, LinearLayoutManager.HORIZONTAL, false
            )
            adapter = this@AddCommunityActivity.adapter
        }

        // 검색 결과 리스트
        searchAdapter = CommunitySuggestionAdapter(onClick = ::onCommunityClick)
        binding.rvSearchResults.apply {
            layoutManager = LinearLayoutManager(
                this@AddCommunityActivity, LinearLayoutManager.HORIZONTAL, false
            )
            adapter = searchAdapter
        }

        initSearchBar()
        loadRecommended()
    }

    /** 버튼 검색 */
    private fun initSearchBar() {

        binding.ivSearch.setOnClickListener {
            val q = binding.etSearchInput.text?.toString()?.trim().orEmpty()
            currentQuery = q
            applySearch(q)
        }
    }

    /** 서버에서 actor+musical 불러와 로컬 캐시 */
    private fun loadRecommended() {
        val userId = TokenStore.loadUserId(this)
        if (userId == null) {
            showToast("로그인이 필요합니다.")
            return
        }
        val bearer = AuthStore.bearerOrThrow()
        val service = RetrofitClient.communityService

        lifecycleScope.launch {
            runCatching {
                val actor = async {
                    service.getCommunitiesByType(bearer, "actor", userId)
                        .body()?.communities ?: emptyList()
                }
                val musical = async {
                    service.getCommunitiesByType(bearer, "musical", userId)
                        .body()?.communities ?: emptyList()
                }
                (actor.await() + musical.await())
            }.onSuccess { list ->
                allCommunities = list.distinctBy { it.communityId }
                adapter.submit(allCommunities.sortedByDescending { it.memberCount })
                if (currentQuery.isNotBlank()) applySearch(currentQuery)
            }.onFailure { e ->
                showToast("추천 커뮤니티 불러오기 실패: ${e.message}")
            }
        }
    }

    /** 로컬 필터링 */
    private fun applySearch(query: String) {
        if (query.isBlank()) {
            binding.tvSearchTitle.visibility = android.view.View.GONE
            binding.emptySearchContainer.visibility = android.view.View.GONE
            binding.rvSearchResults.visibility = android.view.View.GONE
            return
        }

        binding.tvSearchTitle.text = "‘$query’ 검색 결과"
        binding.tvSearchTitle.visibility = android.view.View.VISIBLE

        val result = allCommunities
            .filter { it.communityName.contains(query, ignoreCase = true) }
            .sortedByDescending { it.memberCount }

        if (result.isEmpty()) {
            binding.emptySearchContainer.visibility = android.view.View.VISIBLE
            binding.rvSearchResults.visibility = android.view.View.GONE
        } else {
            binding.emptySearchContainer.visibility = android.view.View.GONE
            binding.rvSearchResults.visibility = android.view.View.VISIBLE
            searchAdapter.submit(result)
        }
    }

    /** 아이템 클릭 → 가입 → 토스트 → 목록/검색 갱신 */
    private fun onCommunityClick(item: CommunityTypeItem) {
        lifecycleScope.launch {
            val userId = TokenStore.loadUserId(this@AddCommunityActivity)
            if (userId == null) {
                showToast("로그인이 필요합니다.")
                return@launch
            }
            val bearer = AuthStore.bearerOrThrow()

            val req = JoinLeaveRequest(
                userId = userId,
                communityId = item.communityId.toInt(),
                action = "join",
                profileType = "BASIC",
                multi = null
            )

            runCatching {
                withContext(Dispatchers.IO) {
                    RetrofitClient.communityService.setUserStatus(bearer, req)
                }
            }.onSuccess { res ->
                if (res.success) {
                    showToast("${item.communityName}에 가입하셨습니다.")
                    loadRecommended()
                } else {
                    showToast(res.message.ifBlank { "가입에 실패했습니다." })
                }
            }.onFailure { e ->
                showToast("가입 실패: ${e.message}")
            }
        }
    }

    // ---- 공통 토스트 ----
    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
    private fun showToast(message: String) {
        val v = layoutInflater.inflate(R.layout.toast_simple, null)
        v.findViewById<TextView>(R.id.tv_toast).text = message
        Toast(this).apply {
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 64.dp)
            view = v
        }.show()
    }
}