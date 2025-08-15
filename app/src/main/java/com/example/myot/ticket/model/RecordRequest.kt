package com.example.myot.ticket.model

import android.graphics.Picture

data class RecordRequest(
    val musicalId: Int,
    val watchDate: String,
    val watchTime: String,
    val seat: seatInfo,
    // 캐스트 정보
    val content: String,
    val rating: Double,
    val imageFiles: Picture?
)

data class seatInfo(
    val id: Int,

)

data class SeatStructureInfo(
    val hasFloor: Boolean,
    val hasZone: Boolean,
    val hasRow: Boolean,
    val hasNumber: Boolean
)
