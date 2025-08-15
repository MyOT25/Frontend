package com.example.myot.retrofit2

import com.example.myot.ticket.book.model.BookCoverResponse
import com.example.myot.ticket.model.RecordRequest
import com.example.myot.ticket.model.MusicalSearchResponse
import com.example.myot.ticket.model.RecordResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface TicketService {
    @GET("api/viewingrecords/ticketbook")
    suspend fun getMyBookCovers(
        @Header("Authorization") token: String
    ): Response<BookCoverResponse>

    @GET("api/viewingrecords/musicals")
    suspend fun searchMusical(
        @Query("name") name: String
    ): Response<MusicalSearchResponse>

    @Multipart
    @POST("api/viewingrecords/musical")
    suspend fun postRecord(
        @Part("musicalId") musicalId: RequestBody,
        @Part("watchDate") watchDate: RequestBody,
        @Part("watchTime") watchTime: RequestBody,
        @Part("seat") seat: RequestBody,          // JSON 문자열
        @Part("casts") casts: RequestBody,        // JSON 문자열
        @Part("content") content: RequestBody,
        @Part("rating") rating: RequestBody,
        @Part imageFiles: List<MultipartBody.Part>
    ): Response<RecordResponse>
}