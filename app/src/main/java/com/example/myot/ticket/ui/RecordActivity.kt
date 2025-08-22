package com.example.myot.ticket.ui

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.myot.R
import com.example.myot.databinding.ActivityRecordBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setEdgeToEdgeWithInsets()
        //setTransparentSystemBars()
        binding.btnBackRecord.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    @Suppress("DEPRECATION")
    private fun setEdgeToEdgeWithInsets() {
        // 1) 엣지-투-엣지 ON
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    )
        }
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        // 2) 시스템 바 영역만큼 패딩 적용
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = sysBars.left,
                top = sysBars.top,
                right = sysBars.right,
                bottom = sysBars.bottom
            )
            insets
        }
    }
}