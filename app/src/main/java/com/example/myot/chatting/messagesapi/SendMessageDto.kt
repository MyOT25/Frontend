package com.example.myot.chatting.messagesapi
// SendMessageDto.kt


// 요청 DTO
data class SendMessageRequest(
    val chatRoomId: Int,
    val content: String
)

// 응답 DTO
data class SendMessageResponse(
    val resultType: String,
    val error: Any?,
    val success: SuccessData
)

data class SuccessData(
    val message: String,
    val data: MessageData
)

data class MessageData(
    val messageId: Int,
    val chatRoomId: Int,
    val senderId: Int,
    val content: String,
    val createdAt: String
)
