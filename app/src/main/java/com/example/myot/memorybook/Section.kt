package com.example.myot.memorybook

import java.io.Serializable
import java.util.UUID

//목차를 정의하는 Section 클래스
//목차 박스 내의 목차들과 그 아래 작성 부분의 목차가 일치해야 하므로 하나의 클래스로 관리

data class Section(
    val id: String = UUID.randomUUID().toString(),     // 고유 ID
    var title: String,                                 // 목차 제목 (ex. 1. 개요)
    var content: String = "",                          // 본문 내용
    var depth: Int = 0,                                // 계층 깊이 (0: 대제목, 1: 부제목, ...)
    var isExpanded: Boolean = true,                    // 접기/펼치기 상태
    var children: ArrayList<Section> = arrayListOf()   // 하위 섹션
) : Serializable
//목차 내에 또다른 하위 목차들이 있으므로 트리 구조로 만들었다.