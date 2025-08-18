package com.example.myot.retrofit2

import com.example.myot.ticket.book.model.BookCoverResponse
import com.example.myot.ticket.book.model.BookIndexResponse
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
}