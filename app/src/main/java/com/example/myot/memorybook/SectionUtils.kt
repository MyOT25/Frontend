package com.example.myot.memorybook

object SectionUtils {
//Section을 다루기 위한 유틸함수
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

    fun convertSectionsToParagraph(sections: List<Section>): String {
        val builder = StringBuilder()
        for (section in sections) {
            val indent = "  ".repeat(section.depth)
            builder.appendLine("$indent• ${section.title}")
            if (section.content.isNotBlank()) {
                builder.appendLine("$indent  ${section.content}")
            }
        }
        return builder.toString().trim()
    }
}