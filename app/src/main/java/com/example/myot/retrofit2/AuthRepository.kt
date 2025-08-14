package com.example.myot.retrofit2

class AuthRepository(
    private val service: AuthService
) {
    suspend fun login(loginId: String, password: String): Result<LoginSuccessDto> = runCatching {
        val res = service.login(LoginRequestDto(loginId, password))
        require(res.resultType == "SUCCESS" && res.success != null) { "LOGIN_FAIL" }
        res.success
    }

    suspend fun signup(req: SignupRequestDto): Result<SignupResponseDto> = runCatching {
        val res = service.signup(req)
        require(res.resultType == "SUCCESS" && res.success != null) { "SIGNUP_FAIL" }
        res.success
    }
}