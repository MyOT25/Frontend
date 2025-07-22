package com.example.myot.community

data class ReviewItem(
    val userName: String,
    val rating: Double,

    val imageUrls: List<String> = emptyList(),

    val theater: String,
    val casting: List<String>,
    val seat: String,
    val showDate: String,

    val content: String,

    val date: String,
    val isfollowed: Boolean = false,
    var likedNumber: Int = 0,
    var isLiked: Boolean = false
)

data class ReviewFilterItem(
    val role: String,
    val castings: List<String>
)