package com.example.myot.feed.model

import com.example.myot.feed.data.*
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private fun isoToDisplayDate(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return try {
        val src = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val dst = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA)
        dst.format(src.parse(iso)!!)
    } catch (_: Exception) { "" }
}

fun QuotePostDto.toFeedItemWithQuoted(baseFeed: FeedItem): FeedItem {
    val images = postImages.mapNotNull { it.url }.filter { it.isNullOrBlank().not() }
    return FeedItem(
        id = id,
        username = user?.nickname ?: "익명",
        content = content.orEmpty(),
        imageUrls = images,
        date = isoToDisplayDate(createdAt),
        community = community?.type.orEmpty(),
        commentCount = 0,
        likeCount = likeCount ?: 0,
        repostCount = 0,
        bookmarkCount = bookmarkCount ?: 0,
        isLiked = false,
        isBookmarked = false,
        profileImageUrl = user?.profileImage,
        communityCoverUrl = community?.coverImage,
        userHandle = null,
        quotedFeed = baseFeed
    )
}