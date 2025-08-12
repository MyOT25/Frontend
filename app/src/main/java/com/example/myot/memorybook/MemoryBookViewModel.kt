package com.example.myot.memorybook

import android.util.Log
import androidx.lifecycle.*
import com.example.myot.memorybook.api.MemoryBookApi
import com.example.myot.memorybook.api.MemoryBookRequest
import com.example.myot.memorybook.api.Content
import com.example.myot.memorybook.api.MemoryBookResponse
import kotlinx.coroutines.launch

// 메모리북 뷰모델
// - MemoryBookApi를 외부에서 주입받도록 수정함
class MemoryBookViewModel(
    private val memoryBookApi: MemoryBookApi // 생성자 주입
) : ViewModel() {

    // 메모리북 섹션 데이터 (트리 구조의 루트 섹션 리스트)
    private val _sectionList = MutableLiveData<List<Section>>()
    val sectionList: LiveData<List<Section>> get() = _sectionList

    // 메모리북 생성 결과 LiveData
    private val _createResult = MutableLiveData<Result<MemoryBookResponse>>()
    val createResult: LiveData<Result<MemoryBookResponse>> get() = _createResult

    init {
        // 초기에는 빈 섹션 리스트로 시작
        _sectionList.value = emptyList()
    }

    // 전체 섹션 교체 (수정 화면에서 저장된 flat 리스트를 트리로 복원 후 반영)
    fun replaceAllSections(flatList: List<Section>) {
        _sectionList.value = rebuildTree(flatList)
    }

    // flat 리스트를 트리 구조로 복원하는 함수
    private fun rebuildTree(flatList: List<Section>): List<Section> {
        val rootList = mutableListOf<Section>()
        val stack = ArrayDeque<Section>()

        for (section in flatList) {
            section.children.clear()

            while (stack.isNotEmpty() && stack.last().depth >= section.depth) {
                stack.removeLast()
            }

            if (stack.isEmpty()) {
                rootList.add(section)
            } else {
                stack.last().children.add(section)
            }

            stack.addLast(section)
        }

        return rootList
    }

    // 메모리북 생성 API 호출
    fun createMemoryBook(request: MemoryBookRequest) {
        viewModelScope.launch {
            try {
                // API 요청 실행
                val response = memoryBookApi.createMemoryBook(request)

                // 디버깅 로그 출력
                Log.d("API_RESPONSE", "code: ${response.code()}")
                Log.d("API_RESPONSE", "errorBody: ${response.errorBody()?.string()}")

                // 응답 결과 처리
                if (response.isSuccessful) {
                    _createResult.postValue(Result.success(response.body()!!))
                } else {
                    _createResult.postValue(Result.failure(Exception("서버 응답 오류")))
                }
            } catch (e: Exception) {
                // 네트워크 예외 처리
                _createResult.postValue(Result.failure(e))
            }
        }
    }
}
