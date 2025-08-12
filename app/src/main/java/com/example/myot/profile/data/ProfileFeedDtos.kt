package com.example.myot.profile.data

data class ProfileFeedData(
    val totalCount: Int? = 0,
    val page: Int? = 1,
    val pageSize: Int? = 20,
    val posts: List<PostDto> = emptyList()
)

data class PostDto(
    val id: Long? = null,
    val content: String? = null,
    val createdAt: String? = null,
    val commentCount: Int? = 0,
    val likeCount: Int? = 0,
    val repostCount: Int? = 0,
    val bookmarkCount: Int? = 0,
    val mediaType: String? = null,
    val hasMedia: Boolean? = null,
    val isRepost: Boolean? = null,
    val repostType: String? = null, // "post" | "quote" | null

    val user: UserDto? = null,
    val postImages: List<PostImageDto> = emptyList(),
    val community: CommunityDto? = null,

    // ALL에 있을 수 있음
    val postLikes: List<PostLikeDto> = emptyList(),
    val postBookmarks: List<PostBookmarkDto> = emptyList(),

    // REPOST/QUOTE에서 있을 수 있음
    val repostTarget: RepostTargetDto? = null
)

data class UserDto(
    val id: Long? = null,
    val nickname: String? = null,
    val profileImage: String? = null
)

data class PostImageDto(val url: String? = null)

data class CommunityDto(
    val id: Long? = null,
    val type: String? = null,
    val coverImage: String? = null
)

data class RepostTargetDto(
    val id: Long? = null,
    val content: String? = null,
    val createdAt: String? = null,
    val user: UserDto? = null,
    val postImages: List<PostImageDto> = emptyList(),
    val community: CommunityDto? = null
)

data class PostLikeDto(val id: Long? = null, val userId: Long? = null)
data class PostBookmarkDto(val id: Long? = null, val userId: Long? = null)