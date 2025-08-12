package com.example.myot.chatting

import android.annotation.SuppressLint
import android.app.Activity
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
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.R
import com.example.myot.chatting.chatroom.ChatRoomActivity
import com.example.myot.chatting.chattinglist.ChatItem
import com.example.myot.chatting.chattinglist.ChatListAdapter
import com.example.myot.chatting.newchat.NewChatActivity

// 채팅 목록을 표시하는 프래그먼트
class ChatFragment : Fragment() {

    // ─── 뷰 및 어댑터 변수 ──────────────────────────
    private lateinit var chatCountText: TextView
    private lateinit var searchButton: ImageButton
    private lateinit var newChatButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatListAdapter

    // ViewModel을 Activity 범위로 공유
    private val chatViewModel: ChatViewModel by activityViewModels()

    // ─── 액티비티 결과 처리 런처 ────────────────────
    private lateinit var newChatLauncher: ActivityResultLauncher<Intent>
    private lateinit var chatRoomLauncher: ActivityResultLauncher<Intent>

    // ─── 스와이프 아이콘 영역 및 상태 관리 ─────────────
    private var pinIconRect: Rect? = null
    private var deleteIconRect: Rect? = null
    private var swipedPosition: Int = RecyclerView.NO_POSITION
    private var isSwipedState: Boolean = false

    // 프래그먼트 생성 시 초기화
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 새 채팅방 생성 후 처리
        newChatLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val nickname = result.data?.getStringExtra("userNickname")
                nickname?.let {
                    val newChat = ChatItem("new", it, "새로운 대화 시작", "방금", 0)
                    chatViewModel.addChat(newChat)
                }
            }
        }

        // 채팅방에서 돌아온 후 마지막 메시지, 시간, 읽음 처리 반영
        chatRoomLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val chatRoomId = result.data?.getStringExtra("chatRoomId")
                val lastMessage = result.data?.getStringExtra("lastMessage")
                val lastMessageTime = result.data?.getStringExtra("lastMessageTime")
                val isRead = result.data?.getBooleanExtra("read", false) ?: false

                if (chatRoomId != null) {
                    chatViewModel.updateChat(chatRoomId, lastMessage, lastMessageTime, isRead)
                }
            }
        }
    }

    // 뷰 생성 및 버튼 초기화
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        chatCountText = view.findViewById(R.id.chat_count_text)
        searchButton = view.findViewById(R.id.btn_search)
        newChatButton = view.findViewById(R.id.btn_new_chat)
        recyclerView = view.findViewById(R.id.chat_recycler_view)
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

    // 뷰 생성 후 로직 처리
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 어댑터 초기화
        adapter = ChatListAdapter(
            mutableListOf(),
            onItemClick = { chatItem ->
                val intent = Intent(requireContext(), ChatRoomActivity::class.java).apply {
                    putExtra("chatRoomId", chatItem.id)
                    putExtra("userId", chatItem.id)
                    putExtra("userNickname", chatItem.nickname)
                }
                chatRoomLauncher.launch(intent)
            },
            onItemPin = { pos -> chatViewModel.pinChat(pos) },
            onItemDelete = { pos -> chatViewModel.deleteChat(pos) }
        )
        recyclerView.adapter = adapter

        // ViewModel 데이터 관찰
        chatViewModel.chatList.observe(viewLifecycleOwner) { updatedList ->
            adapter.updateList(updatedList)
            chatCountText.text = "${updatedList.size}개의 채팅"
        }

        // 스와이프 처리
        val touchHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun getSwipeDirs(rv: RecyclerView, vh: RecyclerView.ViewHolder): Int {
                return if (isSwipedState && vh.adapterPosition == swipedPosition) {
                    ItemTouchHelper.RIGHT
                } else {
                    ItemTouchHelper.LEFT
                }
            }

            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                if (direction == ItemTouchHelper.LEFT) {
                    swipedPosition = vh.adapterPosition
                    isSwipedState = true
                    adapter.notifyItemChanged(swipedPosition)
                } else if (direction == ItemTouchHelper.RIGHT && isSwipedState && vh.adapterPosition == swipedPosition) {
                    adapter.notifyItemChanged(swipedPosition)
                    resetSwiped()
                }
            }

            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = 0.3f

            override fun onChildDraw(
                c: Canvas, rv: RecyclerView, vh: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                val itemView = vh.itemView

                val iconFix = ContextCompat.getDrawable(requireContext(), R.drawable.ic_chatting_fix)!!
                val iconDelete = ContextCompat.getDrawable(requireContext(), R.drawable.ic_chatting_delete)!!
                val background = ColorDrawable(Color.parseColor("#EEEEEE"))

                val iconMargin = (itemView.height - iconFix.intrinsicHeight) / 2
                val iconSize = iconFix.intrinsicWidth
                val iconSpacing = 20
                val rightEdge = itemView.right - iconMargin

                val swipingOpen = !isSwipedState && dX < 0 && isCurrentlyActive
                val swipedOpen = isSwipedState && vh.adapterPosition == swipedPosition

                if (swipingOpen || swipedOpen) {
                    background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                    background.draw(c)

                    val delRight = rightEdge
                    val delLeft = delRight - iconSize
                    val top = itemView.top + iconMargin
                    val bottom = top + iconFix.intrinsicHeight
                    iconDelete.setBounds(delLeft, top, delRight, bottom)
                    iconDelete.draw(c)
                    deleteIconRect = Rect(delLeft, top, delRight, bottom)

                    val pinRight = delLeft - iconSpacing
                    val pinLeft = pinRight - iconSize
                    iconFix.setBounds(pinLeft, top, pinRight, bottom)
                    iconFix.draw(c)
                    pinIconRect = Rect(pinLeft, top, pinRight, bottom)

                    val translateX = if (swipedOpen) {
                        -1f * (iconSize * 2 + iconSpacing + iconMargin)
                    } else {
                        dX
                    }
                    super.onChildDraw(c, rv, vh, translateX, dY, actionState, isCurrentlyActive)
                } else {
                    super.onChildDraw(c, rv, vh, 0f, dY, actionState, isCurrentlyActive)
                }
            }
        })
        touchHelper.attachToRecyclerView(recyclerView)

        // 아이콘 클릭 처리
        recyclerView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP && swipedPosition != RecyclerView.NO_POSITION) {
                val x = event.x.toInt()
                val y = event.y.toInt()
                when {
                    pinIconRect?.contains(x, y) == true -> {
                        chatViewModel.pinChat(swipedPosition)
                        resetSwiped()
                    }
                    deleteIconRect?.contains(x, y) == true -> {
                        chatViewModel.deleteChat(swipedPosition)
                        resetSwiped()
                    }
                }
            }
            false
        }
    }

    // ────────────────────────────────────────────────
    // 스와이프 상태 초기화
    private fun resetSwiped() {
        recyclerView.post {
            adapter.notifyItemChanged(swipedPosition)
            swipedPosition = RecyclerView.NO_POSITION
            isSwipedState = false
        }
    }
}
