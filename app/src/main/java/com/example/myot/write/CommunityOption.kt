package com.example.myot.write

import com.example.myot.home.MyCommunityItem

data class CommunityOption(
    val id: Long,
    val name: String,
    val desc: String? = null,
    val imageUrl: String? = null,
    val type: String = "musical"
)

private fun typeToDesc(type: String): String = when (type.lowercase()) {
    "musical" -> "관극 커뮤니티"
    "actor"   -> "배우 커뮤니티"
    else      -> "커뮤니티"
}

fun MyCommunityItem.toOption(): CommunityOption =
    CommunityOption(
        id = communityId.toLong(),
        name = communityName,
        desc = typeToDesc(type),
        imageUrl = null,
        type = type
    )