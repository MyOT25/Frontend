package com.example.myot

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myot.signup.SignupActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

        window.decorView.postDelayed({
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }, 2000)
    }
}