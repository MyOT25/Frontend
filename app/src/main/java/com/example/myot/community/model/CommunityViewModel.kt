package com.example.myot.community.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myot.retrofit2.AuthStore
import com.example.myot.retrofit2.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val profileManager: ProfileManager
) : ViewModel() {
    private val _community = MutableLiveData<Community>()
    val community: LiveData<Community> get() = _community

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _communityMode = MutableStateFlow(CommunityMode.GUEST)
    val communityMode: StateFlow<CommunityMode> = _communityMode.asStateFlow()

    val token = AuthStore.bearerOrThrow()
    //val token =
    //    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjYsImxvZ2luSWQiOiJ0ZXN0dXNlcjA1IiwiaWF0IjoxNzU1MjQxNjY3LCJleHAiOjE3NTU4NDY0Njd9.Qa4rVwsUqqYLUNUptvJC4Z-HwA27zMNnlf7K8fwjQEA"

    fun setCommunityMode(mode: CommunityMode) {
        _communityMode.value = mode
    }

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile.asStateFlow()

    private val _profileType = MutableStateFlow<String?>(null)
    val profileType: StateFlow<String?> = _profileType

    fun switchCommunityMode() {
        if (_communityMode.value == CommunityMode.MEMBER) {
            _communityMode.value = CommunityMode.GUEST
        } else {
            _communityMode.value = CommunityMode.MEMBER
        }
    }

    fun fetchCommunity(type: String, CommunityId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.communityService.getCommunityInfo(
                    token,
                    type, CommunityId
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    _community.value = response.body()!!.community
                    if (response.body()!!.isJoined) {
                        setCommunityMode(CommunityMode.MEMBER)
                    } else {
                        setCommunityMode(CommunityMode.GUEST)
                    }
                } else {
                    _error.value = "여기서 응답 실패: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "오류: ${e.message}"
            }
        }
    }

    fun fetchProfile(communityId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.communityService.getMyCommunityProfile(
                    token,
                    communityId
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    _profileType.value = response.body()!!.profileType
                    _profile.value = response.body()!!.profile
                } else {
                    _error.value = "응답 실패: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "오류: ${e.message}"
            }
        }
    }

    fun JoinLeaveCommunity(
        userId: Long,
        communityId: Int,
        profileType: String,
        action: String,
        multi: Multi?
    ) {
        viewModelScope.launch {
            try {
                val request = JoinLeaveRequest(userId, communityId, action, profileType, multi)
                val response =
                    RetrofitClient.communityService.setUserStatus(token, request)
                if (response.success) {
                    Log.d("JoinLeave", "성공")
                    switchCommunityMode()
                } else {
                    _error.value = "응답 실패: ${response.message}"
                }

            } catch (e: Exception) {
                _error.value = "오류: ${e.message}"
            }
        }
    }

    fun patchProfile(changeTo: String, communityId: Int, multi: Multi?) {
        viewModelScope.launch {
            try {
                val request = PatchProfileRequest(changeTo, multi)
                val response =
                    RetrofitClient.communityService.patchMultiProfileType(token, communityId, request)
                if (response.success) {
                    Log.d("PatchProfile", "성공")
                    fetchProfile(communityId)
                } else {
                    _error.value = "응답 실패: ${response.message}"
                }

            } catch (e: Exception) {
                _error.value = "오류: ${e.message}"
            }
        }
    }

    fun deleteProfile(profile: Profile) {
        viewModelScope.launch {
            try {
                val response =
                    RetrofitClient.communityService.deleteMultiProfile("Bearer $token", profile.id)
                if (response.isSuccessful && response.body()?.success == true) {

                } else {
                    _error.value = "응답 실패: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "오류: ${e.message}"
            }
        }
    }
}