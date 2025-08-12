package com.example.myot.question.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class QuestionItem(
    val id: Long,
    val title: String,
    val content: String,
    val username: String,
    val profileImage: String?,
    val createdAt: String,
    val tags: List<String>
) : Parcelable