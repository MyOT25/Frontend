package com.example.myot.memorybook.api

import com.example.myot.memorybook.api.MemoryBookRequest
import com.example.myot.memorybook.api.MemoryBookResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// memorybook/MemoryBookApi.kt

// Retrofit 인터페이스
interface MemoryBookApi {
    @POST("/api/posts/memorybooks")
    suspend fun createMemoryBook(@Body request: MemoryBookRequest): Response<MemoryBookResponse>
}
//테스트
