package com.example.myot.question

data class QuestionItem(
    val title: String,
    val time: String,
    val content: String,
    val likeCount: Int,
    val commentCount: Int,
    val imageUrl: String? = null
)