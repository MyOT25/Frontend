package com.example.myot.question.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class QuestionItem(
    val isAnonymous: Boolean,
    val username: String? = null,
    val title: String,
    val time: String,
    val content: String,
    val likeCount: Int,
    val commentCount: Int,
    val imageUrls: List<String>? = null
) : Parcelable