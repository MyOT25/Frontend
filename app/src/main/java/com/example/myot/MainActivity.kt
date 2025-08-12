package com.example.myot

import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.provider.Settings
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
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

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var topBar: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 네비게이션 바 종류에 맞게 바텀 네비 크기 변경
        adjustBottomNavMargin()

        // 시스템 상단/하단 바까지 화면 처리
        setTransparentSystemBars()

        // 아이콘 tint 제거 (drawable 원본 색 유지)
        binding.bottomNavigationView.itemIconTintList = null

        selectTab(R.id.menu_home)

        binding.bottomNavigationView.setOnItemSelectedListener {
            selectTab(it.itemId)
            true
        }


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
}
