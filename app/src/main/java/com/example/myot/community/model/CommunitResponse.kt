package com.example.myot.community.model

data class CommunityResponse(
    val success: Boolean,
    val community: Community,
    val isJoined: Boolean,
    val joinedProfile: Profile?
)

data class Community(
    val communityId: Int,
    val groupName: String,
    val type: String,
    val targetId: Int,
    val recentPerformanceDate: String?,
    val theaterName: String,
    val ticketLink: String,
    val createdAt: String,
    val coverImage: String
)

data class JoinLeaveRequest(
    val communityId: Int,
    val action: String  // "join" 또는 "leave"
)

data class JoinLeaveResponse(
    val success: Boolean,
    val message: String
)

data class ProfileRequest(
    val userId: Int,
    val communityId: Int,
    val nickname: String,
    val image: String,
    val bio: String
)

data class ProfileResponse(
    val success: Boolean,
    val message: String,
    val profile: Profile
)

data class MultiProfilesResponse(
    val success: Boolean,
    val profile: List<Profile>
)

data class Profile(
    val id: Int,
    val userId: Int,
    val nickname: String,
    val image: String,
    val bio: String,
    val communityId: Int
)