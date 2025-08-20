package com.example.myot.feed.model

data class PostDetailResponse(
    val resultType: String,
    val data: PostDetailData?
)

data class PostDetailData(
    val id: Long,
    val content: String?,
    val createdAt: String?,
    val commentCount: Int?,
    val likeCount: Int?,
    val isLiked: Boolean?,
    val repostCount: Int?,
    val bookmarkCount: Int?,
    val isBookmarked: Boolean?,
    val isRepost: Boolean?,
    val repostTarget: Any?,
    val user: PostUser,
    val postImages: List<PostImage> = emptyList(),
    val community: PostCommunity?
)

data class PostUser(
    val id: Long,
    val loginId: String?,
    val nickname: String?,
    val profileImage: String?
)

data class PostImage(val url: String?)
data class PostCommunity(
    val id: Long,
    val type: String?,
    val coverImage: String?
)