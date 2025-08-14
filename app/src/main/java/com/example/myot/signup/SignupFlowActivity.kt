package com.example.myot.signup

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.myot.R
import com.example.myot.databinding.ActivitySignupFlowBinding
import com.example.myot.signup.data.SignupViewModel

class SignupFlowActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupFlowBinding

    val vm: SignupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupFlowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setNextEnabled(false)

        // 첫 화면 진입
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.signup_container, SignupStep4Fragment())
                .commit()
        }

        binding.btnNext.setOnClickListener {
            (supportFragmentManager.findFragmentById(R.id.signup_container) as? SignupStep)
                ?.onNextClicked()
        }

        // 취소 → SignupActivity로
        binding.tvCancel.setOnClickListener { navigateBackToSignup() }

        // 시스템 뒤로가기: 스택 있으면 pop, 없으면 SignupActivity로
        onBackPressedDispatcher.addCallback(this) {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
            } else {
                navigateBackToSignup()
            }
        }

        liftWithIme(target = binding.btnNext, insetHost = binding.signupRoot, offsetDp = 15)
    }

    fun goNext(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.signup_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun navigateBackToSignup() {
        startActivity(
            Intent(this, SignupActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        )
        finish()
    }

    fun setNextEnabled(enabled: Boolean) {
        binding.btnNext.isEnabled = enabled
        if (enabled) {
            binding.btnNext.setBackgroundResource(R.drawable.bg_write_btn)
            binding.tvNext.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            binding.btnNext.setBackgroundResource(R.drawable.bg_write_btn_disabled)
            binding.tvNext.setTextColor(ContextCompat.getColor(this, R.color.gray2))
        }
    }

    fun showStandardNav() {
        binding.tvCancel.visibility = View.VISIBLE
        binding.btnNext.visibility = View.VISIBLE
        binding.bottomDual.visibility = View.GONE
    }


    fun showDualNav(
        imageChosen: Boolean,
        onLater: (() -> Unit)?,
        onNext:  (() -> Unit)?
    ) {
        // 상단 숨김, 듀얼바 표시
        binding.tvCancel.visibility = View.GONE
        binding.btnNext.visibility = View.GONE
        binding.bottomDual.visibility = View.VISIBLE

        // 버튼 상태 지정
        setLaterEnabled(!imageChosen)
        setDualNextEnabled(imageChosen)

        binding.btnLater.setOnClickListener {
            if (binding.btnLater.isEnabled) onLater?.invoke()
        }
        binding.btnDualNext.setOnClickListener {
            if (binding.btnDualNext.isEnabled) onNext?.invoke()
        }
    }

    fun setDualNextEnabled(enabled: Boolean) {
        binding.btnDualNext.isEnabled = enabled
        if (enabled) {
            binding.btnDualNext.setBackgroundResource(R.drawable.bg_write_btn)
            binding.btnDualNext.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.point_deep_purple)
            binding.tvDualNext.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            binding.btnDualNext.setBackgroundResource(R.drawable.bg_write_btn_disabled)
            binding.tvDualNext.setTextColor(ContextCompat.getColor(this, R.color.gray4))
        }
    }

    fun setLaterEnabled(enabled: Boolean) {
        binding.btnLater.isEnabled = enabled

         if (enabled) {
             binding.btnLater.setBackgroundResource(R.drawable.bg_write_btn_disabled)
             binding.tvLater.setTextColor(ContextCompat.getColor(this, R.color.gray1))
         } else {
             binding.btnLater.setBackgroundResource(R.drawable.bg_write_btn_disabled)
             binding.tvLater.setTextColor(ContextCompat.getColor(this, R.color.gray4))
         }
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
}