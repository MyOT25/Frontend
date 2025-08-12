package com.example.myot.retrofit2

import com.example.myot.community.model.CommunityResponse
import com.example.myot.community.model.DeleteProfileResponse
import com.example.myot.community.model.JoinLeaveRequest
import com.example.myot.community.model.JoinLeaveResponse
import com.example.myot.community.model.MultiProfilesResponse
import com.example.myot.community.model.ProfileRequest
import com.example.myot.community.model.ProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
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

    @DELETE("api/community/profile/{profileId}")
    suspend fun deleteMultiProfile(
        @Header("Authorization") token: String,
        @Path("profileId") profileId: Int
    ): Response<DeleteProfileResponse>
}