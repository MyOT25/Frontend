package com.example.myot.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.R

/**
 * 채팅 메시지 데이터 모델
 *
 * @param senderId 메시지 보낸 사람의 고유 ID
 * @param content 메시지 본문 텍스트
 * @param profileUrl 프로필 이미지 URL (null 이면 기본 이미지 사용)
 */
data class ChatMessage(
    val senderId: String,
    val content: String,
    val profileUrl: String? = null
)

/**
 * RecyclerView.Adapter
 * - 같은 발화자 메시지를 묶어서
 * - 그룹의 마지막 메시지에만 꼬리(tail)가 붙도록 4개 뷰 타입으로 분기
 */
class ChatMessageAdapter(
    private val messageList: List<ChatMessage>, // 보여줄 메시지 목록
    private val currentUserId: String            // 현재 사용자 ID
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_LEFT        = 0  // 상대방, 꼬리 없음
        private const val VIEW_TYPE_LEFT_TAIL   = 1  // 상대방, 꼬리 있음
        private const val VIEW_TYPE_RIGHT       = 2  // 내 메시지, 꼬리 없음
        private const val VIEW_TYPE_RIGHT_TAIL  = 3  // 내 메시지, 꼬리 있음
    }

    /**
     * 총 메시지 개수 반환
     */
    override fun getItemCount(): Int = messageList.size

    /**
     * 각 position 의 뷰 타입 결정
     * - 같은 발화자의 메시지 그룹 마지막이면 “TAIL” 타입 반환
     */
    override fun getItemViewType(position: Int): Int {
        val msg = messageList[position]
        val isMe = msg.senderId == currentUserId
        // 그룹 마지막인지: 마지막 아이템이거나 다음 메시지 발화자와 다를 때
        val isLastInGroup = position == messageList.lastIndex ||
                messageList[position + 1].senderId != msg.senderId

        return when {
            !isMe && isLastInGroup -> VIEW_TYPE_LEFT_TAIL
            !isMe                  -> VIEW_TYPE_LEFT
            isMe  && isLastInGroup -> VIEW_TYPE_RIGHT_TAIL
            else                    -> VIEW_TYPE_RIGHT
        }
    }

    /**
     * 뷰 타입에 따라 알맞은 레이아웃(item_chat_...)을 inflate
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_LEFT_TAIL -> {
                val view = inf.inflate(R.layout.item_chat_left_tail, parent, false)
                LeftTailViewHolder(view)
            }
            VIEW_TYPE_LEFT -> {
                val view = inf.inflate(R.layout.item_chat_left, parent, false)
                LeftViewHolder(view)
            }
            VIEW_TYPE_RIGHT_TAIL -> {
                val view = inf.inflate(R.layout.item_chat_right_tail, parent, false)
                RightTailViewHolder(view)
            }
            else -> {
                val view = inf.inflate(R.layout.item_chat_right, parent, false)
                RightViewHolder(view)
            }
        }
    }

    /**
     * ViewHolder 에 메시지 데이터 바인딩
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messageList[position]
        when (holder) {
            is LeftViewHolder      -> holder.bind(msg)
            is LeftTailViewHolder  -> holder.bind(msg)
            is RightViewHolder     -> holder.bind(msg)
            is RightTailViewHolder -> holder.bind(msg)
        }
    }

    /** 상대방 일반 메시지 ViewHolder */
    inner class LeftViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivProfile = view.findViewById<ImageView>(R.id.ivProfile)
        private val tvMessage = view.findViewById<TextView>(R.id.tvMessage)

        fun bind(msg: ChatMessage) {
            tvMessage.text = msg.content
            // TODO: Glide/Coil 등을 사용해 ivProfile 로드
        }
    }

    /** 상대방 꼬리 메시지 ViewHolder */
    inner class LeftTailViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivProfile = view.findViewById<ImageView>(R.id.ivProfile)
        private val tvMessage = view.findViewById<TextView>(R.id.tvMessage)
        private val ivTail    = view.findViewById<ImageView>(R.id.ivTail)

        fun bind(msg: ChatMessage) {
            tvMessage.text = msg.content
            // TODO: 프로필 로드
            // ivTail 는 XML 에서 tail 이미지를 지정해 두었습니다.
        }
    }

    /** 내 일반 메시지 ViewHolder */
    inner class RightViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivProfile = view.findViewById<ImageView>(R.id.ivProfile)
        private val tvMessage = view.findViewById<TextView>(R.id.tvMessage)

        fun bind(msg: ChatMessage) {
            tvMessage.text = msg.content
            // TODO: 내 프로필 이미지 설정
        }
    }

    /** 내 꼬리 메시지 ViewHolder */
    inner class RightTailViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivProfile = view.findViewById<ImageView>(R.id.ivProfile)
        private val tvMessage = view.findViewById<TextView>(R.id.tvMessage)
        private val ivTail    = view.findViewById<ImageView>(R.id.ivTail)

        fun bind(msg: ChatMessage) {
            tvMessage.text = msg.content
            // TODO: 내 프로필 설정
            // ivTail 이미지는 XML 에서 지정됩니다.
        }
    }
}
