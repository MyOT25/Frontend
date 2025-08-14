package com.example.myot.retrofit2

class AuthRepository(
    private val service: AuthService
) {
    suspend fun login(id: String, pw: String): Result<LoginSuccessDto> = runCatching {
        val res = service.login(LoginRequestDto(id, pw))
        val ok = res.success ?: error("서버 응답에 success 없음")
        AuthStore.accessToken = ok.accessToken
        ok
    }
}