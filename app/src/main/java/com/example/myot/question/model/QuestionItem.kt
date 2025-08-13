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
    val tags: List<String>,
    val isAnonymous: Boolean = false,
    val thumbnailUrl: String? = null,
    val likeCount: Int? = null,
    val commentCount: Int? = null
) : Parcelable