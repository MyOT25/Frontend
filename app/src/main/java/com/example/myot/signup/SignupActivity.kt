package com.example.myot.signup

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.text.method.LinkMovementMethod
import android.text.Spanned
import androidx.appcompat.app.AppCompatActivity
import com.example.myot.R
import com.example.myot.databinding.ActivitySignupBinding
import com.example.myot.login.LoginActivity
import kotlin.jvm.java

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 구글/카카오(placeholder)
        binding.btnGoogle.setOnClickListener {
            android.widget.Toast.makeText(this, "Google 로그인 준비중", android.widget.Toast.LENGTH_SHORT).show()
        }
        binding.btnKakao.setOnClickListener {
            android.widget.Toast.makeText(this, "Kakao 로그인 준비중", android.widget.Toast.LENGTH_SHORT).show()
        }

        // 계정 만들기
        binding.btnCreateAccount.setOnClickListener {
        }

        // “로그인하기”만 클릭 가능하게 스팬 처리
        makeLoginClickable()
    }

    private fun makeLoginClickable() {
        val full = getString(R.string.signup_login_guide)
        val start = full.indexOf("로그인하기")
        val end = start + "로그인하기".length
        val sp = SpannableString(full).apply {
            setSpan(ForegroundColorSpan(getColor(R.color.point_blue)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(object : android.text.style.ClickableSpan() {
                override fun onClick(widget: android.view.View) {
                    startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.tvLoginLink.text = sp
        binding.tvLoginLink.movementMethod = LinkMovementMethod.getInstance()
    }
}