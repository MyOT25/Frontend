package com.example.myot.feed.data

import com.example.myot.feed.model.PostDetailResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

data class SimpleResult(
    val resultType: String? = null,
    val message: String? = null
)

interface FeedService {
    @GET("api/posts/{postId}")
    suspend fun getPostDetail(
        @Header("Authorization") token: String,
        @Path("postId") postId: Long
    ): Response<PostDetailResponse>

    @DELETE("api/posts/{postId}")
    suspend fun deletePost(
        @Header("Authorization") token: String,
        @Path("postId") postId: Long
    ): Response<SimpleResult>
}