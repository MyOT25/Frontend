package com.example.myot.retrofit2

import com.example.myot.ticket.calendar.model.CalendarEntryRequest
import com.example.myot.ticket.calendar.model.DeleteRecordResponse
import com.example.myot.ticket.calendar.model.MonthlyRecordsResponse
import com.example.myot.ticket.calendar.model.SaveRecordResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface CalendarService {
    @GET("api/viewingrecords/monthly-summary")
    fun getMonthlyRecords(
        @Header("Authorization") token: String,
        @Query("year") year: Int,
        @Query("month") month: Int
    ): Response<MonthlyRecordsResponse>

    @POST("api/viewingrecords")
    fun saveRecord(
        @Header("Authorization") token: String,
        @Body record: CalendarEntryRequest
    ): Response<SaveRecordResponse>

    @PUT("api/viewingrecords/{id}")
    fun updateRecord(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body record: CalendarEntryRequest
    ): Response<SaveRecordResponse>

    @DELETE("api/viewingrecords/{id}")
    fun deleteRecord(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<DeleteRecordResponse>
}