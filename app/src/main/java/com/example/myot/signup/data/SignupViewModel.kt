package com.example.myot.signup.data

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SignupViewModel : ViewModel() {
    val name = MutableLiveData<String>()
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val loginId = MutableLiveData<String>()
    val profileImageUri = MutableLiveData<Uri?>()
    val selectedCommunities = MutableLiveData<Set<Long>>(emptySet())
}