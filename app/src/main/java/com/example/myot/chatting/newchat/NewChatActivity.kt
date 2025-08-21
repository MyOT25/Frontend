package com.example.myot.chatting.newchat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.R
import com.example.myot.chatting.chatapi.*
import com.example.myot.chatting.chatroom.ChatRoomActivity
import kotlinx.coroutines.launch

// 새로운 채팅방을 만드는 액티비티
class NewChatActivity : AppCompatActivity() {

    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userAdapter: UserListAdapter
    private lateinit var chatRepository: ChatRepository

    // 하드코딩 토큰
    private val hardcodedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
            "eyJ1c2VySWQiOjEsImxvZ2luSWQiOiJ0ZXN0dXNlcjAxIiwiaWF0IjoxNzU1NjcxNDc" +
            "1LCJleHAiOjE3NTYyNzYyNzV9.DmfPu1Zo0Qi9q8sTstWqOl6sKuahsp2BGDJe3nEE03I"

    // 더미 유저 목록
    private val dummyUsers = listOf(
        User(1, "다람쥐"),
        User(2, "너굴맨"),
        User(3, "늉늉이"),
        User(4, "스라소니")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_chat)

        // ChatApi 생성 시 토큰 제공
        val chatApi = ChatApi.create(tokenProvider = object : ChatApi.Companion.TokenProvider {
            override fun getAccessToken(): String? {
                return hardcodedToken
            }
        })
        chatRepository = ChatRepository(chatApi)

        userRecyclerView = findViewById(R.id.rv_user_list)
        userRecyclerView.layoutManager = LinearLayoutManager(this)

        userAdapter = UserListAdapter(dummyUsers) { selectedUser ->
            handleUserSelection(selectedUser)
        }
        userRecyclerView.adapter = userAdapter
    }

    // 유저 선택 시 채팅방 생성 요청
    private fun handleUserSelection(user: User) {
        lifecycleScope.launch {
            val token = hardcodedToken
            Log.d("TOKEN_DEBUG", "사용 중인 토큰: '$token'")

            if (token.isNullOrBlank()) {
                Toast.makeText(this@NewChatActivity, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // 요청 JSON 구조 로그로 확인
            val req = CreateChatRoomRequest(
                type = "ONE_TO_ONE",  // 서버 명세 상 대문자 ENUM
                opponentId = user.id  // 서버가 요구하는 정확한 키와 타입
            )
            Log.d("NewChatActivity", "채팅방 생성 요청 데이터: $req")

            val result = chatRepository.createChatRoom(req)
            result.onSuccess { response ->
                Log.d("NewChatActivity", "채팅방 생성 성공: $response")

                val chatRoomId = response.chatRoomId
                val chatRoomName = response.name ?: user.nickname

                val chatIntent = Intent(this@NewChatActivity, ChatRoomActivity::class.java).apply {
                    putExtra("chatRoomId", chatRoomId)
                    putExtra("userId", user.id.toString())  // 상대방 ID
                    putExtra("userNickname", chatRoomName)  // 채팅방 이름 or 상대방 닉네임
                }
                startActivity(chatIntent)
                finish()
            }.onFailure { e ->
                Log.e("NewChatActivity", "채팅방 생성 실패", e)
                Toast.makeText(
                    this@NewChatActivity,
                    "채팅방 생성 실패: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
