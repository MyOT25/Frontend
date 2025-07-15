package com.example.myot

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myot.databinding.ActivityMainBinding
import com.example.myot.question.QuestionSearchFragment


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var topBar: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 아이콘 tint 제거 (drawable 원본 색 유지)
        binding.bottomNavigationView.itemIconTintList = null

        selectTab(R.id.menu_home)

        binding.bottomNavigationView.setOnItemSelectedListener {
            selectTab(it.itemId)
            true
        }

        topBar = findViewById(R.id.top_bar)

        supportFragmentManager.addOnBackStackChangedListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
            if (currentFragment is QuestionSearchFragment) {
                topBar.visibility = View.GONE
            } else {
                topBar.visibility = View.VISIBLE
            }
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
