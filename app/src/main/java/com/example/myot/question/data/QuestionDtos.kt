package com.example.myot.question.data

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("resultType") val resultType: String,
    @SerializedName("error") val error: Any?,
    @SerializedName("success") val success: SuccessBody<T?>?
) {
    data class SuccessBody<T>(
        @SerializedName("message") val message: String,
        @SerializedName("data") val data: T
    )
}

data class QuestionListItemDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String?,
    @SerializedName("tagList") val tagList: List<String>?,
    @SerializedName("isAnonymous") val isAnonymous: Boolean,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("user") val user: UserDto,
    @SerializedName("likeCount") val likeCount: Int?,
    @SerializedName("commentCount") val commentCount: Int?
)

data class UserDto(
    @SerializedName("id") val id: Long?,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("profileImage") val profileImage: String?,          // 구버전 호환
    @SerializedName("profileImageUrl") val profileImageUrl: String?     // 신버전
)

data class QuestionTagDto(
    @SerializedName("id") val id: Long,
    @SerializedName("questionId") val questionId: Long,
    @SerializedName("userId") val userId: Long,
    @SerializedName("tagId") val tagId: Long,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("tag") val tag: TagDto
)

data class CommentsPageDto(
    @SerializedName("page") val page: Int?,
    @SerializedName("size") val size: Int?,
    @SerializedName("total") val total: Int?,
    @SerializedName("comments") val comments: List<AnswerDto>?
)

data class TagDto(
    @SerializedName("id") val id: Long,
    @SerializedName("tagName") val tagName: String,
    @SerializedName("createdAt") val createdAt: String
)

data class LikeActionDto(
    @SerializedName("id") val id: Long,
    @SerializedName("questionId") val questionId: Long,
    @SerializedName("userId") val userId: Long,
    @SerializedName("createdAt") val createdAt: String
)

data class LikeCountDto(
    @SerializedName("questionId") val questionId: Long,
    @SerializedName("likeCount") val likeCount: Int
)

data class QuestionDetailDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName(value = "imageUrl", alternate = ["imageUrls"]) val imageUrls: List<String>?, // ← 변경
    @SerializedName("tagList") val tagList: List<String>,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("user") val user: DetailUserDto,
    @SerializedName("isAnonymous") val isAnonymous: Boolean? = null,
    @SerializedName("likeCount") val likeCount: Int? = null,
    @SerializedName("commentCount") val commentCount: Int? = null,
    @SerializedName("thumbnailUrl") val thumbnailUrl: String? = null
)

data class DetailUserDto(
    @SerializedName("id") val id: Long,
    @SerializedName(value = "username", alternate = ["nickname"]) val username: String,          // ← 변경
    @SerializedName(value = "profileImage", alternate = ["profileImageUrl"]) val profileImage: String? // ← 변경
)

data class AnswerDto(
    @SerializedName("id") val id: Long?,
    @SerializedName("content") val content: String?,
    @SerializedName("isAnonymous") val isAnonymous: Boolean?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("user") val user: AnswerUserDto?
)

data class AnswerUserDto(
    @SerializedName("id") val id: Long?,
    @SerializedName(value = "username", alternate = ["nickname"]) val username: String?,
    @SerializedName(value = "profileImage", alternate = ["profileImageUrl"]) val profileImage: String?
)
data class AnswerLikeCountDto(
    @SerializedName("answerId") val answerId: Long?,
    @SerializedName("likeCount") val likeCount: Int?
)

data class AnswerLikeActionDto(
    @SerializedName("id") val id: Long?,
    @SerializedName("answerId") val answerId: Long?,
    @SerializedName("userId") val userId: Long?,
    @SerializedName("questionId") val questionId: Long?,
    @SerializedName("createdAt") val createdAt: String?
)

data class QuestionMeDto(
    @SerializedName("hasLiked") val hasLiked: Boolean,
    @SerializedName("hasCommented") val hasCommented: Boolean
)