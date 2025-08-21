package com.example.myot.chatting.messagesapi

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
// MessageSendRetrofitClient.kt
// 메시지 전송 API 전용 Retrofit


object MessageSendRetrofitClient {

    // API 서버 기본 URL
    private const val BASE_URL = "http://43.203.70.205:3000/"

    // Retrofit 인스턴스 생성
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // JSON 파싱
            .build()
    }

    // MessageSendApi 구현체
    val api: MessageSendApi by lazy {
        retrofit.create(MessageSendApi::class.java)
    }
}
