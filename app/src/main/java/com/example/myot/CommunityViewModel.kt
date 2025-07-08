package com.example.myot

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CommunityViewModel: ViewModel() {
    private val _communityMode = MutableStateFlow(CommunityMode.GUEST)
    val communityMode: StateFlow<CommunityMode> = _communityMode.asStateFlow()

    fun setCommunityMode(mode: CommunityMode) {
        _communityMode.value = mode
    }

    fun switchCommunityMode() {
        _communityMode.value = if (_communityMode.value == CommunityMode.MEMBER) CommunityMode.GUEST else CommunityMode.MEMBER
    }
}