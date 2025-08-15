package com.example.myot.retrofit2

import com.example.myot.community.model.CommunityResponse
import com.example.myot.community.model.JoinLeaveRequest
import com.example.myot.community.model.JoinLeaveResponse
import com.example.myot.community.model.MultiProfilesResponse
import com.example.myot.community.model.ProfileRequest
import com.example.myot.community.model.ProfileResponse
import com.example.myot.home.MyCommunitiesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Header

interface CommunityService {
    @GET("api/community/musical/{communityId}")
    suspend fun getCommunityInfo(
        @Header("Authorization") token: String,
        @Path("communityId") communityId: Int
    ): Response<CommunityResponse>

    @POST("api/community/type/join")
    suspend fun setUserStatus(
        @Header("Authorization") token: String,
        @Body request: JoinLeaveRequest
    ): JoinLeaveResponse

    @POST("api/community/profile")
    suspend fun setCommunityProfile(
        @Header("Authorization") token: String,
        @Body request: ProfileRequest
    ): Response<ProfileResponse>

    @GET("api/community/user-profile/{communityId}/{userId}")
    suspend fun getUserMultiProfiles(
        @Header("Authorization") token: String,
        @Path("communityId") communityId: Int,
        @Path("userId") userId: Int
    ): Response<MultiProfilesResponse>

    @GET("api/community/profile/my/{communityId}")
    suspend fun getMyMultiProfiles(
        @Header("Authorization") token: String,
        @Path("communityId") communityId: Int
    ): Response<MultiProfilesResponse>

    @GET("api/community/mine")
    suspend fun getMyCommunities(
        @Header("Authorization") token: String
    ): Response<MyCommunitiesResponse>

    @GET("api/community/{type}/{id}")
    suspend fun getCommunityDetail(
        @Header("Authorization") token: String,
        @Path("type") type: String,
        @Path("id") id: Int
    ): Response<CommunityResponse>
}