package com.example.myot

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FeedItem(
    val username: String,
    val community: String,
    val date: String,

    val content: String,
    val imageUrls: List<String> = emptyList(),

    var commentCount: Int = 0,
    var likeCount: Int = 0,
    var repostCount: Int = 0,
    var bookmarkCount: Int = 0,

    var isLiked: Boolean = false,
    var isReposted: Boolean = false,
    var isBookmarked: Boolean = false,

    val quotedFeed: FeedItem? = null
) : Parcelable
