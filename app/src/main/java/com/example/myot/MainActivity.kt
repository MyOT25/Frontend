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
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
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
import com.example.myot.retrofit2.TokenStore
import com.google.android.material.navigation.NavigationView

// 드로어 각 화면 프래그먼트 import
import com.example.myot.drawer.TicketMarkFragment
import com.example.myot.drawer.communitymanager.CommunityManagerFragment
import com.example.myot.drawer.NotificationFragment
import com.example.myot.drawer.SubscribeFragment
import com.example.myot.drawer.SettingFragment
import com.example.myot.drawer.CustomerCenterFragment
import com.example.myot.search.SearchFragment

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var imeVisibleFlag: Boolean = false
    private var commentTextWatcher: TextWatcher? = null

    // 드로어 관련 변수
    private lateinit var topBar: View
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 액세스 토큰 로드
        AuthStore.accessToken = TokenStore.loadAccessToken(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 네비게이션 바 종류에 맞게 바텀 네비 크기 변경
        adjustBottomNavMargin()

        // 시스템 상단/하단 바 투명 처리
        setTransparentSystemBars()

        // 하단 네비 아이콘 기본 색상 제거
        binding.bottomNavigationView.itemIconTintList = null

        // 초기 탭은 홈으로 설정
        selectTab(R.id.menu_home)

        // 하단 네비게이션 아이템 클릭 이벤트 처리
        binding.bottomNavigationView.setOnItemSelectedListener {
            selectTab(it.itemId)
            true
        }

        // 햄버거 메뉴 버튼 클릭 시 드로어 열기
        drawerLayout = findViewById(R.id.drawer_layout)
        val profileButton = findViewById<ImageView>(R.id.iv_profile)
        profileButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // 네비게이션 드로어 메뉴 클릭 리스너 등록
        binding.navView.setNavigationItemSelectedListener(navigationItemSelectedListener)

        // 뒤로가기 버튼 처리 (댓글창, 키보드 상태 관리 + 드로어 닫기)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    return
                }
                if (imeVisibleFlag) {
                    hideKeyboardAndClearFocus()
                    return
                }
                if (binding.commentBar.root.visibility == View.VISIBLE) {
                    hideCommentBar()
                    return
                }
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
                isEnabled = true
            }
        })

        // 알림창 관련 뷰
        val topContainer = findViewById<View>(R.id.top_bar)
        val alarmBtn = findViewById<ImageView>(R.id.iv_notification)
        val bg = findViewById<View>(R.id.iv_top_bg)

        // 더미 알림 리스트
        val dummyList = mutableListOf<NotificationItem>()

        // 알림 아이콘 상태 업데이트
        fun updateAlarmIcon() {
            val hasNew = dummyList.any { it.isNew }
            alarmBtn.setImageResource(
                if (hasNew) R.drawable.ic_alarm_new else R.drawable.ic_alarm_no
            )
        }

        var isDown = false

        // 알림창 애니메이션 처리
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

        // 알림 RecyclerView 설정
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

    // 드로어 메뉴 클릭 이벤트 처리
    private val navigationItemSelectedListener =
        NavigationView.OnNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            when (menuItem.itemId) {
                R.id.nav_ticket -> {
                    replaceFragment(TicketMarkFragment(), addToBackStack = false)
                }
                R.id.nav_community -> {
                    replaceFragment(CommunityManagerFragment(), addToBackStack = false)
                }
                R.id.nav_notice -> {
                    replaceFragment(NotificationFragment(), addToBackStack = false)
                }
                R.id.nav_subscribe -> {
                    replaceFragment(SubscribeFragment(), addToBackStack = false)
                }
                R.id.nav_setting -> {
                    replaceFragment(SettingFragment(), addToBackStack = false)
                }
                R.id.nav_support -> {
                    replaceFragment(CustomerCenterFragment(), addToBackStack = false)
                }
                else -> {
                    toastWip("준비 중입니다.")
                }
            }
            true
        }

    // 프래그먼트 교체 공통 함수
    private fun replaceFragment(fragment: Fragment, addToBackStack: Boolean) {
        val tx = supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainerView.id, fragment)
        if (addToBackStack) tx.addToBackStack(null)
        tx.commit()
    }

    // 네비게이션 종류에 따른 바텀 네비 마진 조정
    private fun adjustBottomNavMargin() {
        val bottomNav = binding.bottomNavigationView
        val params = bottomNav.layoutParams as ViewGroup.MarginLayoutParams

        if (isGestureNavigation()) {
            params.bottomMargin = (-17).dpToPx()
            params.height = 90.dpToPx()
        } else {
            params.bottomMargin = (-7).dpToPx()
            params.height = 110.dpToPx()
        }
        bottomNav.layoutParams = params
    }

    // 현재 기기의 내비게이션 방식 확인
    private fun isGestureNavigation(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Settings.Secure.getInt(contentResolver, "navigation_mode", 0) == 2
        } else {
            false
        }
    }

    // dp → px 변환
    private fun Int.dpToPx(): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()
    }

    // 시스템 상단/하단 바 투명 처리
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

    // 댓글창 보여주기
    fun showCommentBar(
        scrollable: View,
        hint: String = "댓글을 입력하세요",
        onSend: (String, Boolean) -> Unit,
        allowAnonymous: Boolean = true
    ) {
        binding.bottomNavigationView.visibility = View.INVISIBLE
        binding.bottomNavigationLine.visibility = View.INVISIBLE

        binding.commentBar.root.visibility = View.VISIBLE
        binding.commentBar.root.bringToFront()
        binding.commentBar.root.translationZ = 24f

        binding.commentBottomSpacer.visibility = View.VISIBLE
        binding.commentBottomSpacer.bringToFront()
        binding.commentBottomSpacer.translationZ = 23f

        // 입력창은 보이되 자동 포커스/자동 키보드는 열지 않는다.
        binding.commentBar.etComment.hint = hint
        binding.commentBar.etComment.clearFocus()
        binding.commentBar.etComment.isCursorVisible = false
        binding.commentBar.etComment.setOnFocusChangeListener { v, has ->
            (v as android.widget.EditText).isCursorVisible = has
        }
        binding.commentBar.etComment.setOnClickListener {
            it.requestFocus() // 눌렀을 때만 포커스 및 키보드
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(it, InputMethodManager.SHOW_IMPLICIT)
        }

        var isAnonymous = false
        fun applyProfileIcon() {
            if (allowAnonymous) {
                binding.commentBar.ivProfile.visibility = View.VISIBLE
                binding.commentBar.ivProfile.setImageResource(
                    if (isAnonymous) R.drawable.ic_profile_anonymous else R.drawable.ic_profile
                )
            } else {
                binding.commentBar.ivProfile.visibility = View.GONE
            }
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
            if (!allowAnonymous) return@setOnClickListener
            isAnonymous = !isAnonymous
            applyProfileIcon()
        }
    }

    // 댓글창 숨기기
    fun hideCommentBar() {
        binding.commentBar.root.visibility = View.GONE
        binding.commentBottomSpacer.visibility = View.GONE
        binding.bottomNavigationView.visibility = View.VISIBLE
        binding.bottomNavigationLine.visibility = View.VISIBLE
        (currentFocus ?: binding.fragmentContainerView).updatePadding(bottom = 0)

        commentTextWatcher?.let {
            binding.commentBar.etComment.removeTextChangedListener(it)
            commentTextWatcher = null
        }
    }

    // 키보드와 댓글창 동작 처리
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
                DISPATCH_MODE_CONTINUE_ON_SUBTREE
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

    // 터치 이벤트 처리 (댓글창 외부 터치 시 키보드 내리기)
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN && imeVisibleFlag) {
            val bar = binding.commentBar.root
            if (bar.visibility == View.VISIBLE) {
                val barRect = Rect()
                bar.getGlobalVisibleRect(barRect)
                if (!barRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    hideKeyboardAndClearFocus()
                    return true
                }
            } else {
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

    // 질문 탭 열기
    fun openQuestionTab() {
        binding.bottomNavigationView.selectedItemId = R.id.menu_question
    }

    // 키보드 내리기 및 포커스 해제
    fun hideKeyboardAndClearFocus() {
        binding.commentBar.etComment.clearFocus()
        binding.commentBar.etComment.isCursorVisible = false

        val view = currentFocus ?: binding.fragmentContainerView
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // 간단 토스트 표시
    private fun toastWip(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}

