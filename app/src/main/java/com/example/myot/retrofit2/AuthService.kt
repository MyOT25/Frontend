package com.example.myot.retrofit2

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequestDto): ApiResponseSimple<LoginSuccessDto?>

    @POST("api/auth/signup")
    suspend fun signup(@Body body: SignupRequestDto): ApiResponseSimple<SignupResponseDto?>
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

data class SignupRequestDto(
    val username: String,
    val email: String,
    val loginId: String,
    val password: String,
    val nickname: String
)

data class SignupResponseDto(
    val id: Long,
    val username: String,
    val nickname: String,
    val email: String
)

data class ApiResponseSimple<T>(
    val resultType: String,
    val error: Any?,
    val success: T?
)