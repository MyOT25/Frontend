// memorybook/MemoryBookRequest.kt
package com.example.myot.memorybook.api

//서버 리퀘스트
data class MemoryBookRequest(
    val targetType: String,
    val targetId: Int,
    val title: String,
    val content: Content,
    val images: List<String>
)

data class Content(val paragraph: String)
