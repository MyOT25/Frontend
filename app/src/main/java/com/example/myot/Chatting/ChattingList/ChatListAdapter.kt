package com.example.myot.Chatting.ChattingList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.R

// 채팅 목록에서 사용하는 어댑터
class ChatListAdapter(
    private val items: MutableList<ChatItem>,
    private val onItemClick: (ChatItem) -> Unit,
    private val onItemPin: (Int) -> Unit,
    private val onItemDelete: (Int) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    // 채팅 목록의 각 아이템을 표현하는 ViewHolder 클래스
    inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val profileImage: ImageView = view.findViewById(R.id.image_profile)
        private val nicknameText: TextView = view.findViewById(R.id.text_nickname)
        private val timeText: TextView = view.findViewById(R.id.text_time)
        private val messageText: TextView = view.findViewById(R.id.text_message)
        private val unreadBadge: TextView = view.findViewById(R.id.badge_unread)

        // ViewHolder에 채팅 데이터를 바인딩하는 함수
        fun bind(item: ChatItem) {
            nicknameText.text = item.nickname
            timeText.text = item.time
            messageText.text = item.lastMessage

            // 읽지 않은 메시지 수에 따라 뱃지 표시
            if (item.isNew) {
                unreadBadge.text = "!"
                unreadBadge.visibility = View.VISIBLE
            } else if (item.unreadCount > 0) {
                unreadBadge.text = if (item.unreadCount > 100) "100+" else item.unreadCount.toString()
                unreadBadge.visibility = View.VISIBLE
            } else {
                unreadBadge.visibility = View.GONE
            }

            // 항목 클릭 시 콜백 실행
            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        if (position in items.indices) {
            holder.bind(items[position])
        }
    }

    // 채팅 상단 고정
    fun pinItem(position: Int) {
        if (position < 0 || position >= items.size) return
        val item = items.removeAt(position).copy(isPinned = true)
        items.add(0, item)
        notifyItemRemoved(position)
        notifyItemInserted(0)
    }

    // 채팅 삭제
    fun deleteItem(position: Int) {
        if (position < 0 || position >= items.size) return
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    // 새 채팅 삽입
    fun insertNewChatItem(item: ChatItem) {
        items.add(0, item)
        notifyItemInserted(0)
    }
}
