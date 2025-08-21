package com.example.myot.chatting.chattinglist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.R

// 채팅 목록 어댑터 - 데이터는 ViewModel에서 관리, 어댑터는 반영만 담당
class ChatListAdapter(
    private val items: MutableList<ChatItem>, // 현재 채팅 리스트
    private val onItemClick: (ChatItem) -> Unit, // 클릭 이벤트 콜백
    private val onItemPin: (Int) -> Unit, // 고정 이벤트 콜백
    private val onItemDelete: (Int) -> Unit // 삭제 이벤트 콜백
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    // ViewHolder 클래스
    inner class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val profileImage: ImageView = view.findViewById(R.id.image_profile)
        private val nicknameText: TextView = view.findViewById(R.id.text_nickname)
        private val timeText: TextView = view.findViewById(R.id.text_time)
        private val messageText: TextView = view.findViewById(R.id.text_message)
        private val unreadBadge: TextView = view.findViewById(R.id.badge_unread)

        // 데이터 바인딩
        fun bind(item: ChatItem) {
            nicknameText.text = item.nickname
            timeText.text = item.time
            messageText.text = item.lastMessage

            // 뱃지 표시
            when {
                item.isNew -> {
                    unreadBadge.text = "!"
                    unreadBadge.visibility = View.VISIBLE
                }
                item.unreadCount > 0 -> {
                    unreadBadge.text =
                        if (item.unreadCount > 100) "100+" else item.unreadCount.toString()
                    unreadBadge.visibility = View.VISIBLE
                }
                else -> {
                    unreadBadge.visibility = View.GONE
                }
            }

            // 아이템 클릭 리스너
            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    // ViewHolder 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    // 아이템 개수
    override fun getItemCount(): Int = items.size

    // ViewHolder에 데이터 바인딩
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        if (position in items.indices) {
            holder.bind(items[position])
        }
    }

    // 채팅 상단 고정
    fun pinItem(position: Int) {
        if (position !in items.indices) return
        val item = items.removeAt(position).copy(isPinned = true)
        items.add(0, item)
        notifyItemMoved(position, 0)
        notifyItemChanged(0)
    }

    // 채팅 삭제
    fun deleteItem(position: Int) {
        if (position !in items.indices) return
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    // 새로운 채팅 추가 (ID 중복 여부와 상관없이 추가)
    fun insertNewChatItem(item: ChatItem) {
        items.add(0, item)
        notifyItemInserted(0)
    }

    // 마지막 메시지 업데이트
    fun updateLastMessage(chatRoomId: String, message: String, time: String) {
        val index = items.indexOfFirst { it.id == chatRoomId }
        if (index != -1) {
            val old = items[index]
            items[index] = old.copy(lastMessage = message, time = time)
            notifyItemChanged(index)
        }
    }

    // 전체 리스트 갱신
    fun updateList(newList: List<ChatItem>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}
