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
    @SerializedName("userId") val userId: Long,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("user") val user: UserDto,
    @SerializedName("questionTags") val questionTags: List<QuestionTagDto>? = emptyList()
)

data class UserDto(
    @SerializedName("id") val id: Long,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("profileImage") val profileImage: String?
)

data class QuestionTagDto(
    @SerializedName("id") val id: Long,
    @SerializedName("questionId") val questionId: Long,
    @SerializedName("userId") val userId: Long,
    @SerializedName("tagId") val tagId: Long,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("tag") val tag: TagDto
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
    @SerializedName("imageUrl") val imageUrl: List<String>,
    @SerializedName("tagList") val tagList: List<String>,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("user") val user: DetailUserDto
)
data class DetailUserDto(
    @SerializedName("id") val id: Long,
    @SerializedName("username") val username: String,
    @SerializedName("profileImage") val profileImage: String?
)


data class AnswerDto(
    @SerializedName("id") val id: Long?,
    @SerializedName("content") val content: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("user") val user: AnswerUserDto?,
)

data class AnswerUserDto(
    @SerializedName("id") val id: Long?,
    @SerializedName("username") val username: String?,
    @SerializedName("profileImage") val profileImage: String?
)

data class AnswerLikeCountDto(
    @SerializedName("answerId") val answerId: Long?,
    @SerializedName("likeCount") val likeCount: Int?
)