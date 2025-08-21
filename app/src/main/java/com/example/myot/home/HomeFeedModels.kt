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
    val postComments: Boolean?,
    val reposts: Boolean?,
    val isRepost: Boolean?,
    val repostType: String?,
    val user: HomeFeedUser?,
    val postImages: List<HomeFeedImage>?,
    val community: HomeFeedCommunity?,
    val postLikes: Boolean?,
    val postBookmarks: Boolean?,

    val repostTarget: HomeFeedRepostTarget? = null
)

data class HomeFeedUser(
    val id: Long?,
    val nickname: String?,
    val profileImage: String?,
    val loginId: String? = null
)

data class HomeFeedImage(
    val url: String?
)

data class HomeFeedCommunity(
    val id: Long?,
    val type: String?,
    val coverImage: String?
)

data class HomeFeedRepostTarget(
    val id: Long,
    val content: String?,
    val createdAt: String?,
    val user: HomeFeedUser?,
    val postImages: List<HomeFeedImage>?,
    val community: HomeFeedCommunity?
)