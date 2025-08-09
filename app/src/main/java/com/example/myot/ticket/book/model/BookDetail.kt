package com.example.myot.ticket.book.model

data class BookDetail(
    val roles: List<TBRole>
)

data class TBActor(
    val name: String,
    val imageUrl: String,
    val count: Int,
    val total: Int
)

data class TBRole(
    val roleName: String,
    val actors: List<TBActor>
)

