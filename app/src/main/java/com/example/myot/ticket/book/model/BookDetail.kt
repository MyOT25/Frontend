package com.example.myot.ticket.book.model

data class BookDetailResponse(
    val resultType: String,
    val error: Any?,
    val success: TBSuccess?
)

data class TBSuccess(
    val message: String,
    val data: BookDetailData
)

data class BookDetailData(
    val musical: Musical,
    val castings: List<TBCasting>
)

data class Musical(
    val id: Int,
    val name: String,
    val performanceCount: Int,
    val myViewingCount: Int
)

data class TBCasting(
    val castingId: Int,
    val role: String,
    val performanceCount: Int,
    val actor: TBActor,
    val myCount: Int
)

data class TBActor(
    val id: Int,
    val name: String,
    val image: String?
)

data class RoleWithActors(
    val role: String,
    val actors: List<TBCasting>
)