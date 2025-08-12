package com.example.myot.question.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
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
    ): ApiResponse<Any?>

    @GET("api/questions/{questionId}/like/count")
    suspend fun getQuestionLikeCount(
        @Path("questionId") questionId: Long
    ): ApiResponse<LikeCountDto?>

    @GET("api/questions/{questionId}")
    suspend fun getQuestionDetail(
        @Path("questionId") questionId: Long
    ): ApiResponse<QuestionDetailDto>

    @GET("api/questions/{questionId}/comments")
    suspend fun getAnswersByQuestion(
        @Path("questionId") questionId: Long,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Header("Authorization") authorization: String? = null
    ): ApiResponse<CommentsPageDto?>

    @Multipart
    @POST("/api/questions")
    suspend fun createQuestion(
        @Part("title") title: RequestBody,
        @Part("content") content: RequestBody,
        @Part("tagIds") tagIdsJson: RequestBody,
        @Part("anonymous") anonymous: RequestBody? = null,
        @Part imageFiles: List<MultipartBody.Part>,
        @Header("Authorization") auth: String? = null
    ): ApiResponse<QuestionDetailDto>

    @POST("api/answers/{answerId}/like")
    suspend fun likeAnswer(
        @Path("answerId") answerId: Long,
        @Header("Authorization") authorization: String
    ): ApiResponse<AnswerLikeActionDto?>

    @DELETE("api/answers/{answerId}/like")
    suspend fun unlikeAnswer(
        @Path("answerId") answerId: Long,
        @Header("Authorization") authorization: String
    ): ApiResponse<Unit?>

    @GET("api/answers/{answerId}/like/count")
    suspend fun getAnswerLikeCount(
        @Path("answerId") answerId: Long
    ): ApiResponse<AnswerLikeCountDto?>
}