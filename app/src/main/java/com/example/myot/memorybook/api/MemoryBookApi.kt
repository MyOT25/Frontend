// memorybook/MemoryBookApi.kt
package com.example.myot.memorybook.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// 메모리북 생성 API
interface MemoryBookApi {
    @POST("/api/posts/memorybooks")
    suspend fun createMemoryBook(
        @Body request: MemoryBookRequest
    ): Response<MemoryBookResponse>
}

// 8/20 오류 수정 완
