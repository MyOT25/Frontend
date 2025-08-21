package com.example.myot.chatting.chatroomapi
// com.example.myot.chatting.chatroomapi.ChatRoomListRetrofitInstance.kt
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ChatRoomListRetrofitInstance {
    private const val BASE_URL = "http://43.203.70.205:3000/"
    private const val TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
            "eyJ1c2VySWQiOjEsImxvZ2luSWQiOiJ0ZXN0dXNlcjAxIiwiaWF0IjoxNzU1Njcx" +
            "NDc1LCJleHAiOjE3NTYyNzYyNzV9.DmfPu1Zo0Qi9q8sTstWqOl6sKuahsp2BGDJe3nEE03I"

    // Authorization 헤더를 추가하는 인터셉터
    private val authInterceptor = Interceptor { chain ->
        val newRequest = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $TOKEN")
            .build()
        chain.proceed(newRequest)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    val api: ChatRoomApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChatRoomApi::class.java)
    }
}
