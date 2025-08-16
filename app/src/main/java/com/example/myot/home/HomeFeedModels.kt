package com.example.myot.home

data class HomeFeedEnvelope(
    val resultType: String,
    val data: HomeFeedData?
)

data class HomeFeedData(
    val total: Int?,
    val page: Int?,
    val limit: Int?,
    val posts: List<HomeFeedPost> = emptyList()
)

data class HomeFeedPost(
    val id: Long,
    val content: String?,
    val createdAt: String?,
    val commentCount: Int?,
    val likeCount: Int?,
    val repostCount: Int?,
    val bookmarkCount: Int?,
    val isRepost: Boolean?,
    val repostType: String?,
    val user: HomeFeedUser?,
    val postImages: List<HomeFeedImage>?,
    val community: HomeFeedCommunity?,
    val postLikes: Boolean?,
    val postBookmarks: Boolean?
)

data class HomeFeedUser(
    val id: Long?,
    val nickname: String?,
    val profileImage: String?
)

data class HomeFeedImage(
    val url: String?
)

data class HomeFeedCommunity(
    val id: Long?,
    val type: String?,         // ex) "musical"
    val coverImage: String?
)