package com.example.myot.community.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.myot.databinding.FragmentCommunityBinding
import com.google.android.material.tabs.TabLayoutMediator
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.myot.R
import com.example.myot.community.model.CommunityMode
import com.example.myot.community.model.CommunityViewModel
import com.example.myot.community.model.ProfileRequest
import com.example.myot.community.ui.adapter.CommunityTabAdapter
import com.example.myot.retrofit2.RetrofitClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CommunityFragment : Fragment() {

    private lateinit var itemType: String   // "actor" or "musical"

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CommunityViewModel by viewModels()   // 커뮤니티 가입 관리

    val myToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjExMiwibG9naW5JZCI6ImNtdGVzdCIsImlhdCI6MTc1NDkyMjMzOSwiZXhwIjoxNzU1NTI3MTM5fQ.I-Cx-ZdGygI5mGS10uOfBZjBRvpDyKAZpcsUkGKhzgI"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemType = arguments?.getString(ARG_ITEM_TYPE) ?: "musical"   // 기본값 "작품"으로 설정

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setCommunity()
        setCommunityMode()
        binding.tvChangeProfile.setOnClickListener {
            setMultiProfile(myToken, 201)
        }

        parentFragmentManager.setFragmentResultListener("multi_profile_result", viewLifecycleOwner) { _, bundle ->
            val nickname = bundle.getString("nickname")
            val bio = bundle.getString("bio")
            if (!nickname.isNullOrEmpty() && !bio.isNullOrEmpty()) {
                val profileReq = ProfileRequest(112, 201, nickname, "https://example.com/myimg.jpg", bio)
                postMultiProfile(profileReq)
                viewModel.switchCommunityMode()
                binding.layoutProfile.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        private const val ARG_ITEM_TYPE = "item_type"
        fun newInstance(type: String): CommunityFragment {
            val fragment = CommunityFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_ITEM_TYPE, type)
            }
            return fragment
        }
    }

    private fun setCommunity() {
        // TODO: 선택한 커뮤니티 정보 Home에서 넘겨받기
        //val communityId = intent.getIntExtra("communityId", 0)
        viewModel.fetchCommunity(201)

        viewModel.community.observe(viewLifecycleOwner) { community ->
            if (community != null) {
                itemType = community.type
                binding.tvCommunityName.text = community.groupName
//                Glide.with(this)
//                    .load(community.coverImage)
//                    .into(binding.ivCommunityCover)
                setTabs(itemType)
            } else {
                Log.w("CommunityFragment", "커뮤니티 데이터가 null입니다.")
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            Log.e("communityAPI", error)
            Toast.makeText(getActivity(), error , Toast.LENGTH_LONG).show()
        }
    }

    private fun setTabs(type: String) {
        val tabTitles = when (type) {
            "actor" -> listOf("하이라이트", "전체", "미디어", "메모리북")
            "musical" -> listOf("하이라이트", "전체", "후기", "미디어", "메모리북")
            else -> listOf("하이라이트", "전체", "미디어", "메모리북")
        }

        val adapter = CommunityTabAdapter(this, tabTitles)
        binding.vpTab.adapter = adapter

        TabLayoutMediator(binding.tlMenu, binding.vpTab) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    private fun setCommunityMode() {
        binding.ivCommunityJoin.setOnClickListener {
            // 버튼 클릭 시 BottomSheet 열기 or 탈퇴 처리
            val mode = viewModel.communityMode.value
            if (mode == CommunityMode.MEMBER) {
                viewModel.switchCommunityMode()
            } else {
                inputMultiProfile()
            }
        }

        // communityMode 상태 변경에 따른 UI 처리
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.communityMode.collect { mode ->
                    if (mode == CommunityMode.MEMBER) {
                        binding.ivCommunityJoin.setImageResource(R.drawable.btn_community_join_selected)
                        binding.layoutProfile.visibility = View.VISIBLE
                    } else {
                        binding.ivCommunityJoin.setImageResource(R.drawable.btn_community_join_unselected)
                        binding.layoutProfile.visibility = View.GONE
                    }
                }
            }
        }
    }

    // 멀티프로필 입력창 띄우기
    private fun inputMultiProfile() {
        val bottomSheet = MultiProfileBottomSheet(false, null)
        bottomSheet.show(parentFragmentManager, "multi_profile_result")
    }

    // 새로운 멀티프로필 등록
    private fun postMultiProfile(profileReq: ProfileRequest) {
        lifecycleScope.launch {
            try {
                // TODO: 토큰 받아오도록 만들기
                val response = RetrofitClient.communityService.setCommunityProfile(
                    token = "Bearer $myToken",
                    profileReq
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(requireContext(), "멀티 프로필이 등록되었습니다", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "등록 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "에러 발생: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setMultiProfile(token: String, communityId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.communityService.getMyMultiProfiles("Bearer ${token}", communityId)
                if (response.isSuccessful && response.body()?.success == true) {
                    if (response.body()?.profile != null) {
                        Log.d("MultiProfile", response.body()?.profile.toString())
                        val profileList = response.body()?.profile!!
                        val bottomSheet = MultiProfileBottomSheet(true, profileList)
                        bottomSheet.setOnProfileSelectedListener { profileId ->
                            val selected = profileList.find { it.id == profileId.toIntOrNull() }
                            selected?.let { viewModel.selectProfile(it) }
                        }
                        bottomSheet.setOnProfileDeletedListener { profile ->
                            val deleted = profileList.find { it.id == profile.id }
                            deleted?.let { viewModel.deleteProfile(it) }
                        }
                        bottomSheet.show(parentFragmentManager, bottomSheet.tag)
                    } else {
                        val bottomSheet = MultiProfileBottomSheet(false, null)
                        bottomSheet.show(parentFragmentManager, bottomSheet.tag)
                    }
                } else {
                    Log.d("MultiProfile", "응답 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.d("MultiProfile", "오류: ${e.message}")
            }
        }
    }
}