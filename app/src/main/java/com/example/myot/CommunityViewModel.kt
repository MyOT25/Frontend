package com.example.myot

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData

class CommunityViewModel: ViewModel() {
    private val _communityMode = MutableLiveData<CommunityMode>()
    val communityMode: LiveData<CommunityMode> = _communityMode

    fun setCommunityMode(mode: CommunityMode) {
        _communityMode.value = mode
    }

    fun switchCommunityMode() {
        _communityMode.value = if (_communityMode.value == CommunityMode.MEMBER) CommunityMode.GUEST else CommunityMode.MEMBER
    }
}