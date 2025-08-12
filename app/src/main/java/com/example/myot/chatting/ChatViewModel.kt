package com.example.myot.chatting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myot.chatting.chattinglist.ChatItem

class ChatViewModel : ViewModel() {

    private val _chatList = MutableLiveData<MutableList<ChatItem>>().apply {
        value = mutableListOf(
            ChatItem("1", "다람쥐", "안녕하세요~~", "방금", 1),
            ChatItem("2", "토깽이", "안녕하세요안녕하세요안녕하세요...", "1분 전", 105),
            ChatItem("3", "고양이", "안녕하세요~~", "10분 전", 0),
            ChatItem("4", "거북이", "사진을 보냈습니다.", "1시간 전", 0)
        )
    }
    val chatList: LiveData<MutableList<ChatItem>> get() = _chatList

    fun addChat(item: ChatItem) {
        _chatList.value?.add(0, item)
        _chatList.postValue(_chatList.value)
    }

    fun updateChat(id: String, lastMessage: String?, lastMessageTime: String?, isRead: Boolean) {
        val list = _chatList.value ?: return
        val idx = list.indexOfFirst { it.id == id }
        if (idx != -1) {
            val old = list[idx]
            list[idx] = old.copy(
                lastMessage = lastMessage ?: old.lastMessage,
                time = lastMessageTime ?: old.time,
                unreadCount = if (isRead) 0 else old.unreadCount
            )
            _chatList.postValue(list)
        }
    }

    fun pinChat(position: Int) {
        _chatList.value?.let {
            val chat = it.removeAt(position)
            it.add(0, chat)
            _chatList.postValue(it)
        }
    }

    fun deleteChat(position: Int) {
        _chatList.value?.let {
            it.removeAt(position)
            _chatList.postValue(it)
        }
    }
}
