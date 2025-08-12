package com.example.myot.profile.data

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface ProfileFeedService {

    @GET("api/users/{userId}/profilefeed/all")
    suspend fun getAll(
        @Path("userId") userId: Long,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Header("Authorization") authorization: String? = null
    ): ProfileFeedEnvelope<ProfileFeedData>

    @GET("api/users/{userId}/profilefeed/repost")
    suspend fun getReposts(
        @Path("userId") userId: Long,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Header("Authorization") authorization: String? = null
    ): ProfileFeedEnvelope<ProfileFeedData>

    @GET("api/users/{userId}/profilefeed/quote")
    suspend fun getQuotes(
        @Path("userId") userId: Long,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Header("Authorization") authorization: String? = null
    ): ProfileFeedEnvelope<ProfileFeedData>

    @GET("api/users/{userId}/profilefeed/media")
    suspend fun getMedia(
        @Path("userId") userId: Long,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Header("Authorization") authorization: String? = null
    ): ProfileFeedEnvelope<ProfileFeedData>
}