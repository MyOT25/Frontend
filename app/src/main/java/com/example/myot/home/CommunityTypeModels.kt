package com.example.myot.home

data class CommunityTypeItem(
    val communityId: Long,
    val communityName: String,
    val type: String,
    val createdAt: String,
    val coverImage: String?,
    val memberCount: Int
)

data class CommunityTypeListResponse(
    val success: Boolean,
    val communities: List<CommunityTypeItem>
)