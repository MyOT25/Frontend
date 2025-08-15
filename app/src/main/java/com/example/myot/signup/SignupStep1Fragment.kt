// SignupStep1Fragment.kt
package com.example.myot.signup

import SignupStep2Fragment
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.myot.R
import com.example.myot.databinding.FragmentSignupStep1Binding
import com.example.myot.signup.data.SignupViewModel

class SignupStep1Fragment : Fragment(), SignupStep  {
    private var _binding: FragmentSignupStep1Binding? = null
    private val binding get() = _binding!!

    private val vm: SignupViewModel by activityViewModels()

    private val NAME_REGEX = Regex("^[가-힣A-Za-z0-9]+$")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupStep1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { updateUi() }
        }
        binding.etName.addTextChangedListener(watcher)
        binding.etEmail.addTextChangedListener(watcher)

        updateUi()
    }

    private fun updateUi() {
        val ctx = requireContext()
        val purple = ContextCompat.getColor(ctx, R.color.point_purple)
        val gray3  = ContextCompat.getColor(ctx, R.color.gray3)

        val name  = binding.etName.text?.toString()?.trim().orEmpty()
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()

        val nameValid  = isValidName(name)
        val emailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()

        binding.etName.setTextColor(if (nameValid) purple else gray3)
        binding.etEmail.setTextColor(if (emailValid) purple else gray3)

        val showEmailError = email.isNotBlank() && !emailValid
        binding.tvEmailError.isVisible = showEmailError

        (activity as? SignupFlowActivity)?.setNextEnabled(nameValid && emailValid)
    }

    override fun onNextClicked() {
        val name  = binding.etName.text?.toString()?.trim().orEmpty()
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        if (name.isEmpty() || email.isEmpty()) return

        vm.name.value = name
        vm.email.value = email

        (activity as? SignupFlowActivity)?.goNext(SignupStep2Fragment())
    }

    private fun isValidName(name: String): Boolean {
        return name.isNotBlank() && NAME_REGEX.matches(name)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}