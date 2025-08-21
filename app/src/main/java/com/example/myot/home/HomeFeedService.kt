package com.example.myot.home

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface HomeFeedService {
    @GET("api/homefeed")
    suspend fun getHomeFeed(
        @Header("Authorization") token: String
    ): Response<HomeFeedEnvelope>
}