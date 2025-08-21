package com.example.myot.feed.data

data class CommentDto(
    val id: Long?,
    val postId: Long?,
    val userId: Long?,
    val anonymous: Boolean?,
    val content: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val user: CommentUserDto?
)

data class CommentUserDto(
    val id: Long?,
    val loginId: String?,
    val username: String?,
    val nickname: String?,
    val profileImage: String?
)