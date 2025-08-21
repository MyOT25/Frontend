package com.example.myot.chatting.newchat

// 채팅 생성 시 대화 상대 검색을 위한 클래스 (API 연동 버전)
data class User(
    val id: Int,          // ← 서버에서 내려오는 사용자 ID, string에서 int로 변경
    val nickname: String  // ← 서버에서 내려오는 닉네임
)
