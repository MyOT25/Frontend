package com.example.myot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MemoryBookViewModel : ViewModel() {

    // 메모리북 섹션 데이터 (트리 구조의 루트 섹션 리스트)
    private val _sectionList = MutableLiveData<List<Section>>()
    val sectionList: LiveData<List<Section>> get() = _sectionList

    // 앱 초기 시작 시엔 빈 리스트로 시작
    init {
        _sectionList.value = emptyList()
    }

    // 전체 섹션 교체 (수정 화면에서 저장된 flat 리스트를 트리로 복원 후 반영)
    fun replaceAllSections(flatList: List<Section>) {
        _sectionList.value = rebuildTree(flatList)
    }

    // flat 리스트 → 트리 구조로 복원
    private fun rebuildTree(flatList: List<Section>): List<Section> {
        val rootList = mutableListOf<Section>()
        val stack = ArrayDeque<Section>()

        for (section in flatList) {
            section.children.clear() // 기존 children 초기화

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
}
