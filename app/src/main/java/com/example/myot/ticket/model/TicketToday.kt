package com.example.myot.ticket.model

data class TicketToday(
    val posterUrl: String,
    val title: String,
    val theater: String,
    val period: String,
    val cast: String,
    val avgRating: Double,
    val myRating: Double
)
