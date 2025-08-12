package com.example.myot.community.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myot.community.model.Profile
import com.example.myot.community.ui.adapter.MultiProfileAdapter
import com.example.myot.databinding.FragmentCmMultiProfileListBinding
import com.example.myot.databinding.FragmentCmMultiProfileNewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MultiProfileBottomSheet(
    private val hasMultiProfile: Boolean,
    private val profileList: List<Profile>?
) : BottomSheetDialogFragment() {

    private var _bindingList: FragmentCmMultiProfileListBinding? = null
    private val bindingList get() = _bindingList!!

    private var _bindingNew: FragmentCmMultiProfileNewBinding? = null
    private val bindingNew get() = _bindingNew!!

    private var onProfileSelected: ((String) -> Unit)? = null
    private var onProfileDeleted: ((Profile) -> Unit)? = null

    fun setOnProfileSelectedListener(listener: (String) -> Unit) {
        onProfileSelected = listener
    }

    fun setOnProfileDeletedListener(listener: (Profile) -> Unit) {
        onProfileDeleted = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return if (hasMultiProfile) {
            _bindingList = FragmentCmMultiProfileListBinding.inflate(inflater, container, false)
            bindingList.root
        } else {
            _bindingNew = FragmentCmMultiProfileNewBinding.inflate(inflater, container, false)
            bindingNew.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (hasMultiProfile && !profileList.isNullOrEmpty()) {
            bindingList.rvMultiProfiles.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = MultiProfileAdapter(
                    profileList,
                    onProfileSelected = { profile ->
                        onProfileSelected?.invoke(profile.id.toString()) // 선택 시 콜백 실행
                        dismiss()
                    },
                    onProfileDeleted = { profile ->
                        onProfileDeleted?.invoke(profile)
                        dismiss()
                    }
                )
            }
        } else {
            bindingNew.btnJoin.setOnClickListener {
                val nickname = bindingNew.etNickname.text.toString().trim()
                val bio = bindingNew.etBio.text.toString().trim()

                if (nickname.isBlank()) {
                    Toast.makeText(requireContext(), "닉네임을 입력해주세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val result = Bundle().apply {
                    putString("nickname", nickname)
                    putString("bio", bio)
                }
                parentFragmentManager.setFragmentResult("multi_profile_result", result)
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingList = null
        _bindingNew = null
    }
}
