package com.example.myot.login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.lifecycle.lifecycleScope
import com.example.myot.MainActivity
import com.example.myot.R
import com.example.myot.databinding.ActivityLoginBinding
import com.example.myot.retrofit2.AuthRepository
import com.example.myot.retrofit2.AuthStore
import com.example.myot.retrofit2.RetrofitClient
import com.example.myot.retrofit2.TokenStore
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val scope = MainScope()
    private val repo by lazy { AuthRepository(RetrofitClient.authService) }

    private var pwVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 취소
        binding.tvCancel.setOnClickListener { finish() }

        // 비번 토글 버튼
        binding.ivPwToggle.setOnClickListener { togglePasswordVisible() }
        applyPwIcon() // 초기: 가려진 상태 아이콘

        // 입력 감지 → 버튼 상태
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateLoginButtonEnabled()
            }
        }
        binding.etId.addTextChangedListener(watcher)
        binding.etPw.addTextChangedListener(watcher)

        // 완료(Enter) 시 로그인
        binding.etPw.setOnEditorActionListener { _, actionId, event ->
            val isEnter = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN
            val isImeAction = actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_GO ||
                    actionId == EditorInfo.IME_ACTION_SEND ||
                    actionId == EditorInfo.IME_ACTION_NEXT

            if (isEnter || isImeAction) {
                true  // 이벤트 소비 → 로그인 안 됨
            } else {
                false
            }
        }

        // 버튼 클릭
        binding.btnLogin.setOnClickListener {
            if (!binding.btnLogin.isEnabled) return@setOnClickListener

            hideKeyboardAndClearFocus()

            val id = binding.etId.text?.toString()?.trim().orEmpty()
            val pw = binding.etPw.text?.toString()?.trim().orEmpty()

            setLoginEnabled(false)
            lifecycleScope.launch {
                repo.login(id, pw)
                    .onSuccess {
                        AuthStore.accessToken = it.accessToken
                        TokenStore.saveAccessToken(this@LoginActivity, it.accessToken)
                        TokenStore.saveUserId(this@LoginActivity, it.userId)

                        startActivity(Intent(this@LoginActivity, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                        finish()
                    }
                    .onFailure {
                        setLoginEnabled(true)
                        showToast("아이디 또는 비밀번호가 일치하지 않습니다.")
                    }
            }
        }
        // 키보드와 함께 15dp 덜 올라오기
        liftWithIme(binding.btnLogin, binding.loginRoot, offsetDp = 15)

        // 최초 비활성
        setLoginEnabled(false)
    }

    private fun isLoginEnabled(): Boolean =
        binding.etId.text?.isNotBlank() == true && binding.etPw.text?.isNotBlank() == true

    private fun updateLoginButtonEnabled() = setLoginEnabled(isLoginEnabled())

    private fun setLoginEnabled(enabled: Boolean) {
        binding.btnLogin.isEnabled = enabled
        if (enabled) {
            binding.btnLogin.setBackgroundResource(R.drawable.bg_write_btn)
            binding.tvLogin.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            binding.btnLogin.setBackgroundResource(R.drawable.bg_write_btn_disabled)
            binding.tvLogin.setTextColor(ContextCompat.getColor(this, R.color.gray2))
        }
    }

    private fun hideKeyboardAndClearFocus() {
        currentFocus?.clearFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        val windowToken = currentFocus?.windowToken ?: binding.loginRoot.windowToken
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun togglePasswordVisible() {
        pwVisible = !pwVisible
        val sel = binding.etPw.selectionStart
        if (pwVisible) {
            binding.etPw.inputType = EditorInfo.TYPE_CLASS_TEXT
        } else {
            binding.etPw.inputType = EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
        }
        // 폰트 유지 & 커서 복원
        binding.etPw.typeface = binding.etId.typeface
        binding.etPw.setSelection(sel.coerceAtLeast(0))
        applyPwIcon()
    }

    private fun applyPwIcon() {
        binding.ivPwToggle.setImageResource(
            if (pwVisible) R.drawable.ic_eye_on else R.drawable.ic_eye_off
        )
    }

    private fun liftWithIme(target: View, insetHost: View, offsetDp: Int = 0) {
        val offsetPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, offsetDp.toFloat(), resources.displayMetrics
        ).toInt()

        fun applyInsets(insets: WindowInsetsCompat) {
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val visible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val lift = (imeBottom - offsetPx).coerceAtLeast(0)
            target.translationY = if (visible) -lift.toFloat() else 0f
        }

        ViewCompat.setOnApplyWindowInsetsListener(insetHost) { _, insets ->
            applyInsets(insets); insets
        }
        ViewCompat.setWindowInsetsAnimationCallback(
            insetHost,
            object : WindowInsetsAnimationCompat.Callback(
                WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE
            ) {
                override fun onProgress(
                    insets: WindowInsetsCompat,
                    running: MutableList<WindowInsetsAnimationCompat>
                ): WindowInsetsCompat {
                    applyInsets(insets); return insets
                }
            }
        )
    }

    private fun showToast(message: String) {
        val v = LayoutInflater.from(this).inflate(R.layout.toast_simple, null)
        v.findViewById<TextView>(R.id.tv_toast).text = message

        Toast(this).apply {
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, (64).dp)
            view = v
        }.show()
    }

    private val Int.dp: Int get() =
        (this * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}