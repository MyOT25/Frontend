package com.example.myot.memorybook.api

// 메모리북 생성 응답 DTO
data class MemoryBookResponse(
    val resultType: String,
    val error: ErrorResponse?,      // 실패 시
    val success: SuccessBlock?      // 성공 시
)

// 실패 시 서버에서 주는 에러 정보
data class ErrorResponse(
    val errorCode: String,
    val reason: String,
    val data: Any? = null
)

// 성공 시 서버에서 주는 정보
data class SuccessBlock(
    val message: String,
    val data: MemoryBookData
)

// 성공 응답의 데이터 필드
data class MemoryBookData(
    val memoryBookId: Int,
    val title: String,
    val targetType: String,
    val targetId: Int,
    val createdAt: String,
    val content: Content            // ← 서버에서 내려주는 본문 데이터
)
