package com.example.myot.community.model

data class basicResponse(
    val success: Boolean,
    val message: String
)

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
    val targetId: Int?,
    val recentPerformanceDate: String,
    val theaterName: String,
    val ticketLink: String,
    val createdAt: String,
    val coverImage: String?
)

data class JoinLeaveRequest(
    val userId: Long,
    val communityId: Int,
    val action: String,  // "join" or "leave"
    val profileType: String, // "BASIC" or "MULTI"
    val multi: Multi?
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

// 답변 형식 확인 필요
data class MultiProfileResponse(
    val profileType: String,
    val multi: Multi
)

data class Multi(
    val nickname: String,
    val image: String,
    val bio: String
)

data class PatchProfileRequest(
    val profileType: String,
    val multi: Multi?
)

data class PatchProfileResponse(
    val success: Boolean,
    val message: String,
    val changedTo: String?
)

data class ProfileResponse(
    val success: Boolean,
    val message: String,
    val profile: Profile
)

data class CommunityProfileResponse(
    val success: Boolean,
    val profileType: String,
    val profile: Profile
)

data class Profile(
    val id: Int,
    val userId: Long,
    val communityId: Int,
    val nickname: String,
    val image: String,
    val bio: String
)
data class DeleteProfileResponse(
    val success: Boolean,
    val message: String,
    val deletedProfile: Profile
)

data class SeatPayload(
    val theaterId: Int,
    val floor: Int,
    val zone: String,
    val blockNumber: Int,
    val rowNumber: Int,
    val seatIndex: Int
)