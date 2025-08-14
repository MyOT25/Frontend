package com.example.myot.signup

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.text.method.LinkMovementMethod
import android.text.Spanned
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
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
            showToast("Google 로그인은 준비 중이에요.")
        }
        binding.btnKakao.setOnClickListener {
            showToast("Kakao 로그인은 준비 중이에요.")
        }

        // 계정 만들기 → 기본 회원가입 폼으로 이동
        binding.btnCreateAccount.setOnClickListener {
            startActivity(Intent(this, SignupFlowActivity::class.java))
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

    private fun showToast(message: String) {
        val v = layoutInflater.inflate(R.layout.toast_simple, null)
        v.findViewById<TextView?>(R.id.tv_toast)?.text = message

        Toast(this).apply {
            duration = Toast.LENGTH_SHORT
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, (64).dp)
            view = v
        }.show()
    }


    private val Int.dp: Int get() =
        (this * resources.displayMetrics.density).toInt()


}