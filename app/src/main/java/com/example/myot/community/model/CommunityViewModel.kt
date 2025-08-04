package com.example.myot.community.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myot.retrofit2.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CommunityViewModel: ViewModel() {
    private val _community = MutableLiveData<Community>()
    val community: LiveData<Community> get() = _community

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _communityMode = MutableStateFlow(CommunityMode.GUEST)
    val communityMode: StateFlow<CommunityMode> = _communityMode.asStateFlow()

    fun setCommunityMode(mode: CommunityMode) {
        _communityMode.value = mode
    }

    fun switchCommunityMode() {
        if (_communityMode.value == CommunityMode.MEMBER) {
            _communityMode.value = CommunityMode.GUEST
            community.value?.let { changeJoinLeave(it.communityId, "leave") }

        } else {
            _communityMode.value = CommunityMode.MEMBER
            community.value?.let { changeJoinLeave(it.communityId, "join") }
        }
    }

    fun fetchCommunity(id: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.communityService.getCommunityInfo(id)
                if (response.isSuccessful && response.body()?.success == true) {
                    _community.value = response.body()!!.community
                } else {
                    _error.value = "응답 실패: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "오류: ${e.message}"
            }
        }
    }

    fun fetchMultiProfiles(communityId: Int, userId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.communityService.getUserMultiProfiles(communityId, userId)
                if (response.isSuccessful && response.body()?.success == true) {

                } else {
                    _error.value = "응답 실패: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "오류: ${e.message}"
            }
        }
    }

    private fun changeJoinLeave(communityId: Int, action: String) {
        viewModelScope.launch {
            try {
                val request = JoinLeaveRequest(communityId, action)
                val response = RetrofitClient.communityService.setUserStatus(request)
                if (response.success) {
                    Log.d("JoinLeave", "성공")
                } else {
                    _error.value = "응답 실패: ${response.message}"
                }
            } catch (e: Exception) {
                _error.value = "오류: ${e.message}"
            }
        }
    }
}