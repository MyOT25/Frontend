package com.example.myot.retrofit2

import com.example.myot.profile.data.ProfileFeedService
import com.example.myot.question.data.QuestionService
import com.example.myot.signup.data.SignupCommunityService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.jvm.java

object RetrofitClient {
    private const val BASE_URL = "http://43.203.70.205:3000/"
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val communityService: CommunityService = retrofit.create(CommunityService::class.java)
    val memorybookService: MemorybookService = retrofit.create(MemorybookService::class.java)
    val questionService: QuestionService = retrofit.create(QuestionService::class.java)
    val ticketService: TicketService = retrofit.create(TicketService::class.java)
    val profileFeedService: ProfileFeedService = retrofit.create(ProfileFeedService::class.java)
    val authService: AuthService = retrofit.create(AuthService::class.java)
    val signupCommunityService: SignupCommunityService = retrofit.create(SignupCommunityService::class.java)
    val bookService: BookService = retrofit.create(BookService::class.java)
    val calendarService: CalendarService = retrofit.create(CalendarService::class.java)
}