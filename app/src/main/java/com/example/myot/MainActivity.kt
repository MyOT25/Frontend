package com.example.myot

import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.example.myot.chatting.ChatFragment
import com.example.myot.databinding.ActivityMainBinding
import com.example.myot.home.HomeFragment
import com.example.myot.question.ui.QuestionFragment
import com.example.myot.ticket.ui.TicketFragment

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
        topBar = findViewById(R.id.top_bar)
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
