package com.example.myot.chatting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myot.chatting.chattinglist.ChatItem

class ChatViewModel : ViewModel() {
    // ChatFragment의 데이터를 담는 뷰모델

    private val _chatList = MutableLiveData<MutableList<ChatItem>>().apply {
        value = mutableListOf(
            ChatItem("5", "다람쥐", "안녕하세요~~", "방금", 99),
            ChatItem("6", "토깽이", "안녕하세요안녕하세요안녕하세요...", "1분 전", 105),
            ChatItem("7", "고양이", "안녕하세요~~", "10분 전", 0),
            ChatItem("8", "거북이", "사진을 보냈습니다.", "1시간 전", 0)
        )
    }
    val chatList: LiveData<MutableList<ChatItem>> get() = _chatList

    // 새 채팅 추가 (ID 중복 여부와 상관없이 항상 맨 앞에 추가)
    fun addChat(item: ChatItem) {
        val currentList = _chatList.value?.toMutableList() ?: mutableListOf()
        currentList.add(0, item)  // 항상 맨 앞에 삽입
        _chatList.value = currentList
    }

    // 채팅 내용/시간/읽음 여부 업데이트
    fun updateChat(id: String, lastMessage: String?, lastMessageTime: String?, isRead: Boolean) {
        val currentList = _chatList.value?.toMutableList() ?: return
        val idx = currentList.indexOfFirst { it.id == id }
        if (idx != -1) {
            val old = currentList[idx]
            currentList[idx] = old.copy(
                lastMessage = lastMessage ?: old.lastMessage,
                time = lastMessageTime ?: old.time,
                unreadCount = if (isRead) 0 else old.unreadCount
            )
            _chatList.value = currentList
        }
    }

    // 채팅 고정 (맨 위로 이동)
    fun pinChat(position: Int) {
        val currentList = _chatList.value?.toMutableList() ?: return
        if (position in currentList.indices) {
            val chat = currentList.removeAt(position)
            currentList.add(0, chat)
            _chatList.value = currentList
        }
    }

    // 채팅 삭제
    fun deleteChat(position: Int) {
        val currentList = _chatList.value?.toMutableList() ?: return
        if (position in currentList.indices) {
            currentList.removeAt(position)
            _chatList.value = currentList
        }
    }

    // 채팅 목록 전체 교체 (서버에서 받아온 목록 반영) // 추가
    fun setChatList(newList: List<ChatItem>) {
        _chatList.value = newList.toMutableList()
    }
}
