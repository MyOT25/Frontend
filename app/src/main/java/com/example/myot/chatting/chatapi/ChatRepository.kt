package com.example.myot.chatting.chatapi

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse

class ChatRepository(private val api: ChatApi) {

    // 새 채팅방 생성
    suspend fun createChatRoom(req: CreateChatRoomRequest): Result<CreateChatRoomResponse> {
        return withContext(Dispatchers.IO) {
            runCatching {
                Log.d("ChatRepository", "채팅방 생성 요청 데이터: $req")

                val res = api.createChatRoom(req).awaitResponse()
                Log.d("ChatRepository", "HTTP Status: ${res.code()}")

                if (!res.isSuccessful) {
                    val errStr = res.errorBody()?.string()
                    Log.e("ChatRepository", "서버 에러 응답: $errStr")
                    error("HTTP ${res.code()} - $errStr")
                }

                val body = res.body()
                Log.d("ChatRepository", "응답 body: $body")

                if (body == null) error("응답이 비어있습니다.")

                // 서버에서 FAIL 대신 SUCCESS로 주면서 "기존 채팅방이 존재" 메시지가 올 수 있으므로
                if (body.success != null && body.success.data != null) {
                    Log.d("ChatRepository", "채팅방 데이터 반환: ${body.success.data}")
                    return@runCatching body.success.data
                }

                // 혹시 success가 null이고 error 메시지가 있는 경우
                val failMsg = body.toString()
                Log.e("ChatRepository", "채팅방 생성 실패 또는 데이터 없음: $failMsg")
                error("채팅방 생성 실패 - $failMsg")
            }
        }
    }
}
