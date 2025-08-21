package com.example.myot.chatting.chatroomapi

// 채팅 목록 조회를 위한 응답
data class ChatRoomListResponse(
    val resultType: String,
    val error: String?,          // 실패 시 에러 메시지 (null 가능)
    val success: SuccessData?    // 성공 시 데이터
)

data class SuccessData(
    val message: String,
    val data: List<ChatRoom>     // 채팅방 목록
)

data class ChatRoom(
    val chatRoomId: Int,
    val name: String?,
    val type: String,
    val participants: List<Participant>,
    val lastMessage: String?
)

data class Participant(
    val id: Int,
    val nickname: String
)
