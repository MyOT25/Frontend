package com.example.myot.signup

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.myot.R
import com.example.myot.databinding.FragmentSignupStep3Binding
import com.example.myot.retrofit2.AuthRepository
import com.example.myot.retrofit2.AuthStore
import com.example.myot.retrofit2.RetrofitClient
import com.example.myot.retrofit2.SignupRequestDto
import com.example.myot.retrofit2.TokenStore
import com.example.myot.signup.data.SignupViewModel
import kotlinx.coroutines.launch

class SignupStep3Fragment : Fragment(), SignupStep {

    private var _binding: FragmentSignupStep3Binding? = null
    private val binding get() = _binding!!

    private val repo by lazy { AuthRepository(RetrofitClient.authService) }
    private val vm: SignupViewModel by activityViewModels()

    private val ID_REGEX = Regex("^[A-Za-z0-9._]{3,20}$")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSignupStep3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.loginId.value?.let { binding.etLoginId.setText(it) }

        binding.etLoginId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateUi()
            }
        })

        updateUi()
    }

    private fun isValidId(id: String) = id.isNotBlank() && ID_REGEX.matches(id)

    private fun updateUi() {
        val ctx = requireContext()
        val purple = ContextCompat.getColor(ctx, R.color.point_purple)
        val gray3  = ContextCompat.getColor(ctx, R.color.gray3)
        val blue   = ContextCompat.getColor(ctx, R.color.point_blue)

        val id = binding.etLoginId.text?.toString()?.trim().orEmpty()
        vm.loginId.value = id

        val valid = isValidId(id)

        binding.etLoginId.setTextColor(if (valid) purple else gray3)
        binding.tvAt.setTextColor(if (valid) purple else gray3)

        binding.tvLoginIdMsg.isVisible = valid
        if (valid) {
            binding.tvLoginIdMsg.setTextColor(blue)
            binding.tvLoginIdMsg.text = "사용 가능한 아이디입니다"
        }

        (activity as? SignupFlowActivity)?.setNextEnabled(valid)
    }

    override fun onNextClicked() {
        val name = vm.name.value.orEmpty()
        val email = vm.email.value.orEmpty()
        val pw = vm.password.value.orEmpty()
        val loginId = vm.loginId.value.orEmpty()
        val birthRaw = vm.birth.value.orEmpty()

        if (name.isBlank() || email.isBlank() || pw.isBlank() || loginId.isBlank() || birthRaw.isBlank()) return

        val nickname = name

        val birthDate = runCatching {
            val inFmt  = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.KOREA).apply { isLenient = false }
            val outFmt = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.KOREA)
            outFmt.format(inFmt.parse(birthRaw)!!)
        }.getOrElse {
            (activity as? SignupFlowActivity)?.setNextEnabled(false)
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repo.signup(
                SignupRequestDto(
                    username = name,
                    email = email,
                    loginId = loginId,
                    password = pw,
                    nickname = nickname,
                    birthDate = birthDate
                )
            ).onSuccess {
                repo.login(loginId, pw).onSuccess { login ->
                    AuthStore.accessToken = login.accessToken
                    TokenStore.saveAccessToken(requireContext(), login.accessToken)
                    TokenStore.saveUserId(requireContext(), login.userId)
                    (activity as? SignupFlowActivity)?.goNext(SignupStep4Fragment())
                }.onFailure {
                    (activity as? SignupFlowActivity)?.navigateBackToSignup()
                }
            }.onFailure {
                (activity as? SignupFlowActivity)?.navigateBackToSignup()
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}