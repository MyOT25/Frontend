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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.myot.chatting.ChatFragment
import com.example.myot.databinding.ActivityMainBinding
import com.example.myot.question.ui.QuestionSearchFragment
import com.example.myot.question.ui.QuestionFragment
import com.google.android.material.navigation.NavigationView

// 드로어 각 화면 프래그먼트 import
import com.example.myot.drawer.TicketMarkFragment
import com.example.myot.drawer.communitymanager.CommunityManagerFragment
import com.example.myot.drawer.NotificationFragment
import com.example.myot.drawer.SubscribeFragment
import com.example.myot.drawer.SettingFragment
import com.example.myot.drawer.CustomerCenterFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding // ViewBinding 객체
    private lateinit var topBar: View // 상단바
    private lateinit var drawerLayout: DrawerLayout // 드로어 레이아웃

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 하단 네비게이션 마진 조정 (제스처/버튼 네비게이션에 맞게)
        adjustBottomNavMargin()

        // 상태바·네비게이션바 투명 처리
        setTransparentSystemBars()

        // 하단 네비 아이콘 tint 제거 (원본 색상 유지)
        binding.bottomNavigationView.itemIconTintList = null

        // 앱 최초 진입 시 홈 탭 선택
        selectTab(R.id.menu_home)

        // 하단 네비게이션 탭 선택 리스너
        binding.bottomNavigationView.setOnItemSelectedListener {
            selectTab(it.itemId)
            true
        }

        topBar = findViewById(R.id.top_bar)

        // 검색 화면 진입 시 상단바 숨김 / 나가면 표시
        supportFragmentManager.addOnBackStackChangedListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
            if (currentFragment is QuestionSearchFragment) {
                topBar.visibility = View.GONE
            } else {
                topBar.visibility = View.VISIBLE
            }
        }

        // 햄버거 메뉴 버튼 클릭 시 드로어 열기
        drawerLayout = findViewById(R.id.drawer_layout)
        val profileButton = findViewById<ImageView>(R.id.iv_profile)
        profileButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // 네비게이션 드로어 메뉴 클릭 리스너 등록
        binding.navView.setNavigationItemSelectedListener(navigationItemSelectedListener)
    }

    // 드로어 메뉴 클릭 이벤트 처리
    private val navigationItemSelectedListener =
        NavigationView.OnNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true // 선택 상태 표시
            when (menuItem.itemId) {
                R.id.nav_ticket -> { // 티켓마크
                    replaceFragment(TicketMarkFragment(), addToBackStack = false)
                }
                R.id.nav_community -> { // 커뮤니티
                    replaceFragment(CommunityManagerFragment(), addToBackStack = false)
                }
                R.id.nav_notice -> { // 공지
                    replaceFragment(NotificationFragment(), addToBackStack = false)
                }
                R.id.nav_subscribe -> { // 구독
                    replaceFragment(SubscribeFragment(), addToBackStack = false)
                }
                R.id.nav_setting -> { // 설정
                    replaceFragment(SettingFragment(), addToBackStack = false)
                }
                R.id.nav_support -> { // 고객센터
                    replaceFragment(CustomerCenterFragment(), addToBackStack = false)
                }
                else -> { // 그 외
                    toastWip("준비 중입니다.")
                }
            }
            true // 드로어 닫기 제거
        }

    // 프래그먼트 교체 공통 함수
    private fun replaceFragment(fragment: Fragment, addToBackStack: Boolean) {
        val tx = supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainerView.id, fragment)
        if (addToBackStack) tx.addToBackStack(null)
        tx.commit()
    }

    // 하단 네비게이션 마진 조정
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

    // 제스처 네비게이션 여부 확인
    private fun isGestureNavigation(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Settings.Secure.getInt(contentResolver, "navigation_mode", 0) == 2
        } else false
    }

    // dp → px 변환
    private fun Int.dpToPx(): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()
    }

    // 시스템 상단/하단바 투명 처리
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

    // 하단 네비게이션 탭 전환 및 아이콘 변경
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

        // 선택된 탭 아이콘 변경
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

    // 간단 토스트 표시
    private fun toastWip(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
