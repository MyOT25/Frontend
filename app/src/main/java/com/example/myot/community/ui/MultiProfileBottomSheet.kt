package com.example.myot.community.ui

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.myot.community.model.Profile
import com.example.myot.databinding.FragmentCmMultiProfileListBinding
import com.example.myot.databinding.FragmentCmMultiProfileNewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class MultiProfileBottomSheet(
    private val hasJoined: Boolean,
    private val profile: Profile?,
    private val profileType: String?
) : BottomSheetDialogFragment() {

    private var _bindingList: FragmentCmMultiProfileListBinding? = null
    private val bindingList get() = _bindingList!!

    private var _bindingNew: FragmentCmMultiProfileNewBinding? = null
    private val bindingNew get() = _bindingNew!!

    private var onDismissCallback: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return if (hasJoined && profile!=null && profileType != null) {
            _bindingList = FragmentCmMultiProfileListBinding.inflate(inflater, container, false)
            bindingList.root
        } else {
            _bindingNew = FragmentCmMultiProfileNewBinding.inflate(inflater, container, false)
            bindingNew.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (hasJoined && profile!=null && profileType != null) {
            when (profileType) {
                "BASIC" -> {
                    bindingList.layoutFullMultiProfile.visibility = View.GONE
                    bindingList.tvBasicProfileNickName.text = profile.nickname
                    bindingList.tvBasicProfileIntroduce.text = profile.bio
                    bindingList.layoutAddMultiProfile.setOnClickListener {

                        viewLifecycleOwner.lifecycleScope.launch {
                            kotlinx.coroutines.delay(200)

                            val newBottomSheet = MultiProfileBottomSheet(
                                hasJoined = true,
                                profile = null,
                                profileType = null
                            )

                            newBottomSheet.addOnDismissListener {
                                dismiss()
                            }
                            newBottomSheet.show(parentFragmentManager, newBottomSheet.tag)
                        }
                    }
                }
                "MULTI" -> {
                    bindingList.layoutAddMultiProfile.visibility = View.GONE
                    bindingList.ivBasicSelected.visibility = View.GONE
                    bindingList.tvMultiProfileNickName.text = profile.nickname
                    bindingList.tvMultiProfileIntroduce.text = profile.bio
                    setupSwipeToDelete(bindingList.layoutMultiProfile, bindingList.btnDelete)
                }
            }
            bindingList.btnLeave.setOnClickListener {
                val result = Bundle().apply {
                    putBoolean("isLeaving", true)
                }
                parentFragmentManager.setFragmentResult("community_leave", result)
                dismiss()
            }
        } else if (hasJoined && profile == null && profileType == null) {
            bindingNew.layoutDefaultJoinProfile.visibility = View.GONE
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
                    putString("type", "MULTI")
                }
                parentFragment?.let { dismiss() }
                parentFragmentManager.setFragmentResult("patch_multi_profile", result)
                dismiss()
            }
        } else {
            bindingNew.layoutDefaultJoinProfile.setOnClickListener{
                val result = Bundle().apply {
                    putString("nickname", null)
                    putString("bio", null)
                    putString("type", "BASIC")
                }
                parentFragmentManager.setFragmentResult("multi_profile_result", result)
                dismiss()
            }
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
                    putString("type", "MULTI")
                }
                parentFragmentManager.setFragmentResult("multi_profile_result", result)
                dismiss()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setupSwipeToDelete(contentLayout: View, deleteButton: View) {
        deleteButton.post {
            var downX = 0f
            val maxSwipe = deleteButton.width.toFloat() + 70f

            contentLayout.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = event.x
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = (event.x - downX).coerceIn(-maxSwipe, 0f)
                        contentLayout.translationX = deltaX
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (contentLayout.translationX < -maxSwipe / 2) {
                            contentLayout.animate().translationX(-maxSwipe).setDuration(200).start()
                        } else {
                            contentLayout.animate().translationX(0f).setDuration(200).start()
                        }
                        true
                    }
                    else -> false
                }
            }

            deleteButton.setOnClickListener {
                val result = Bundle().apply {
                    putString("nickname", null)
                    putString("bio", null)
                    putString("type", "BASIC")
                }
                parentFragmentManager.setFragmentResult("patch_multi_profile", result)
                dismiss()
            }
        }
    }

    fun addOnDismissListener(callback: () -> Unit) {
        onDismissCallback = callback
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissCallback?.invoke()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bindingList = null
        _bindingNew = null
    }
}
