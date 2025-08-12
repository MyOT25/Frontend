package com.example.myot.memorybook.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// MemoryBook API와 통신하기 위한 Retrofit 클라이언트 객체
object MemoryBookRetrofitClient {

    // Swagger와 연결된 실제 API 서버 주소
    private const val BASE_URL = "http://43.203.70.205:3000/"

    // 개발용 access token (나중에 로그인 기능으로 대체 예정)
    // 현재는 관리자용 토큰을 하드코딩해둔 상태
    private const val DEV_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
            "eyJ1c2VySWQiOjIsImxvZ2luSWQiOiJoYWt5NTAyNiIsImlhdCI6MTc1NDAwNDg1MCwiZXhwIjoxNzU0NjA5NjUwfQ." +
            "bo5wItdkZuPj4pUuT0DOPVC5rudpBpyFtpvhWArB6-M"

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

    // Retrofit 객체 생성 및 MemoryBookApi 구현체 반환
    val memoryBookApi: MemoryBookApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // 정확한 API 루트 주소
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MemoryBookApi::class.java)
    }
}
