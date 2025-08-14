package com.example.myot.retrofit2

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("api/auth/login")
    suspend fun login(
        @Body body: LoginRequestDto
    ): ApiResponseSimple<LoginSuccessDto?>
}

// 요청/응답 DTO
data class LoginRequestDto(
    val loginId: String,
    val password: String
)

data class LoginSuccessDto(
    val userId: Long,
    val accessToken: String
)

data class ApiResponseSimple<T>(
    val resultType: String,
    val error: Any?,
    val success: T?
)