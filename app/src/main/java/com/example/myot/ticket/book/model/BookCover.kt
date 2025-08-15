package com.example.myot.ticket.book.model

data class BookCoverResponse(
    val resultType: String,
    val error: Int?,
    val success: BookCoverSuccess
)

data class BookCoverSuccess(
    val message: String,
    val data: List<BookCover>
)

data class BookCover(
    val musical_id: Int,
    val title: String,
    val poster: String,
    // 날짜나 시즌
    val theater: Theater
)

data class Theater(
    val name: String,
    val region: Int
)