package com.example.myot.Chatting.ChattingList

data class ChatItem(
    val id: String, //채팅의 고유 식별자
    val nickname: String, //UI 정보
    val lastMessage: String,
    val time: String,
    val unreadCount: Int, //읽지 않은 메세지의 개수
    val isNew: Boolean = false, //새로 만든 채팅방인지 확인하기 위한 불리안 변수 true면 '!'이미지 출력
    val isPinned: Boolean = false //fix 기능을 위한 불리안 변수
)
//채팅 목록의 리사이클러 뷰를 위한 아이템
