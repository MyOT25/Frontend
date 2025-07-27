package com.example.myot.feed.model

data class CommentItem(
    val username: String,
    val content: String,
    val date: String,

    var commentCount: Int = 0,
    var likeCount: Int = 0,
    var repostCount: Int = 0,
    var quoteCount: Int = 0,

    var isLiked: Boolean = false,
    var isReposted: Boolean = false,
    var isQuoted: Boolean = false,

    val isAnonymous: Boolean = false
)