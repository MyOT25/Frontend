package com.example.myot.feed.data

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface QuoteApi {
    @GET("api/posts/{postId}/quoted")
    suspend fun getQuotedPosts(
        @Header("Authorization") authorization: String?,
        @Path("postId") postId: Long
    ): QuoteListResponse
}

data class QuoteListResponse(
    val resultType: String,
    val error: Any?,
    val success: List<QuotePostDto>?
)

data class QuotePostDto(
    val id: Long,
    val content: String?,
    val createdAt: String?,
    val user: QuoteUserDto?,
    val postImages: List<QuotePostImage> = emptyList(),
    val community: QuoteCommunityDto?,
    val likeCount: Int? = 0,
    val bookmarkCount: Int? = 0
)

data class QuoteUserDto(
    val id: Long?,
    val nickname: String?,
    val profileImage: String?
)

data class QuotePostImage(val url: String?)
data class QuoteCommunityDto(
    val id: Long?,
    val type: String?,
    val coverImage: String?
)