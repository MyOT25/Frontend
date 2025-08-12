package com.example.myot.memorybook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myot.memorybook.api.MemoryBookApi

// MemoryBookViewModelFactory
//MemoryBookApi를 외부에서 주입받아 MemoryBookViewModel을 생성해주는 팩토리 클래스
//ViewModelProvider에서 ViewModel 생성 시 사용
class MemoryBookViewModelFactory(
    private val memoryBookApi: MemoryBookApi // 생성자 주입 받을 API 인스턴스
) : ViewModelProvider.Factory {

    // ViewModel 인스턴스 생성 함수
    // ViewModel 클래스 타입이 MemoryBookViewModel과 일치하는 경우 생성 후 반환
    @Suppress("UNCHECKED_CAST") // 타입 안전성 경고 무시
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MemoryBookViewModel::class.java) -> {
                MemoryBookViewModel(memoryBookApi) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
