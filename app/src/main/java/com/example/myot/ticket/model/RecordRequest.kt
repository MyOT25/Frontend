package com.example.myot.ticket.model

data class Musical(
    val id: Int,
    val name: String,
    val period: String,
    val theater: String,
    val avgRating: Double?,
    val poster: String,
    val performanceCount: Int,
    val ticketpic: String,
    val seatStructure: SeatStructure
)

data class SeatStructure(
    val hasFloor: Boolean,
    val hasZone: Boolean,
    val hasRowNumber: Boolean,
    val hasColumn: Boolean
)

data class MusicalSearchResponse(
    val resultType: String,
    val error: Any?,
    val success: MusicalSearchResult?
)

data class MusicalSearchResult(
    val message: String,
    val data: List<Musical>
)

data class RecordResponse(
    val resultType: String,
    val error: Any?,
    val success: RecordResult?
)

data class RecordResult(
    val message: String,
    val data: WatchRecord
)

data class WatchRecord(
    val id: Int,
    val userId: Int,
    val musicalId: Int,
    val seatId: Int,
    val date: String,
    val time: String,
    val content: String,
    val rating: Float,
    val averageRating: Float
)

data class BasicResponse(
    val resultType: String,
    val error: Any?,
    val success: BasicSuccess?
)

data class BasicSuccess(
    val message: String
)

data class CastResponse(
    val resultType: String,
    val error: String?,
    val success: CastSuccess?
)

data class CastSuccess(
    val message: String,
    val data: CastData
)

data class CastData(
    val musicalId: Int,
    val roles: List<Role>
)

data class Role(
    val role: String,
    val actors: List<Actor>
)

data class Actor(
    val actorId: Int,
    val name: String,
    val image: String,
    val birthDate: String,
    val profile: String?,
    val snsLink: String?,
    val performanceCount: Int
)
