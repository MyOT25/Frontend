// memorybook/MemoryBookRequest.kt
package com.example.myot.memorybook.api

// 메모리북 생성 요청 DTO
data class MemoryBookRequest(
    val targetType: String,       // 예: "MUSICAL"
    val targetId: Int,            // 대상 ID (예: 뮤지컬 ID)
    val title: String,            // 메모리북 제목
    val content: Content          // 블록 형식의 본문 데이터
)

// Content 구조
data class Content(
    val blocks: List<Block>       // 여러 블록(제목, 문단 등)
)

// 블록 데이터 구조
data class Block(
    val type: String,             // 예: "header", "paragraph"
    val data: BlockData           // 블록 데이터 (텍스트, 레벨 등)
)

// 블록 안의 데이터 구조
data class BlockData(
    val text: String,             // 텍스트 내용
    val level: Int? = null         // 헤더일 경우 레벨 (optional)
)
