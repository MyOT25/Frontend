package com.example.myot.retrofit2

import com.example.myot.ticket.book.model.BookCoverResponse
import com.example.myot.ticket.book.model.BookDetailResponse
import com.example.myot.ticket.book.model.BookIndexResponse
import com.example.myot.ticket.book.model.BookSeatResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface BookService {
    @GET("api/viewingrecords/ticketbook")
    suspend fun getMyBookCovers(
        @Header("Authorization") token: String
    ): Response<BookCoverResponse>

    @GET("api/ticketbook/{musicalId}/series")
    suspend fun getMyBookSesons(
        @Header("Authorization") token: String,
        @Path("musicalId") id: Int
    ): Response<BookIndexResponse>

    @GET("api/ticketbook/count/{musicalId}")
    suspend fun getTicketBookCount(
        @Header("Authorization") token: String,
        @Path("musicalId") musicalId: Int
    ): Response<BookDetailResponse>

    @GET("api/ticketbook/{musicalId}")
    suspend fun getSeatData(
        @Header("Authorization") token: String,
        @Path("musicalId") musicalId: Int
    ): Response<BookSeatResponse>
}