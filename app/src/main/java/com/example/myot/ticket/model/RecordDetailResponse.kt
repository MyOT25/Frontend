package com.example.myot.ticket.model

data class ApiEnvelope<T>(
    val resultType: String,
    val error: Any?,
    val success: Success<T>?
) {
    data class Success<T>(val message: String, val data: T)
}

data class RecordDetailResponse(
    val id: Long,
    val musicalId: Long,
    val musicalTitle: String,
    val seat: SeatInfo?,
    val content: String?,
    val rating: Float,
    val averageRating: Float?,
    val casting: List<CastingInfo>?,
    val images: List<String>?,
    val date: String,   // "2025-06-16T00:00:00.000Z"
    val time: String,   // "2025-06-16T19:30:00.000Z"
    val author: AuthorInfo?,
    val isMine: Boolean
)

data class SeatInfo(
    val theaterId: Long?,
    val floor: Int?,
    val zone: String?,
    val rowNumber: String?,
    val columnNumber: String?
)

data class CastingInfo(
    val role: String?,
    val actorName: String?,
    val part: String?
)

data class AuthorInfo(
    val nickname: String?,
    val profileImage: String?
)

sealed interface RecordUiState {
    object Loading : RecordUiState
    data class Success(val data: RecordDetailResponse) : RecordUiState
    data class Error(val message: String) : RecordUiState
}