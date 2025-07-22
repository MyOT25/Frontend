package com.example.myot.chatting.newchat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.chatting.chatroom.ChatRoomActivity
import com.example.myot.R

//새로운 채팅방을 생성하는 액티비티
class NewChatActivity : AppCompatActivity() {

    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userAdapter: UserListAdapter

    // 더미 사용자 데이터
    private val dummyUsers = listOf(
        User("u1", "다람쥐"),
        User("u2", "너굴맨"),
        User("u3", "늉늉이"),
        User("u4", "스라소니")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_chat)

        // RecyclerView 설정
        userRecyclerView = findViewById(R.id.rv_user_list)
        userRecyclerView.layoutManager = LinearLayoutManager(this)

        // 어댑터 설정 및 클릭 시 유저 선택 처리
        userAdapter = UserListAdapter(dummyUsers) { selectedUser ->
            handleUserSelection(selectedUser)
        }
        userRecyclerView.adapter = userAdapter
    }

    // 유저 선택 시 ChatFragment에 결과 전달 및 ChatRoomActivity로 이동
    private fun handleUserSelection(user: User) {
        Toast.makeText(this, "${user.nickname} 선택됨", Toast.LENGTH_SHORT).show()

        // 선택된 사용자 정보를 ChatFragment로 전달
        val resultIntent = Intent().apply {
            putExtra("userId", user.id)  // ChatRoomActivity에서 사용하려면 유지
            putExtra("userNickname", user.nickname)
        }
        setResult(Activity.RESULT_OK, resultIntent)

        // ChatRoomActivity로 이동
        val chatIntent = Intent(this, ChatRoomActivity::class.java).apply {
            putExtra("userId", user.id)
            putExtra("userNickname", user.nickname)
        }
        startActivity(chatIntent)

        // 이 액티비티 종료 → ChatFragment로 돌아감
        finish()
    }
}
