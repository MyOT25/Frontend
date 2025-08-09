package com.example.myot.question.data

import com.example.myot.question.model.QuestionItem
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

private val VIEW_TIME_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")

fun QuestionListItemDto.toDomain(): QuestionItem {
    val displayTime = try {
        val odt = OffsetDateTime.parse(createdAt)
        odt.format(VIEW_TIME_FORMATTER)
    } catch (e: Exception) {
        createdAt
    }

    return QuestionItem(
        id = id,
        title = title,
        content = content,
        username = user.nickname,
        profileImage = user.profileImage,
        createdAt = displayTime,
        tags = questionTags?.map { it.tag.tagName } ?: emptyList()
    )
}