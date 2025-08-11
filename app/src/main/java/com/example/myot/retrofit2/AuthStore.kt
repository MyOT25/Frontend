package com.example.myot.retrofit2

object AuthStore {
    var accessToken: String? = null

    fun bearerOrNull(): String? = accessToken?.let { "Bearer $it" }
    fun bearerOrThrow(): String =
        bearerOrNull() ?: throw IllegalStateException("로그인이 필요합니다")
}