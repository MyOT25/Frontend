package com.example.myot.home

data class MyCommunitiesResponse(
    val success: Boolean,
    val communities: List<MyCommunityItem>
)

data class MyCommunityItem(
    val communityId: Int,
    val communityName: String,
    val type: String,
    val createdAt: String
)