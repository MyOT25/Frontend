package com.example.myot.retrofit2

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://43.203.70.205:3000/"
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val communityService: CommunityService = retrofit.create(CommunityService::class.java)
    val memorybookService: MemorybookService = retrofit.create(MemorybookService::class.java)
}