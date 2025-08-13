package com.example.myot.profile.data

import com.example.myot.feed.model.FeedItem
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ProfileFeedRepository(
    private val service: ProfileFeedService
) {
    suspend fun getAll(userId: Long, page: Int = 1, pageSize: Int = 20, auth: String? = null): List<FeedItem> {
        val res = service.getAll(userId, page, pageSize, auth)
        val posts: List<PostDto> = res.data?.posts.orEmpty()
        return posts.map { it.toFeedItem() }
    }
    suspend fun getReposts(userId: Long, page: Int = 1, pageSize: Int = 20, auth: String? = null): List<FeedItem> {
        val res = service.getReposts(userId, page, pageSize, auth)
        val posts: List<PostDto> = res.data?.posts.orEmpty()
        return posts.map { it.toFeedItem() }
    }
    suspend fun getQuotes(userId: Long, page: Int = 1, pageSize: Int = 20, auth: String? = null): List<FeedItem> {
        val res = service.getQuotes(userId, page, pageSize, auth)
        val posts: List<PostDto> = res.data?.posts.orEmpty()
        return posts.map { it.toFeedItem() }
    }
    suspend fun getMedia(userId: Long, page: Int = 1, pageSize: Int = 20, auth: String? = null): List<FeedItem> {
        val res = service.getMedia(userId, page, pageSize, auth)
        val posts: List<PostDto> = res.data?.posts.orEmpty()
        return posts.map { it.toFeedItem() }
    }
}

/** ====== Mapper ====== */
private fun String?.toDisplay(): String {
    if (this.isNullOrBlank()) return ""
    return try {
        Instant.parse(this)
            .atZone(ZoneId.of("Asia/Seoul"))
            .format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))
    } catch (_: Exception) {
        this
    }
}

private fun PostDto.toFeedItem(): FeedItem {
    val original = repostTarget?.toFeedItemLite()
    return FeedItem(
        username     = user?.nickname ?: "알 수 없음",
        content      = content.orEmpty(),
        imageUrls    = postImages.mapNotNull { it.url },
        date         = createdAt.toDisplay(),
        community    = community?.type.orEmpty(),
        isReposted   = (isRepost == true && repostType == "post"),
        isQuoted     = (isRepost == true && repostType == "quote"),
        commentCount = commentCount ?: 0,
        likeCount    = likeCount ?: 0,
        repostCount  = repostCount ?: 0,
        quoteCount   = 0,
        quotedFeed   = original
    )
}

private fun RepostTargetDto.toFeedItemLite(): FeedItem = FeedItem(
    username     = user?.nickname ?: "알 수 없음",
    content      = content.orEmpty(),
    imageUrls    = postImages.mapNotNull { it.url },
    date         = createdAt.toDisplay(),
    community    = community?.type.orEmpty(),
    isReposted   = false,
    isQuoted     = false,
    commentCount = 0, likeCount = 0, repostCount = 0, quoteCount = 0,
    quotedFeed   = null
)