package com.example.myot.signup

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.myot.MainActivity
import com.example.myot.R
import com.example.myot.databinding.FragmentSignupStep5Binding
import com.example.myot.retrofit2.AuthStore
import com.example.myot.retrofit2.RetrofitClient
import com.example.myot.retrofit2.TokenStore
import com.example.myot.signup.data.CommunityDto
import com.example.myot.signup.data.JoinCommunityRequest
import com.example.myot.signup.data.SignupViewModel
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class SignupStep5Fragment : Fragment(), SignupStep {

    private var _binding: FragmentSignupStep5Binding? = null
    private val binding get() = _binding!!
    private val vm: SignupViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSignupStep5Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)

        // 커뮤니티 불러와서 칩 그리기
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                RetrofitClient.signupCommunityService.getCommunities()
            }.onSuccess { res ->
                val musical = res.communities.filter { it.type.equals("musical", ignoreCase = true) }
                val actor   = res.communities.filter { it.type.equals("actor",   ignoreCase = true) }

                renderChips(musical, isMusical = true)
                renderChips(actor,   isMusical = false)

                // 이전에 선택한 값 복원
                applyCheckedFromVm()
                updateDualBar()
            }.onFailure {
                // 실패 시에도 최소한 듀얼바 상태는 갱신
                updateDualBar()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateDualBar()
    }

    private fun renderChips(list: List<CommunityDto>, isMusical: Boolean) {
        val parent = if (isMusical) binding.chipsMusical else binding.chipsActor
        parent.removeAllViews()

        val inflater = LayoutInflater.from(requireContext())
        list.forEach { dto ->
            val chip = inflater.inflate(R.layout.item_community_chip, parent, false) as Chip
            chip.text = dto.communityName
            chip.tag  = dto.communityId   // ★ 선택 집합 갱신용 키
            chip.isCheckable = true
            chip.isCheckedIconVisible = false

            chip.setOnCheckedChangeListener { btn, isChecked ->
                val id = (btn.tag as? Long) ?: return@setOnCheckedChangeListener
                val cur = vm.selectedCommunities.value ?: emptySet()
                vm.selectedCommunities.value = if (isChecked) cur + id else cur - id
                updateDualBar()
            }
            parent.addView(chip)
        }
    }

    private fun applyCheckedFromVm() {
        val selected = vm.selectedCommunities.value ?: emptySet()
        fun apply(group: com.google.android.material.chip.ChipGroup) {
            group.children.forEach { view ->
                val chip = view as? Chip ?: return@forEach
                val id = (chip.tag as? Long) ?: return@forEach
                chip.isChecked = id in selected
            }
        }
        apply(binding.chipsMusical)
        apply(binding.chipsActor)
    }

    private fun updateDualBar() {
        val hasSelection = !(vm.selectedCommunities.value.isNullOrEmpty())
        (activity as? SignupFlowActivity)?.showDualNav(
            imageChosen = hasSelection,
            onLater     = { goMain() },
            onNext      = { joinAndGoMain() },
            nextText    = "시작하기"
        )
    }

    private fun goMain() {
        startActivity(Intent(requireContext(), MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        requireActivity().finish()
    }

    private fun joinAndGoMain() {
        val selected = vm.selectedCommunities.value.orEmpty()
        if (selected.isEmpty()) { goMain(); return }

        val userId = TokenStore.loadUserId(requireContext())
        if (userId == null) { goMain(); return }

        (activity as? SignupFlowActivity)?.apply {
            setDualNextEnabled(false)
            setLaterEnabled(false)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val bearer = AuthStore.bearerOrThrow()
            selected.forEach { cid ->
                runCatching {
                    RetrofitClient.signupCommunityService.joinCommunity(
                        bearer,
                        JoinCommunityRequest(
                            userId = userId,
                            communityId = cid,
                            action = "join",
                            profileType = "BASIC"
                        )
                    )
                }
            }
            goMain()
        }
    }

    override fun onNextClicked() { /* 상단 next 미사용 */ }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}