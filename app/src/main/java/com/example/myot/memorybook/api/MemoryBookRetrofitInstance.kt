package com.example.myot.memorybook.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// MemoryBook 전용 Retrofit 인스턴스를 제공하는 객체
object MemoryBookRetrofitInstance {

    // Retrofit 인스턴스 생성
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://your-api-base-url.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // MemoryBookApi 구현체 반환
    val memoryBookApi: MemoryBookApi by lazy {
        retrofit.create(MemoryBookApi::class.java)
    }
}
