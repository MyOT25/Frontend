package com.example.myot.write

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myot.databinding.ActivityWriteBinding

class WriteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWriteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 닫기
        binding.tvCancel.setOnClickListener {
            finish()
        }
    }
}