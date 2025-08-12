package com.example.myot.chatting.chatroom

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.R
import com.example.myot.chat.ChatMessage
import com.example.myot.chat.ChatMessageAdapter
import java.text.SimpleDateFormat
import java.util.*

/**
 * 채팅 화면 Activity
 * - 상대방 메시지는 왼쪽, 내 메시지는 오른쪽으로 표시한다.
 * - 키보드 '전송' 버튼으로 메시지를 보낸다.
 * - 채팅방을 나가면 해당 채팅의 읽지 않은 메시지 수는 0으로 처리된다.
 */
class ChatRoomActivity : AppCompatActivity() {

    // ─── 뷰 바인딩 ──────────────────────────
    private lateinit var backButton: ImageButton   // 뒤로 가기 버튼
    private lateinit var chatUserText: TextView    // 채팅 상대 닉네임
    private lateinit var messageInput: EditText    // 메시지 입력창
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatMessageAdapter

    // ─── 데이터 ────────────────────────────
    private val chatMessages = mutableListOf<ChatMessage>()
    private val currentUserId = "me" // 실제 사용자 ID로 교체하세요

    private var chatRoomId: String = "" // 채팅방 고유 ID
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)

        // 1) 뷰 초기화
        backButton   = findViewById(R.id.btn_back)
        chatUserText = findViewById(R.id.tv_chat_user)
        messageInput = findViewById(R.id.et_message)
        recyclerView = findViewById(R.id.rv_chat_messages)

        // 2) 인텐트로 전달된 채팅방 정보
        chatRoomId   = intent.getStringExtra("chatRoomId") ?: "unknown_room"
        userId       = intent.getStringExtra("userId") ?: "unknown_user"
        val userNickname = intent.getStringExtra("userNickname") ?: "알 수 없는 유저"
        chatUserText.text = userNickname

        // 3) 더미 메시지 (테스트용)
        chatMessages.add(ChatMessage(senderId = userId,         content = "안녕하세요~~"))
        chatMessages.add(ChatMessage(senderId = currentUserId,  content = "네, 반갑습니다!"))
        chatMessages.add(ChatMessage(senderId = userId,         content = "이제 왼쪽/오른쪽 배치가 잘 될 거예요."))

        // 4) RecyclerView 설정
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ChatMessageAdapter(chatMessages, currentUserId)
        recyclerView.adapter = adapter

        // 5) 키보드 전송 버튼 처리
        messageInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }

        // 6) 뒤로가기 버튼 → 읽음 처리 + 마지막 메시지 전달
        backButton.setOnClickListener {
            val lastMsg = chatMessages.lastOrNull() // 마지막 메시지 가져오기
            val resultIntent = Intent().apply {
                putExtra("chatRoomId", chatRoomId)
                putExtra("read", true) // 읽음 처리
                putExtra("lastMessage", lastMsg?.content ?: "")
                putExtra("lastMessageTime", getCurrentTimeFormatted())
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    /**
     * 메시지를 리스트에 추가하고 RecyclerView 갱신 + 결과 전달
     */
    private fun sendMessage() {
        val text = messageInput.text.toString().trim()
        if (text.isNotEmpty()) {
            val newMsg = ChatMessage(
                senderId = currentUserId,
                content = text,
                profileUrl = null
            )
            chatMessages.add(newMsg)
            adapter.notifyItemInserted(chatMessages.size - 1)
            recyclerView.scrollToPosition(chatMessages.size - 1)
            messageInput.text.clear()

            val formattedTime = getCurrentTimeFormatted()

            val resultIntent = Intent().apply {
                putExtra("chatRoomId", chatRoomId)
                putExtra("lastMessage", newMsg.content)
                putExtra("lastMessageTime", formattedTime)
                putExtra("read", true) // 메시지 보냈다면 당연히 읽은 상태
            }
            setResult(Activity.RESULT_OK, resultIntent)
        }
    }

    /**
     * 현재 시간을 포맷팅된 문자열로 반환 (예: "오후 3:40")
     */
    private fun getCurrentTimeFormatted(): String {
        val sdf = SimpleDateFormat("a h:mm", Locale.KOREA)
        return sdf.format(Date())
    }
}
