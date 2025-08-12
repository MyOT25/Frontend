package com.example.myot.question.model

data class AnswerItem(
    val id: Long,
    val content: String,
    val createdAt: String,
    val authorName: String,
    val authorProfileImage: String?,
    val isAnonymous: Boolean = false
)