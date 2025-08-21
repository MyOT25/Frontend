package com.example.myot.chatting.messagesapi

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// MessageSendApi.kt

// 메시지 전송 API 정의
interface MessageSendApi {

    // 메시지 전송
    @POST("/api/messages")
    suspend fun sendMessage(
        @Header("Authorization") accessToken: String, // 엑세스 토큰 헤더
        @Body request: SendMessageRequest              // 요청 바디 DTO
    ): SendMessageResponse // 응답 DTO
}

// 하드코딩된 액세스 토큰
const val HARDCODED_ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
        "eyJ1c2VySWQiOjEsImxvZ2luSWQiOiJ0ZXN0dXNlcjAxIiwiaWF0IjoxNzU1Njcx" +
        "NDc1LCJleHAiOjE3NTYyNzYyNzV9.DmfPu1Zo0Qi9q8sTstWqOl6sKuahsp2BGDJe3nEE03I"
