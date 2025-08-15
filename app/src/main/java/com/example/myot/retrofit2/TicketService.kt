package com.example.myot.retrofit2

import com.example.myot.ticket.book.model.BookCoverResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface TicketService {
    @GET("api/viewingrecords/ticketbook")
    suspend fun getMyBookCovers(
        @Header("Authorization") token: String
    ): Response<BookCoverResponse>

}