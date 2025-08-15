package com.example.myot.signup.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SignupCommunityService {
    @GET("api/community")
    suspend fun getCommunities(): CommunityListResponse

    @POST("api/community/type/join")
    suspend fun joinCommunity(
        @retrofit2.http.Header("Authorization") authorization: String,
        @Body body: JoinCommunityRequest
    ): JoinCommunityResponse
}

data class CommunityListResponse(
    val success: Boolean,
    val communities: List<CommunityDto>
)
data class CommunityDto(
    val communityId: Long,
    val communityName: String,
    val type: String,
    val createdAt: String
)

data class JoinCommunityRequest(
    val userId: Long,
    val communityId: Long,
    val action: String = "join",
    val profileType: String = "BASIC"
)
data class JoinCommunityResponse(
    val success: Boolean,
    val message: String
)