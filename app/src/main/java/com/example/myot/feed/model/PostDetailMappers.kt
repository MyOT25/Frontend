package com.example.myot.feed.model

import java.text.SimpleDateFormat
import java.util.*

fun PostDetailData.toFeedItem(): FeedItem {
    val images = postImages.mapNotNull { it.url }.filter { it.isNotBlank() }

    val loginIdText = user.loginId?.takeIf { it.isNotBlank() }
    val handle = loginIdText?.let { "@$it" }

    return FeedItem(
        id = this.id?.toLong() ?: -1L,
        username = user.nickname ?: loginIdText ?: "익명",
        content = content.orEmpty(),
        imageUrls = images,
        date = isoToDisplayDate(createdAt),
        community = community?.type ?: "",
        commentCount = commentCount ?: 0,
        likeCount = likeCount ?: 0,
        repostCount = repostCount ?: 0,
        bookmarkCount = bookmarkCount ?: 0,
        isLiked = isLiked ?: false,
        isBookmarked = isBookmarked ?: false,
        isReposted = isRepost ?: false,
        isCommented = false,
        profileImageUrl = user.profileImage,
        communityCoverUrl = community?.coverImage,
        userHandle = handle
    )
}

private fun isoToDisplayDate(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX"
    )
    for (p in patterns) {
        try {
            val fmt = SimpleDateFormat(p, Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val dt = fmt.parse(iso) ?: continue
            return SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(dt)
        } catch (_: Exception) {}
    }
    return ""
}