package com.example.myot.retrofit2

import com.example.myot.community.model.CommunityProfileResponse
import com.example.myot.community.model.CommunityResponse
import com.example.myot.community.model.DeleteProfileResponse
import com.example.myot.community.model.JoinLeaveRequest
import com.example.myot.community.model.JoinLeaveResponse
import com.example.myot.community.model.MultiProfileResponse
import com.example.myot.community.model.PatchProfileRequest
import com.example.myot.community.model.PatchProfileResponse
import com.example.myot.community.model.ProfileRequest
import com.example.myot.community.model.ProfileResponse
import com.example.myot.home.MyCommunitiesResponse
import com.example.myot.community.model.basicResponse
import com.example.myot.home.CommunityTypeListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Header
import retrofit2.http.PATCH

interface CommunityService {
    @GET("api/community/{type}/{communityId}")
    suspend fun getCommunityInfo(
        @Header("Authorization") token: String,
        @Path("type") type: String,
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
    suspend fun getUserMultiProfile(
        @Header("Authorization") token: String,
        @Path("communityId") communityId: Int,
        @Path("userId") userId: Int
    ): Response<MultiProfileResponse>

    @GET("api/community/profile/my/{communityId}")
    suspend fun getMyCommunityProfile(
        @Header("Authorization") token: String,
        @Path("communityId") communityId: Int
    ): Response<CommunityProfileResponse>

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

    @PATCH("api/community/profile/type/{communityId}")
    suspend fun patchMultiProfileType(
        @Header("Authorization") token: String,
        @Path("communityId") communityId: Int,
        @Body request: PatchProfileRequest
    ): PatchProfileResponse

    @DELETE("api/community/profile/{profileId}")
    suspend fun deleteMultiProfile(
        @Header("Authorization") token: String,
        @Path("profileId") profileId: Int
    ): Response<DeleteProfileResponse>

    @GET("api/community/type/{type}/{userId}")
    suspend fun getCommunitiesByType(
        @Header("Authorization") token: String,
        @Path("type") type: String,
        @Path("userId") userId: Long
    ): Response<CommunityTypeListResponse>
}