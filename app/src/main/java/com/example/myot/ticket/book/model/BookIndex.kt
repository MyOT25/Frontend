package com.example.myot.ticket.book.model

import androidx.lifecycle.LiveData

data class BookIndex(
    val season: String,
    val posterUrl: String
)

data class BookIndexResponse(
    val resultType: String,
    val error: Any?,
    val success: BookIndexSuccess
)

data class BookIndexSuccess(
    val message: String,
    val data: BookIndexData
)

data class BookIndexData(
    val title: String,
    val series: List<BookSeries>
)

data class BookSeries(
    val label: String,
    val period: Period,
    val poster: String,
    val theater: Theater,
    val seasonMusicalId: Int,
    val entries: List<ViewingEntry>
)

data class Period(
    val startDate: String, // ISO 8601 형식
    val endDate: String    // ISO 8601 형식
)

// 관람 기록
data class ViewingEntry(
    val viewingId: Int,
    val watchDate: String,    // ISO 8601 형식
    val watchTime: String,    // ISO 8601 형식
    val theater: Theater,
    val rating: Int,
    val content: String
)