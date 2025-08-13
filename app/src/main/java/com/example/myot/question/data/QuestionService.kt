package com.example.myot.question.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
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

    // ✅ 댓글 좋아요 API (신규 스펙)
    @POST("api/questions/{questionId}/comments/{commentId}/like")
    suspend fun likeComment(
        @Path("questionId") questionId: Long,
        @Path("commentId") commentId: Long,
        @Header("Authorization") authorization: String
    ): ApiResponse<CommentLikeActionDto?>

    @DELETE("api/questions/{questionId}/comments/{commentId}/like")
    suspend fun unlikeComment(
        @Path("questionId") questionId: Long,
        @Path("commentId") commentId: Long,
        @Header("Authorization") authorization: String
    ): ApiResponse<Any?>

    @GET("api/questions/{questionId}/comments/{commentId}/like/count")
    suspend fun getCommentLikeCount(
        @Path("questionId") questionId: Long,
        @Path("commentId") commentId: Long
    ): ApiResponse<CommentLikeCountDto?>

    @GET("api/questions/{questionId}/comments/{commentId}/me")
    suspend fun getCommentLikedByMe(
        @Path("questionId") questionId: Long,
        @Path("commentId") commentId: Long,
        @Header("Authorization") authorization: String? = null
    ): ApiResponse<Boolean?>

    @GET("api/questions/{questionId}/me")
    suspend fun getQuestionMe(
        @Path("questionId") questionId: Long,
        @Header("Authorization") authorization: String? = null
    ): ApiResponse<QuestionMeDto?>

    @POST("api/questions/{questionId}/comments")
    suspend fun postComment(
        @Path("questionId") questionId: Long,
        @Header("Authorization") authorization: String,
        @Body body: CreateCommentRequestDto
    ): ApiResponse<AnswerDto?>
}