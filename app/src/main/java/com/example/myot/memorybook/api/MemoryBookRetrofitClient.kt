package com.example.myot.memorybook.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// MemoryBook API와 통신하기 위한 Retrofit 클라이언트 객체
// - MemoryBookRequest / MemoryBookResponse DTO 구조에 맞춰 API 요청을 전송
// - createMemoryBook() 호출 시 JSON Body로 전송됨
object MemoryBookRetrofitClient {

    // Swagger와 연결된 실제 API 서버 주소
    private const val BASE_URL = "http://43.203.70.205:3000/"

    // 개발용 access token (나중에 로그인 기능으로 대체 예정)
    // 현재는 관리자용 토큰을 하드코딩해둔 상태
    private const val DEV_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
            "eyJ1c2VySWQiOjEsImxvZ2luSWQiOiJ0ZXN0dXNlcjAxIiwiaWF0IjoxNzU1Njcx" +
            "NDc1LCJleHAiOjE3NTYyNzYyNzV9.DmfPu1Zo0Qi9q8sTstWqOl6sKuahsp2BGDJe3nEE03I"

    // Authorization 헤더를 추가하는 Interceptor
    // 모든 API 요청에 Bearer 토큰을 포함
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
    // - BASE_URL과 인증 클라이언트를 연결
    // - GsonConverterFactory를 통해 DTO <-> JSON 변환
    val memoryBookApi: MemoryBookApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // 정확한 API 루트 주소
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MemoryBookApi::class.java)
    }
}
