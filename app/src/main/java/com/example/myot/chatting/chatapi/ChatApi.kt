package com.example.myot.chatting.chatapi

import android.content.Context
import com.google.gson.annotations.SerializedName
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

// 채팅방 생성 API

// ───── 공통 응답 래퍼 ───────────────────────────────
data class ApiEnvelope<T>(
    val resultType: String,
    val success: SuccessBlock<T>?
)
data class SuccessBlock<T>(
    val message: String,
    val data: T?
)

// ───── 채팅방 목록 조회 DTO ────────────────────────
data class ChatRoomDto(
    val id: Int,
    val name: String,
    val type: String
)

// ───── 채팅방 생성 DTO ────────────────────────────
// 백엔드 요구사항: type + opponentId만 필수, 나머지는 기본 null
data class CreateChatRoomRequest(
    val type: String,
    val opponentId: Int? = null,
    val userIds: List<Int>? = null,
    val name: String? = null
)

data class CreateChatRoomResponse(
    @SerializedName("chatRoomId") val chatRoomId: Int,
    val type: String,
    val name: String?
)

// ───── 메시지 조회 DTO ─────────────────────────────
data class ChatMessageDto(
    val id: Int,
    val content: String,
    val senderId: Int,
    val createdAt: String
)
data class MessageListDto(
    val messages: List<ChatMessageDto>,
    val nextCursor: Int?
)

// ───── 메시지 전송 DTO ─────────────────────────────
data class SendMessageRequest(
    val chatRoomId: Int,
    val content: String
)
data class SendMessageResponse(
    val id: Int,
    val content: String,
    val senderId: Int,
    val chatRoomId: Int,
    val createdAt: String
)

// ───── Retrofit API 인터페이스 ─────────────────────
interface ChatApi {
    @GET("/api/chatrooms")
    fun getChatRooms(): Call<ApiEnvelope<List<ChatRoomDto>>>

    @POST("/api/chatrooms")
    fun createChatRoom(@Body body: CreateChatRoomRequest): Call<ApiEnvelope<CreateChatRoomResponse>>

    @GET("/api/messages")
    fun getMessages(
        @Query("chatRoomId") chatRoomId: Int,
        @Query("cursor") cursor: Int? = null
    ): Call<ApiEnvelope<MessageListDto>>

    @POST("/api/messages")
    fun sendMessage(@Body body: SendMessageRequest): Call<ApiEnvelope<SendMessageResponse>>

    companion object {
        interface TokenProvider {
            fun getAccessToken(): String?
        }

        fun create(
            baseUrl: String = "http://43.203.70.205:3000",
            tokenProvider: TokenProvider
        ): ChatApi {
            val auth = object : Interceptor {
                override fun intercept(chain: Interceptor.Chain): Response {
                    val token = tokenProvider.getAccessToken()
                    val reqBuilder = chain.request().newBuilder()
                    if (!token.isNullOrBlank()) {
                        reqBuilder.addHeader("Authorization", "Bearer $token")
                    }
                    return chain.proceed(reqBuilder.build())
                }
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(auth)
                .build()

            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(ChatApi::class.java)
        }

        // SharedPreferences에서 토큰 불러오는 버전
        fun createWithPrefs(context: Context): ChatApi {
            return create(tokenProvider = object : TokenProvider {
                override fun getAccessToken(): String? {
                    val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    return prefs.getString("access_token", null)
                }
            })
        }
    }
}
