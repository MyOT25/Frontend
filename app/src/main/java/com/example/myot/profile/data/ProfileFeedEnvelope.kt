package com.example.myot.profile.data

data class ProfileFeedEnvelope<T>(
    val resultType: String? = null,
    val data: T? = null
)