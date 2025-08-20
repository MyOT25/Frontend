package com.example.myot.profile

import retrofit2.Response
import retrofit2.http.*

data class IsFollowingEnvelope(
    val resultType: String?,
    val error: Any?,
    val success: IsFollowingSuccess?
)
data class IsFollowingSuccess(val isFollowing: Boolean)

interface ProfileService {
    @GET("/api/users/{userId}/isFollowing")
    suspend fun isFollowing(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long
    ): Response<IsFollowingEnvelope>

    @POST("/api/users/{userId}/follow")
    suspend fun follow(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long
    ): Response<IsFollowingEnvelope>

    @DELETE("/api/users/{userId}/follow")
    suspend fun unfollow(
        @Header("Authorization") token: String,
        @Path("userId") userId: Long
    ): Response<IsFollowingEnvelope>
}