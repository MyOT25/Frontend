package com.example.myot.Chatting

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.Chatting.ChatRoom.ChatRoomActivity
import com.example.myot.Chatting.ChattingList.ChatItem
import com.example.myot.Chatting.ChattingList.ChatListAdapter
import com.example.myot.Chatting.NewChat.NewChatActivity
import com.example.myot.R

class ChatFragment : Fragment() {

    private lateinit var chatCountText: TextView
    private lateinit var searchButton: ImageButton
    private lateinit var newChatButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatListAdapter

    private val dummyChats = mutableListOf(
        ChatItem("1", "다람쥐", "안녕하세요~~", "방금", 1),
        ChatItem("2", "다람쥐", "안녕하세요안녕하세요안녕하세요...", "1분 전", 105),
        ChatItem("3", "다람쥐", "안녕하세요~~", "10분 전", 0),
        ChatItem("4", "다람쥐", "사진을 보냈습니다.", "1시간 전", 0)
    )

    private lateinit var newChatLauncher: ActivityResultLauncher<Intent>

    private var pinIconRect: Rect? = null
    private var deleteIconRect: Rect? = null
    private var swipedPosition: Int = RecyclerView.NO_POSITION
    private var isSwipedState: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        newChatLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val nickname = result.data?.getStringExtra("userNickname")
                nickname?.let {
                    val newChat = ChatItem("new", it, "새로운 대화 시작", "방금", 0)
                    dummyChats.add(0, newChat)
                    adapter.notifyItemInserted(0)
                    recyclerView.scrollToPosition(0)
                    chatCountText.text = "${dummyChats.size}개의 채팅"
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        chatCountText  = view.findViewById(R.id.chat_count_text)
        searchButton   = view.findViewById(R.id.btn_search)
        newChatButton  = view.findViewById(R.id.btn_new_chat)
        recyclerView   = view.findViewById(R.id.chat_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        searchButton.setOnClickListener {
            Toast.makeText(context, "검색 클릭됨", Toast.LENGTH_SHORT).show()
        }
        newChatButton.setOnClickListener {
            val intent = Intent(requireContext(), NewChatActivity::class.java)
            newChatLauncher.launch(intent)
        }
        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ChatListAdapter(
            dummyChats,
            onItemClick = { chatItem ->
                val intent = Intent(requireContext(), ChatRoomActivity::class.java).apply {
                    putExtra("userNickname", chatItem.nickname)
                }
                startActivity(intent)
            },
            onItemPin    = { pos -> adapter.pinItem(pos) },
            onItemDelete = { pos -> adapter.deleteItem(pos) }
        )
        recyclerView.adapter = adapter

        val touchHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun getSwipeDirs(
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder
            ): Int {
                return if (isSwipedState && vh.adapterPosition == swipedPosition) {
                    ItemTouchHelper.RIGHT
                } else {
                    ItemTouchHelper.LEFT
                }
            }

            override fun onMove(
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                if (direction == ItemTouchHelper.LEFT) {
                    swipedPosition = vh.adapterPosition
                    isSwipedState = true
                    adapter.notifyItemChanged(swipedPosition)
                } else if (direction == ItemTouchHelper.RIGHT
                    && isSwipedState && vh.adapterPosition == swipedPosition
                ) {
                    adapter.notifyItemChanged(swipedPosition)
                    resetSwiped()
                }
            }

            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = 0.3f

            override fun onChildDraw(
                c: Canvas,
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = vh.itemView

                val iconFix    = ContextCompat.getDrawable(requireContext(),
                    R.drawable.ic_chatting_fix
                )!!
                val iconDelete = ContextCompat.getDrawable(requireContext(),
                    R.drawable.ic_chatting_delete
                )!!
                val background = ColorDrawable(Color.parseColor("#EEEEEE"))

                val iconMargin = (itemView.height - iconFix.intrinsicHeight) / 2
                val iconSize   = iconFix.intrinsicWidth
                val iconSpacing= 20                        // 아이콘 간격을 조금 줄임
                val rightEdge  = itemView.right - iconMargin

                val swipingOpen = !isSwipedState && dX < 0 && isCurrentlyActive
                val swipedOpen  = isSwipedState && vh.adapterPosition == swipedPosition

                if (swipingOpen || swipedOpen) {
                    // 1) 배경
                    background.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                    background.draw(c)

                    // 2) 삭제 휴지통 (가장 오른쪽)
                    val delRight = rightEdge
                    val delLeft  = delRight - iconSize
                    val top      = itemView.top + iconMargin
                    val bottom   = top + iconFix.intrinsicHeight
                    iconDelete.setBounds(delLeft, top, delRight, bottom)
                    iconDelete.draw(c)
                    deleteIconRect = Rect(delLeft, top, delRight, bottom)

                    // 3) 고정 핀 (삭제 왼쪽에)
                    val pinRight = delLeft - iconSpacing
                    val pinLeft  = pinRight - iconSize
                    iconFix.setBounds(pinLeft, top, pinRight, bottom)
                    iconFix.draw(c)
                    pinIconRect = Rect(pinLeft, top, pinRight, bottom)

                    // 4) 뷰 translate
                    val translateX = if (swipedOpen) {
                        -1f * (iconSize * 2 + iconSpacing + iconMargin)
                    } else {
                        dX
                    }
                    super.onChildDraw(c, rv, vh, translateX, dY, actionState, isCurrentlyActive)
                } else {
                    super.onChildDraw(c, rv, vh, 0f,      dY, actionState, isCurrentlyActive)
                }
            }
        })
        touchHelper.attachToRecyclerView(recyclerView)

        // 터치 업 시 아이콘 클릭 처리 (컨슘되지 않으므로 백 제스처 주의)
        recyclerView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP
                && swipedPosition != RecyclerView.NO_POSITION
            ) {
                val x = event.x.toInt()
                val y = event.y.toInt()
                when {
                    pinIconRect?.contains(x, y) == true -> {
                        adapter.pinItem(swipedPosition)
                        resetSwiped()
                    }
                    deleteIconRect?.contains(x, y) == true -> {
                        adapter.deleteItem(swipedPosition)
                        resetSwiped()
                    }
                }
            }
            // 터치 완전 소비하지 않으면 뒤로가기 제스처가 동작할 수 있음
            false
        }

        chatCountText.text = "${dummyChats.size}개의 채팅"
    }

    private fun resetSwiped() {
        recyclerView.post {
            adapter.notifyItemChanged(swipedPosition)
            swipedPosition = RecyclerView.NO_POSITION
            isSwipedState = false
        }
    }
}
