package com.example.myot.feed.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FeedItem(
    val id: Long = -1L,
    val username: String,
    val community: String,
    val date: String,
    val content: String,
    val imageUrls: List<String> = emptyList(),

    var commentCount: Int = 0,
    var likeCount: Int = 0,
    var repostCount: Int = 0,
    var quoteCount: Int = 0,
    var bookmarkCount: Int = 0,

    var isLiked: Boolean = false,
    var isReposted: Boolean = false,
    var isQuoted: Boolean = false,
    var isBookmarked: Boolean = false,

    val profileImageUrl: String? = null,
    val communityCoverUrl: String? = null,
    val userHandle: String? = null,

    val quotedFeed: FeedItem? = null
) : Parcelable


@Parcelize
data class FeedbackUserUi(
    val nickname: String,
    val loginId: String? = null,
    val profileImage: String? = null
) : Parcelable
