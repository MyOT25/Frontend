package com.example.myot.signup

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myot.databinding.FragmentSignupStep4Binding
import com.example.myot.signup.data.SignupViewModel

class SignupStep4Fragment : Fragment(), SignupStep {
    private var _binding: FragmentSignupStep4Binding? = null
    private val binding get() = _binding!!
    private val vm: SignupViewModel by activityViewModels()

    private val picker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        vm.profileImageUri.value = uri
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSignupStep4Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 클릭: 이미지 선택
        binding.boxAdd.setOnClickListener { picker.launch("image/*") }
        binding.tvChange.setOnClickListener { picker.launch("image/*") }

        // 초기 표시
        applyState(vm.profileImageUri.value)
        updateDualBar()

        // 이미지 바뀌면 UI/듀얼바 갱신
        vm.profileImageUri.observe(viewLifecycleOwner) {
            applyState(it)
            updateDualBar()
        }
    }

    override fun onResume() {
        super.onResume()
        updateDualBar()
    }

    private fun updateDualBar() {
        val hasImage = vm.profileImageUri.value != null
        (activity as? SignupFlowActivity)?.showDualNav(
            imageChosen = hasImage,
            onLater = { goNext() },   // 이미지 없을 때만 활성
            onNext  = { goNext() }    // 이미지 있을 때만 활성
        )
    }

    private fun goNext() {
        // TODO: 커뮤니티 선택 프래그먼트로 이동
        // (activity as? SignupFlowActivity)?.goNext(SignupStep5CommunityFragment())
    }

    private fun applyState(uri: Uri?) {
        if (uri == null) {
            binding.boxAdd.visibility = View.VISIBLE
            binding.boxAvatar.visibility = View.GONE
        } else {
            binding.boxAdd.visibility = View.GONE
            binding.boxAvatar.visibility = View.VISIBLE
            binding.ivAvatar.setImageURI(uri)
        }
    }

    override fun onNextClicked() { /* 상단 next 미사용 */ }

    override fun onDestroyView() { _binding = null; super.onDestroyView() }
}