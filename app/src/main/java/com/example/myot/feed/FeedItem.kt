package com.example.myot.feed

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
    var quoteCount: Int = 0,

    var isLiked: Boolean = false,
    var isReposted: Boolean = false,
    var isQuoted: Boolean = false,

    val quotedFeed: FeedItem? = null
) : Parcelable