package com.example.myot.community.model
data class CommunityFeedResponse(
    val success: Boolean,
    val feed: List<CommunityPostDto>,
    val nextCursor: String?
)

data class CommunityPostDto(
    val id: Long,
    val userId: Long?,
    val communityId: Long?,
    val isRepost: Boolean,
    val repostType: String?,
    val repostTargetId: Long?,
    val title: String?,
    val content: String?,
    val mediaType: String?,
    val viewCount: Int,
    val commentCount: Int,
    val likeCount: Int,
    val bookmarkCount: Int,
    val repostCount: Int,
    val createdAt: String?,
    val updatedAt: String?,
    val hasMedia: Boolean,
    val user: CommunityPostUserDto?,
    val community: CommunityInfoDto?,
    val postTags: List<Any>?,
    val postImages: List<PostImageDto>?,
    val repostTarget: CommunityPostDto?
)

data class CommunityPostUserDto(
    val id: Long?,
    val nickname: String?,
    val profileImage: String?
)

data class CommunityInfoDto(
    val id: Long?,
    val groupName: String?
)

data class PostImageDto(
    val id: Long?,
    val url: String?
)

