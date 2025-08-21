package com.example.myot.retrofit2

import com.example.myot.ticket.model.ApiEnvelope
import com.example.myot.ticket.model.CastResponse
import com.example.myot.ticket.model.MusicalSearchResponse
import com.example.myot.ticket.model.RecordDetailResponse
import com.example.myot.ticket.model.RecordResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface TicketService {
    @GET("api/viewingrecords/musicals")
    suspend fun searchMusical(
        @Query("name") name: String
    ): Response<MusicalSearchResponse>

    @Multipart
    @POST("api/viewingrecords/musical")
    suspend fun postViewingRecord(
        @Header("Authorization") token: String,
        @Part("musicalId") musicalId: RequestBody,
        @Part("watchDate") watchDate: RequestBody,
        @Part("watchTime") watchTime: RequestBody,
        @Part("seat") seat: RequestBody,
        @Part("casts") casts: RequestBody,
        @Part("content") content: RequestBody,
        @Part("rating") rating: RequestBody,
        @Part imageFiles: List<MultipartBody.Part>?
    ): Response<RecordResponse>

    @GET("api/viewingrecords/{musicalId}/cast")
    suspend fun getCastList(
        @Header("Authorization") token: String,
        @Path("musicalId") musicalId: Int
    ): Response<CastResponse>

    @GET("api/viewingrecords/{postId}")
    suspend fun getViewingRecord(
        @Header("Authorization") token: String,
        @Path("postId") postId: Int
    ): ApiEnvelope<RecordDetailResponse>
}
