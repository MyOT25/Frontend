package com.example.myot.chatting.chattinglist

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

    // ───────────────────────────────
    // ViewHolder: 채팅 목록 아이템 뷰
    inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val profileImage: ImageView = view.findViewById(R.id.image_profile)
        private val nicknameText: TextView = view.findViewById(R.id.text_nickname)
        private val timeText: TextView = view.findViewById(R.id.text_time)
        private val messageText: TextView = view.findViewById(R.id.text_message)
        private val unreadBadge: TextView = view.findViewById(R.id.badge_unread)

        // 아이템 바인딩 함수
        fun bind(item: ChatItem) {
            nicknameText.text = item.nickname
            timeText.text = item.time
            messageText.text = item.lastMessage

            // 뱃지 처리
            when {
                item.isNew -> {
                    unreadBadge.text = "!"
                    unreadBadge.visibility = View.VISIBLE
                }
                item.unreadCount > 0 -> {
                    unreadBadge.text = if (item.unreadCount > 100) "100+" else item.unreadCount.toString()
                    unreadBadge.visibility = View.VISIBLE
                }
                else -> {
                    unreadBadge.visibility = View.GONE
                }
            }

            // 클릭 리스너
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

    // ───────────────────────────────
    // 채팅 상단 고정 기능
    fun pinItem(position: Int) {
        if (position !in items.indices) return
        val item = items.removeAt(position).copy(isPinned = true)
        items.add(0, item)
        notifyItemRemoved(position)
        notifyItemInserted(0)
    }

    // 채팅 삭제
    fun deleteItem(position: Int) {
        if (position !in items.indices) return
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    // 새로운 채팅 추가
    fun insertNewChatItem(item: ChatItem) {
        items.add(0, item)
        notifyItemInserted(0)
    }

    // 마지막 메시지 갱신
    fun updateLastMessage(chatRoomId: String, message: String, time: String) {
        val index = items.indexOfFirst { it.id == chatRoomId }
        if (index != -1) {
            val old = items[index]
            items[index] = old.copy(lastMessage = message, time = time)
            notifyItemChanged(index)
        }
    }

    // ID로 ChatItem 가져오기
    fun getItemById(chatRoomId: String): ChatItem? {
        return items.find { it.id == chatRoomId }
    }

    // ───────────────────────────────
    // ViewModel로부터 갱신된 전체 리스트 적용
    fun updateList(newList: List<ChatItem>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}
