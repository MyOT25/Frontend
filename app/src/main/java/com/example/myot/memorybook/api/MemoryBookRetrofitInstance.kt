package com.example.myot.memorybook.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// MemoryBook 전용 Retrofit 인스턴스를 제공하는 객체
// - MemoryBookRequest / MemoryBookResponse DTO 구조에 맞춰 API 요청을 전송
// - 모든 요청에 Authorization 헤더를 포함시킴
object MemoryBookRetrofitInstance {

    // Swagger와 연결된 실제 API 서버 주소
    private const val BASE_URL = "http://43.203.70.205:3000/"

    // 개발용 access token (나중에 로그인 기능으로 대체 예정)
    private const val DEV_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
            "eyJ1c2VySWQiOjIsImxvZ2luSWQiOiJoYWt5NTAyNiIsImlhdCI6MTc1NDk5MDcwMCwi" +
            "ZXhwIjoxNzU1NTk1NTAwfQ.OB7JKIrVGjqG2lruN_D4q6dcbCPSP9_Hpm9cGNp5jOI"

    // Authorization 헤더를 추가하는 Interceptor
    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        requestBuilder.addHeader("Authorization", "Bearer $DEV_ACCESS_TOKEN")
        chain.proceed(requestBuilder.build())
    }

    // 인증 헤더가 포함된 OkHttpClient 생성
    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    // Retrofit 인스턴스 생성
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // 정확한 API 루트 주소
            .client(client) // 인증 클라이언트 연결
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // MemoryBookApi 구현체 반환
    val memoryBookApi: MemoryBookApi by lazy {
        retrofit.create(MemoryBookApi::class.java)
    }
}
