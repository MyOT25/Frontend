package com.example.myot.ticket.calendar.model

// 응답 데이터 클래스들
data class MonthlyRecordsResponse(
    val resultType: String,
    val error: ErrorResponse?,
    val success: SuccessResponse?
)

data class ErrorResponse(
    val errorCode: String?,
    val reason: String?,
    val data: Any?
)

data class SuccessResponse(
    val message: String,
    val data: List<ViewingRecordResponse>
)

data class ViewingRecordResponse(
    val postId: Int,
    val musicalId: Int,
    val musicalTitle: String,
    val watchDate: String,
    val watchTime: String,
    val seat: SeatResponse,
    val content: String,
    val imageUrls: List<String>
)

data class SeatResponse(
    val locationId: Int
)

data class CalendarEntryRequest(
    val musicalId: Int,
    val watchDate: String,
    val watchTime: String,
    val content: String,
    val imageUrls: List<String>,
    val seat: SeatRequest
)

data class SeatRequest(
    val locationId: Int
)

data class SaveRecordResponse(
    val resultType: String,
    val error: ErrorResponse?,
    val success: SuccessResponse?
)

data class DeleteRecordResponse(
    val resultType: String,
    val error: ErrorResponse?,
    val success: SuccessResponse?
)

data class CalendarDay(
    val text: String,
    val isHeader: Boolean,
    val isToday: Boolean,
    val entries: List<CalendarEntry>
)

data class CalendarEntry(
    val id: String,
    val imageUri: String,
    val memo: String,
    val date: String,
    val musicalTitle: String = "",
    val watchTime: String = "00:00",
    val allImageUrls: List<String> = emptyList()
)