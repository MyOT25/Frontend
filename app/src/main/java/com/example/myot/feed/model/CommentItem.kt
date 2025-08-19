package com.example.myot.feed.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CommentItem(
    val username: String,
    val userid: String,
    val content: String,
    val date: String,
    val profileImageUrl: String? = null,

    var commentCount: Int = 0,
    var likeCount: Int = 0,
    var repostCount: Int = 0,
    var quoteCount: Int = 0,

    var isLiked: Boolean = false,
    var isReposted: Boolean = false,
    var isQuoted: Boolean = false,

    val isAnonymous: Boolean = false
) : Parcelable