package com.example.myot.chatting

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
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
import com.example.myot.chatting.chatroomapi.ChatRoomListRetrofitInstance
import com.example.myot.chatting.chatroomapi.ChatRoomListResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatFragment : Fragment() {

    private lateinit var chatCountText: TextView
    private lateinit var searchButton: ImageButton
    private lateinit var newChatButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatListAdapter
    private lateinit var searchInput: EditText // 검색 입력창

    private val chatViewModel: ChatViewModel by activityViewModels()

    private lateinit var newChatLauncher: ActivityResultLauncher<Intent>
    private lateinit var chatRoomLauncher: ActivityResultLauncher<Intent>

    private var pinIconRect: Rect? = null
    private var deleteIconRect: Rect? = null
    private var swipedPosition: Int = RecyclerView.NO_POSITION
    private var isSwipedState: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 새 채팅방 생성 후 처리
        newChatLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val chatRoomId = result.data?.getStringExtra("chatRoomId")
                    val nickname = result.data?.getStringExtra("userNickname")
                    if (!chatRoomId.isNullOrEmpty() && !nickname.isNullOrEmpty()) {
                        val newChat = ChatItem(
                            id = chatRoomId,
                            nickname = nickname,
                            lastMessage = "새로운 대화 시작",
                            time = "방금",
                            unreadCount = 0
                        )
                        chatViewModel.addChat(newChat) // ViewModel에 추가
                    }
                }
            }

        // 채팅방에서 돌아온 후 마지막 메시지, 시간, 읽음 처리 반영
        chatRoomLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val chatRoomId = result.data?.getStringExtra("chatRoomId")
                    val lastMessage = result.data?.getStringExtra("lastMessage")
                    val lastMessageTime = result.data?.getStringExtra("lastMessageTime")
                    val isRead = result.data?.getBooleanExtra("read", false) ?: false

                    if (!chatRoomId.isNullOrEmpty()) {
                        chatViewModel.updateChat(chatRoomId, lastMessage, lastMessageTime, isRead)
                    }
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        chatCountText = view.findViewById(R.id.chat_count_text)
        searchButton = view.findViewById(R.id.btn_search)
        newChatButton = view.findViewById(R.id.btn_new_chat)
        recyclerView = view.findViewById(R.id.chat_recycler_view)
        searchInput = view.findViewById(R.id.chat_search_input)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 검색 버튼 클릭 시 검색창 표시 & 키보드 띄우기
        searchButton.setOnClickListener {
            searchInput.visibility = View.VISIBLE
            searchInput.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT)
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

        // LiveData를 관찰하여 리스트 갱신
        chatViewModel.chatList.observe(viewLifecycleOwner) { updatedList ->
            adapter.updateList(updatedList)
            chatCountText.text = "${updatedList.size}개의 채팅"
        }

        // 검색 입력 감지 (ID, 닉네임, 마지막 메시지 모두 검색 가능하도록 수정)
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                val filteredList = chatViewModel.chatList.value?.filter {
                    it.id.contains(query, ignoreCase = true) ||
                            it.nickname.contains(query, ignoreCase = true) ||
                            it.lastMessage.contains(query, ignoreCase = true) // 마지막 메시지까지 검색
                } ?: emptyList()
                adapter.updateList(filteredList.toMutableList())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // 채팅방 목록 조회
        loadChatRoomList()

        // 스와이프 기능
        val touchHelper = ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun getSwipeDirs(rv: RecyclerView, vh: RecyclerView.ViewHolder): Int {
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
                } else if (direction == ItemTouchHelper.RIGHT &&
                    isSwipedState &&
                    vh.adapterPosition == swipedPosition
                ) {
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

                val iconFix =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_chatting_fix)!!
                val iconDelete =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_chatting_delete)!!
                val background = ColorDrawable(Color.parseColor("#EEEEEE"))

                val iconMargin = (itemView.height - iconFix.intrinsicHeight) / 2
                val iconSize = iconFix.intrinsicWidth
                val iconSpacing = 20
                val rightEdge = itemView.right - iconMargin

                val swipingOpen = !isSwipedState && dX < 0 && isCurrentlyActive
                val swipedOpen = isSwipedState && vh.adapterPosition == swipedPosition

                if (swipingOpen || swipedOpen) {
                    background.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
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

    // 채팅방 목록을 서버에서 불러오는 함수
    private fun loadChatRoomList() {
        ChatRoomListRetrofitInstance.api.getChatRooms()
            .enqueue(object : Callback<ChatRoomListResponse> {
                override fun onResponse(
                    call: Call<ChatRoomListResponse>,
                    response: Response<ChatRoomListResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        val rooms = body?.success?.data ?: emptyList()
                        val items = rooms.map {
                            ChatItem(
                                id = it.chatRoomId.toString(),
                                nickname = it.name ?: "",
                                lastMessage = it.lastMessage ?: "",
                                time = "방금",
                                unreadCount = 0
                            )
                        }
                        chatViewModel.setChatList(items)
                    } else {
                        Log.e("ChatFragment", "API 응답 오류: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ChatRoomListResponse>, t: Throwable) {
                    Log.e("ChatFragment", "API 호출 실패: ${t.message}")
                }
            })
    }

    private fun resetSwiped() {
        recyclerView.post {
            adapter.notifyItemChanged(swipedPosition)
            swipedPosition = RecyclerView.NO_POSITION
            isSwipedState = false
        }
    }
}
