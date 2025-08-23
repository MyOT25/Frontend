package com.example.myot.feed.model

import com.example.myot.community.model.CommunityPostDto
import java.text.SimpleDateFormat
import java.util.*

fun PostDetailData.toFeedItem(): FeedItem {
    val images = postImages.mapNotNull { it.url }.filter { it.isNullOrBlank().not() }

    val loginIdText = user.loginId?.takeIf { it.isNotBlank() }
    val handle = loginIdText?.let { "@$it" }

    val likedByArray = (postLikes?.isNotEmpty() == true)
    val bookmarkedByArray = (postBookmarks?.isNotEmpty() == true)

    val quoted: FeedItem? =
        if (isRepost == true && repostTarget != null) {
            val rt = repostTarget
            val qImgs = (rt.postImages ?: emptyList())
                .mapNotNull { it.url }
                .filter { it.isNullOrBlank().not() }

            FeedItem(
                id = rt.id,
                username = rt.user?.nickname ?: rt.user?.loginId ?: "익명",
                community = rt.community?.type.orEmpty(),
                date = isoToDisplayDate(rt.createdAt),
                content = rt.content.orEmpty(),
                imageUrls = qImgs,

                commentCount = 0,
                likeCount = 0,
                repostCount = 0,
                quoteCount = 0,
                bookmarkCount = 0,

                isLiked = false,
                isReposted = false,
                isQuoted = false,
                isBookmarked = false,
                isCommented = false,

                profileImageUrl = rt.user?.profileImage,
                communityCoverUrl = rt.community?.coverImage,
                userHandle = rt.user?.loginId?.let { "@$it" },

                quotedFeed = null
            )
        } else null

    return FeedItem(
        id = this.id,
        username = user.nickname ?: loginIdText ?: "익명",
        content = content.orEmpty(),
        imageUrls = images,
        date = isoToDisplayDate(createdAt),
        community = community?.type ?: "",

        commentCount = commentCount ?: 0,
        likeCount = likeCount ?: 0,
        repostCount = repostCount ?: 0,
        bookmarkCount = bookmarkCount ?: 0,

        isLiked = isLiked ?: likedByArray,
        isBookmarked = isBookmarked ?: bookmarkedByArray,
        isReposted = isRepost ?: false,
        isCommented = false,

        profileImageUrl = user.profileImage,
        communityCoverUrl = community?.coverImage,
        userHandle = handle,

        quotedFeed = quoted
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

fun CommunityPostDto.toFeedItem(): FeedItem {
    val images = (postImages ?: emptyList())
        .mapNotNull { it.url }
        .filter { it.isNullOrBlank().not() }

    val quoted = repostTarget?.toFeedItem()

    return FeedItem(
        id = id,
        username = user?.nickname ?: "익명",
        profileImageUrl = user?.profileImage,
        content = content.orEmpty(),
        imageUrls = images,
        date = isoToDisplayDate(createdAt),

        community = community?.groupName.orEmpty(),
        commentCount = commentCount,
        likeCount = likeCount,
        bookmarkCount = bookmarkCount,
        repostCount = repostCount,

        isReposted = isRepost ?: false,
        isLiked = false,
        isBookmarked = false,
        isCommented = false,
        quotedFeed = quoted
    )
}