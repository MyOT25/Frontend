    package com.example.myot.feed.data

    import com.example.myot.feed.model.PostDetailResponse
    import retrofit2.Response
    import retrofit2.http.DELETE
    import retrofit2.http.GET
    import retrofit2.http.Header
    import retrofit2.http.Path
    import retrofit2.http.POST
    import retrofit2.http.Query

    data class SimpleResult(
        val resultType: String? = null,
        val message: String? = null
    )

    data class LikeToggleResponse(
        val userId: Long,
        val postId: Long,
        val isLiked: Boolean,
        val message: String
    )

    data class BookmarkToggleResponse(
        val userId: Long,
        val postId: Long,
        val isBookmarked: Boolean,
        val message: String
    )

    data class CommentsListResponse(
        val resultType: String? = null,
        val error: Any? = null,
        val success: List<CommentDto>? = null
    )

    data class PostLikesEnvelope(
        val resultType: String? = null,
        val error: Any? = null,
        val success: PostLikesData? = null
    )

    data class PostLikesData(
        val message: String? = null,
        val total: Int? = null,
        val page: Int? = null,
        val limit: Int? = null,
        val users: List<PostLikeUser>? = null
    )

    data class PostLikeUser(
        val id: Long?,
        val loginId: String?,
        val nickname: String?,
        val profileImage: String?
    )

    data class RepostedUsersEnvelope(
        val resultType: String? = null,
        val error: Any? = null,
        val success: List<RepostEntry>? = null
    )

    data class RepostEntry(
        val id: Long?,
        val userId: Long?,
        val isRepost: Boolean?,
        val repostType: String?,
        val repostTargetId: Long?,
        val createdAt: String?,
        val updatedAt: String?,
        val user: RepostUser?
    )

    data class RepostUser(
        val id: Long?,
        val loginId: String?,
        val nickname: String?,
        val profileImage: String?
    )


    interface FeedService {
        @GET("api/posts/{postId}")
        suspend fun getPostDetail(
            @Header("Authorization") token: String,
            @Path("postId") postId: Long
        ): Response<PostDetailResponse>

        @DELETE("api/posts/{postId}")
        suspend fun deletePost(
            @Header("Authorization") token: String,
            @Path("postId") postId: Long
        ): Response<SimpleResult>

        @POST("api/posts/{postId}/like")
        suspend fun toggleLike(
            @Header("Authorization") token: String,
            @Path("postId") postId: Long
        ): Response<LikeToggleResponse>

        @POST("api/posts/{postId}/bookmarks")
        suspend fun addBookmark(
            @Header("Authorization") token: String,
            @Path("postId") postId: Long
        ): Response<BookmarkToggleResponse>

        @DELETE("api/posts/{postId}/bookmarks")
        suspend fun deleteBookmark(
            @Header("Authorization") token: String,
            @Path("postId") postId: Long
        ): Response<BookmarkToggleResponse>

        @GET("api/posts/{postId}/comments")
        suspend fun getPostComments(
            @Header("Authorization") token: String,
            @Path("postId") postId: Long
        ): Response<CommentsListResponse>

        @GET("api/posts/{postId}/likes")
        suspend fun getPostLikes(
            @Header("Authorization") token: String,
            @Path("postId") postId: Long,
            @Query("page") page: Int,
            @Query("limit") limit: Int
        ): Response<PostLikesEnvelope>

        @GET("api/posts/{postId}/reposted-users")
        suspend fun getRepostedUsers(
            @Header("Authorization") token: String,
            @Path("postId") postId: Long,
            @Query("page") page: Int = 1,
            @Query("limit") limit: Int = 20
        ): Response<RepostedUsersEnvelope>
    }