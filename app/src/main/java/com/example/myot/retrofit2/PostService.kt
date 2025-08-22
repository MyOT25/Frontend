package com.example.myot.retrofit2

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class PostCreateRequest(
    @SerializedName("content") val content: String,
    @SerializedName("postimages") val postImages: List<String>,
    @SerializedName("communityId") val communityId: Long,
    @SerializedName("visibility") val visibility: String // "public" | "friends"
)

data class BasicEnvelope<T>(
    val resultType: String?,
    val data: T?
)

interface PostService {
    @POST("api/post")
    suspend fun createPost(
        @Header("Authorization") bearer: String,
        @Body body: PostCreateRequest
    ): Response<BasicEnvelope<Unit>>
}