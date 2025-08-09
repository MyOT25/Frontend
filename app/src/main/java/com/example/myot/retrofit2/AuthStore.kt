package com.example.myot.retrofit2

object AuthStore {
    var accessToken: String? = null
    fun bearer(): String = "Bearer ${accessToken ?: ""}"
}