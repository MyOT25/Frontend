package com.example.myot.ticket.book.model

data class SeatHighlightInfo(
    val floor: Int,
    val zone: String,
    val columnNumber: Int?,
    val rowNumber: Int,
    val seatIndex: Int,
    val numberOfSittings: Int
)

data class SeatData(
    val theaterId: Int,
    val seats: List<SeatHighlightInfo>
)

data class BookSeatResponse(
    val resultType: String,
    val error: Any?,
    val success: Success?
) {
    data class Success(
        val message: String,
        val data: SeatData
    )
}