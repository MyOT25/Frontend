package com.example.myot.community.model

import android.content.Context
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor (
    private val profileManager: ProfileManager
) : ViewModel() {
    private val _community = MutableLiveData<Community>()
    val community: LiveData<Community> get() = _community

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _communityMode = MutableStateFlow(CommunityMode.GUEST)
    val communityMode: StateFlow<CommunityMode> = _communityMode.asStateFlow()

    val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjExMiwibG9naW5JZCI6ImNtdGVzdCIsImlhdCI6MTc1NDkyMjMzOSwiZXhwIjoxNzU1NTI3MTM5fQ.I-Cx-ZdGygI5mGS10uOfBZjBRvpDyKAZpcsUkGKhzgI"
    fun setCommunityMode(mode: CommunityMode) {
        _communityMode.value = mode
    }

    private val _profiles = MutableStateFlow<List<Profile>>(emptyList())
    val profiles: StateFlow<List<Profile>> = _profiles

    private val _selectedProfile = MutableStateFlow<Profile?>(null)
    val selectedProfile: StateFlow<Profile?> = _selectedProfile

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
                val response = RetrofitClient.communityService.getCommunityInfo(
                    token = "Bearer $token",
                    id
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

    fun fetchMultiProfiles(communityId: Int, userId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.communityService.getUserMultiProfiles("Bearer $token", communityId, userId)
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
                val response = RetrofitClient.communityService.setUserStatus("Bearer ${token}", request)
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

    fun loadProfiles() {
        viewModelScope.launch {
            _profiles.value = profileManager.getProfiles()
            _selectedProfile.value = profileManager.getSelectedProfile()
        }
    }

    fun selectProfile(profile: Profile) {
        viewModelScope.launch {
            profileManager.selectProfile(profile.id)
            _selectedProfile.value = profile
        }
    }

    fun addProfile(profile: Profile) {
        viewModelScope.launch {
            profileManager.addProfile(profile)
            _profiles.value = profileManager.getProfiles()
        }
    }

    fun deleteProfile(profile: Profile) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.communityService.deleteMultiProfile("Bearer $token", profile.id)
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