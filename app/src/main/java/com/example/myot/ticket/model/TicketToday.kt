package com.example.myot.ticket.model

import com.google.gson.annotations.SerializedName

data class TicketToday(
    val title: String,
    val theater: String,
    val period: String,
    val cast: String,
    val avgRating: Double,
    val myRating: Double,
    val posterUrl: String
)
data class LatestViewingResponse(
    @SerializedName("resultType") val resultType: String,
    @SerializedName("error") val error: String?,
    @SerializedName("success") val success: SuccessResponse?
)

data class SuccessResponse(
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: ViewingData
)

data class ViewingData(
    val viewingId: Int,
    val title: String,
    val poster: String,
    val place: String,
    val region: Region,
    val period: String,
    val actors: List<RoleAndActor>,
    val averageRating: Double,
    val myRating: Double,
    val watchedDate: String,
    val watchedTime: String,
    val musicalId: Int
)

data class Region(
    val id: Int,
    val name: String
)

data class RoleAndActor(
    val role: String,
    val name: String
)