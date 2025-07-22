package com.example.myot

// 계층 구조를 평탄화해서 RecyclerView에 넘길 수 있도록 변환하는 함수
fun flattenSections(sections: List<Section>): List<Section> {
    val flatList = mutableListOf<Section>()
    for (section in sections) {
        flatList.add(section)
        if (section.isExpanded && section.children.isNotEmpty()) {
            flatList.addAll(flattenSections(section.children))
        }
    }
    return flatList
}