package com.example.myot.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myot.databinding.FragmentSignupStep5Binding

class SignupStep5Fragment : Fragment(), SignupStep {
    private var _binding: FragmentSignupStep5Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupStep5Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onNextClicked() { /* 상단 next 미사용 시 비워둠 */ }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
