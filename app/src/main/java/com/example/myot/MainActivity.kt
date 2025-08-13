package com.example.myot

import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.chatting.ChatFragment
import com.example.myot.databinding.ActivityMainBinding
import com.example.myot.home.HomeFragment
import com.example.myot.question.ui.QuestionFragment
import com.example.myot.ticket.ui.TicketFragment
import dagger.hilt.android.AndroidEntryPoint
import com.example.myot.notification.NotificationAdapter
import com.example.myot.notification.NotificationItem
import com.example.myot.retrofit2.AuthStore

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var imeVisibleFlag: Boolean = false
    private var commentTextWatcher: TextWatcher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AuthStore.accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjExMywibG9naW5JZCI6Im11bXUiLCJpYXQiOjE3NTQ5MDEwMTUsImV4cCI6MTc1NTUwNTgxNX0.zatWNXS5KSZUkRP67bDAamWmOzESAQC1aeMHhQPRDeY"

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 네비게이션 바 종류에 맞게 바텀 네비 크기 변경
        adjustBottomNavMargin()

        // 시스템 상단/하단 바까지 화면 처리
        setTransparentSystemBars()

        binding.bottomNavigationView.itemIconTintList = null

        selectTab(R.id.menu_home)

        binding.bottomNavigationView.setOnItemSelectedListener {
            selectTab(it.itemId)
            true
        }

        // 질문 댓글창 기능
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 키보드가 떠 있으면 먼저 내리고 끝
                if (imeVisibleFlag) {
                    hideKeyboardAndClearFocus()
                    return
                }
                // 댓글바 열려 있으면 닫고 끝
                if (binding.commentBar.root.visibility == View.VISIBLE) {
                    hideCommentBar()
                    return
                }
                // 그 외에는 기존 back 동작 수행
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
                isEnabled = true
            }
        })

        // 알림창 기능
        val topContainer = findViewById<View>(R.id.top_bar)
        val alarmBtn = findViewById<ImageView>(R.id.iv_notification)
        val bg = findViewById<View>(R.id.iv_top_bg)

        val dummyList = mutableListOf(
            NotificationItem(1, "user1, user2 님 외 여러 명이 회원님의 피드를 좋아합니다.", R.drawable.ic_profile_over, true),
            NotificationItem(2, "user2 님이 회원님의 피드에 댓글을 남겼습니다.", R.drawable.ic_profile_outline, false),
            NotificationItem(3, "user2 님이 회원님의 피드를 리포스트 했습니다.", R.drawable.ic_profile_outline, false)
        )

        fun updateAlarmIcon() {
            val hasNew = dummyList.any { it.isNew }
            alarmBtn.setImageResource(
                if (hasNew) R.drawable.ic_alarm_new else R.drawable.ic_alarm_no
            )
        }

        var isDown = false

        topContainer.post {
            val bgHeight = bg.height.toFloat()
            val topBarHeight = topContainer.height.toFloat()

            val hideY = -(topBarHeight - bgHeight + 10.dpToPx())
            val showY = 0f

            topContainer.translationY = hideY

            alarmBtn.setOnClickListener {
                val goingDown = !isDown
                val targetY = if (goingDown) showY else hideY

                topContainer.animate()
                    .translationY(targetY)
                    .setDuration(300)
                    .withEndAction {
                        isDown = goingDown
                        if (isDown) {
                            updateAlarmIcon()
                        } else {
                            alarmBtn.setImageResource(R.drawable.ic_alarm)
                        }
                    }
                    .start()
            }
        }

        val rv = findViewById<RecyclerView>(R.id.rv_notifications).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        lateinit var adapter: NotificationAdapter
        adapter = NotificationAdapter(dummyList) { pos ->

            val item = dummyList.getOrNull(pos) ?: return@NotificationAdapter
            if (item.isNew) {
                item.isNew = false
                adapter.notifyItemChanged(pos)
                if (isDown) updateAlarmIcon()
            }
        }
        rv.adapter = adapter
    }


    private fun adjustBottomNavMargin() {
        val bottomNav = binding.bottomNavigationView
        val params = bottomNav.layoutParams as ViewGroup.MarginLayoutParams

        if (isGestureNavigation()) {
            // 제스처 내비게이션일 때
            params.bottomMargin = (-17).dpToPx()
            params.height = 90.dpToPx()
        } else {
            // 버튼 내비게이션일 때
            params.bottomMargin = (-7).dpToPx()
            params.height = 110.dpToPx()
        }

        bottomNav.layoutParams = params
    }



    private fun isGestureNavigation(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Settings.Secure.getInt(contentResolver, "navigation_mode", 0) == 2
        } else {
            false
        }
    }

    private fun Int.dpToPx(): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()
    }


    @Suppress("DEPRECATION", "NewApi")
    private fun setTransparentSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)

            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT

        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    )
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
        }
    }

    // 하단 네비게이션 프래그먼트 전환 및 아이콘 변경
    private fun selectTab(menuId: Int) {
        val fragment = when (menuId) {
            R.id.menu_home -> HomeFragment()
            R.id.menu_search -> SearchFragment()
            R.id.menu_ticket -> TicketFragment()
            R.id.menu_question -> QuestionFragment()
            R.id.menu_chat -> ChatFragment()
            else -> HomeFragment()
        }

        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainerView.id, fragment)
            .commit()

        val menu = binding.bottomNavigationView.menu
        menu.findItem(R.id.menu_home).setIcon(
            if (menuId == R.id.menu_home) R.drawable.ic_nav_home_selected else R.drawable.ic_nav_home_unselected
        )
        menu.findItem(R.id.menu_search).setIcon(
            if (menuId == R.id.menu_search) R.drawable.ic_nav_search_selected else R.drawable.ic_nav_search_unselected
        )
        menu.findItem(R.id.menu_ticket).setIcon(
            if (menuId == R.id.menu_ticket) R.drawable.ic_nav_ticket_selected else R.drawable.ic_nav_ticket_unselected
        )
        menu.findItem(R.id.menu_question).setIcon(
            if (menuId == R.id.menu_question) R.drawable.ic_nav_question_selected else R.drawable.ic_nav_question_unselected
        )
        menu.findItem(R.id.menu_chat).setIcon(
            if (menuId == R.id.menu_chat) R.drawable.ic_nav_chat_selected else R.drawable.ic_nav_chat_unselected
        )
    }

    fun showCommentBar(
        scrollable: View,
        hint: String = "댓글을 입력하세요",
        onSend: (String, Boolean) -> Unit
    ) {
        // 바텀 네비 숨기고 댓글바/스페이서 보이기
        binding.bottomNavigationView.visibility = View.INVISIBLE
        binding.bottomNavigationLine.visibility = View.INVISIBLE

        binding.commentBar.root.visibility = View.VISIBLE
        binding.commentBar.root.bringToFront()
        binding.commentBar.root.translationZ = 24f

        binding.commentBottomSpacer.visibility = View.VISIBLE
        binding.commentBottomSpacer.bringToFront()
        binding.commentBottomSpacer.translationZ = 23f

        // 힌트/커서(깜빡임) 끄기
        binding.commentBar.etComment.hint = null
        binding.commentBar.etComment.clearFocus()
        binding.commentBar.etComment.isCursorVisible = false
        binding.commentBar.etComment.setOnFocusChangeListener { v, has ->
            (v as android.widget.EditText).isCursorVisible = has
        }
        binding.commentBar.etComment.setOnClickListener {
            it.requestFocus()
        }

        var isAnonymous = false
        fun applyProfileIcon() {
            binding.commentBar.ivProfile.setImageResource(
                if (isAnonymous) R.drawable.ic_profile_anonymous else R.drawable.ic_profile
            )
        }

        binding.commentBar.btnSend.setOnClickListener {
            val text = binding.commentBar.etComment.text.toString().trim()
            if (text.isNotEmpty()) {
                onSend(text, isAnonymous)
                binding.commentBar.etComment.setText("")
            }
        }
        binding.commentBar.etComment.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                binding.commentBar.btnSend.performClick(); true
            } else false
        }

        registerImeLift(
            targetBar = binding.commentBar.root,
            scrollable = scrollable,
            spacer = binding.commentBottomSpacer
        )

        commentTextWatcher?.let { binding.commentBar.etComment.removeTextChangedListener(it) }
        commentTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val hasText = !s.isNullOrBlank()
                binding.commentBar.btnSend.setImageResource(
                    if (hasText) R.drawable.ic_comment_send_on else R.drawable.ic_comment_send
                )
            }
        }.also { binding.commentBar.etComment.addTextChangedListener(it) }

        binding.commentBar.btnSend.setImageResource(R.drawable.ic_comment_send)

        applyProfileIcon()
        binding.commentBar.ivProfile.setOnClickListener {
            isAnonymous = !isAnonymous
            applyProfileIcon()
        }
    }

    fun hideCommentBar() {
        binding.commentBar.root.visibility = View.GONE
        binding.commentBottomSpacer.visibility = View.GONE
        binding.bottomNavigationView.visibility = View.VISIBLE
        binding.bottomNavigationLine.visibility = View.VISIBLE
        // 스크롤 하단 패딩 원복
        (currentFocus ?: binding.fragmentContainerView).updatePadding(bottom = 0)

        commentTextWatcher?.let {
            binding.commentBar.etComment.removeTextChangedListener(it)
            commentTextWatcher = null
        }
    }

    private fun registerImeLift(targetBar: View, scrollable: View?, spacer: View) {
        val content = findViewById<View>(android.R.id.content)

        fun applyInsets(insets: androidx.core.view.WindowInsetsCompat) {
            val imeBottom = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.ime()).bottom
            val imeVisible = imeBottom > 0

            imeVisibleFlag = imeVisible
            targetBar.translationY = -imeBottom.toFloat()
            spacer.visibility = if (imeVisible) View.GONE else View.VISIBLE
            val spacerH = if (spacer.visibility == View.VISIBLE) spacer.height else 0
            scrollable?.updatePadding(bottom = targetBar.height + spacerH)
        }

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(content) { _, insets ->
            applyInsets(insets); insets
        }
        androidx.core.view.ViewCompat.setWindowInsetsAnimationCallback(
            content,
            object : androidx.core.view.WindowInsetsAnimationCompat.Callback(
                androidx.core.view.WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE
            ) {
                override fun onProgress(
                    insets: androidx.core.view.WindowInsetsCompat,
                    running: MutableList<androidx.core.view.WindowInsetsAnimationCompat>
                ): androidx.core.view.WindowInsetsCompat {
                    applyInsets(insets); return insets
                }
            }
        )
        targetBar.post {
            val spacerH = if (spacer.visibility == View.VISIBLE) spacer.height else 0
            scrollable?.updatePadding(bottom = targetBar.height + spacerH)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN && imeVisibleFlag) {
            // 댓글바 영역 안을 탭했는지 체크 (안에서 탭이면 무시)
            val bar = binding.commentBar.root
            if (bar.visibility == View.VISIBLE) {
                val barRect = Rect()
                bar.getGlobalVisibleRect(barRect)
                // 댓글바 밖을 탭했으면 키보드 내림
                if (!barRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    hideKeyboardAndClearFocus()
                    // 탭 이벤트는 소비해서 다른 클릭이 실행되지 않도록
                    return true
                }
            } else {
                // 댓글바가 없어도, 포커스된 EditText가 있고 그 밖을 탭하면 키보드 내림
                val focused = currentFocus
                if (focused is android.widget.EditText) {
                    val r = Rect()
                    focused.getGlobalVisibleRect(r)
                    if (!r.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                        hideKeyboardAndClearFocus()
                        return true
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun hideKeyboardAndClearFocus() {
        // 포커스 지우고 커서 비표시
        binding.commentBar.etComment.clearFocus()
        binding.commentBar.etComment.isCursorVisible = false

        val view = currentFocus ?: binding.fragmentContainerView
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }



}
