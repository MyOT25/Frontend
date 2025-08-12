package com.example.myot.memorybook.api

// 서버 리스폰스
data class MemoryBookResponse(
    val resultType: String,
    val error: ErrorResponse?, // FAIL일 경우 존재
    val success: MemoryBookData?, // SUCCESS일 경우 존재
    val message: String? = null
)

// 실패 시 서버에서 주는 에러 정보
data class ErrorResponse(
    val errorCode: String,
    val reason: String,
    val data: Any? = null
)

// 성공 응답 시 들어오는 데이터
data class MemoryBookData(
    val id: Int,
    val targetType: String,
    val title: String,
    val content: Content,
    val createdAt: String
)
