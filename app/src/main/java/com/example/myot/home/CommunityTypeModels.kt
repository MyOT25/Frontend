package com.example.myot.home

import com.example.myot.write.CommunityOption

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