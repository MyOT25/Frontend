package com.example.myot.question.data

import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface QuestionService {

    @GET("api/questions")
    suspend fun getQuestions(
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): ApiResponse<List<QuestionListItemDto>?>

    @POST("api/questions/{questionId}/like")
    suspend fun likeQuestion(
        @Path("questionId") questionId: Long,
        @Header("Authorization") authorization: String
    ): ApiResponse<LikeActionDto?>

    @DELETE("api/questions/{questionId}/like")
    suspend fun unlikeQuestion(
        @Path("questionId") questionId: Long,
        @Header("Authorization") authorization: String
    ): ApiResponse<Unit?>

    @GET("api/questions/{questionId}/like/count")
    suspend fun getQuestionLikeCount(
        @Path("questionId") questionId: Long
    ): ApiResponse<LikeCountDto>

    @GET("api/questions/{questionId}")
    suspend fun getQuestionDetail(
        @Path("questionId") questionId: Long
    ): ApiResponse<QuestionDetailDto>

    @GET("api/answers/question/{questionId}")
    suspend fun getAnswersByQuestion(
        @Path("questionId") questionId: Long
    ): ApiResponse<List<AnswerDto>?>

    @GET("api/answers/{answerId}/like/count")
    suspend fun getAnswerLikeCount(
        @Path("answerId") answerId: Long
    ): ApiResponse<AnswerLikeCountDto?>
}