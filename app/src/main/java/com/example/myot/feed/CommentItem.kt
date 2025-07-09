package com.example.myot.feed

data class CommentItem(
    val username: String,
    val content: String,
    val date: String,

    var commentCount: Int = 0,
    var likeCount: Int = 0,
    var repostCount: Int = 0,
    var bookmarkCount: Int = 0,

    var isLiked: Boolean = false,
    var isReposted: Boolean = false,
    var isBookmarked: Boolean = false
)