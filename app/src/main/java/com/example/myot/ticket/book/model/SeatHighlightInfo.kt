package com.example.myot.ticket.book.model

data class SeatHighlightInfo(
    val floor: Int,
    val zone: String,
    val blockNumber: Int,
    val rowNumber: Int,
    val seatIndex: Int,  // 좌석 배열 내 인덱스
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