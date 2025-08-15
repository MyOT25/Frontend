package com.example.myot.question.data

import com.example.myot.feed.model.CommentItem
import com.example.myot.question.model.AnswerItem
import com.example.myot.question.model.QuestionItem
import com.example.myot.retrofit2.AuthStore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import java.io.File
import java.io.InputStream

class QuestionRepository(
    private val service: QuestionService,
    private val contentResolver: ContentResolver
) {
    suspend fun fetchQuestions(page: Int?, limit: Int?): Result<List<QuestionItem>> {
        return try {
            val res = service.getQuestions(page, limit)

            val rawList: List<QuestionListItemDto> =
                res.success?.data ?: emptyList()

            val items = rawList.mapNotNull { it.toModelOrNull() }
            Result.success(items)
        } catch (e: Exception) {
            android.util.Log.e("QuestionRepo", "fetchQuestions failed", e)
            Result.failure(e)
        }
    }

    private fun QuestionListItemDto.toModelOrNull(): QuestionItem? {
        val safeTitle = title.ifBlank { return null }
        val safeContent = content.orEmpty()

        val nickname = user.nickname.ifBlank { "사용자" }
        val profile = user.profileImageUrl ?: user.profileImage

        val displayTime = try {
            val odt = java.time.OffsetDateTime.parse(createdAt)
            val local = odt.toInstant().atZone(java.time.ZoneId.systemDefault())
            local.format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))
        } catch (_: Exception) { createdAt }

        val tagNames: List<String> =
            (tagList ?: emptyList()).filter { it.isNotBlank() }

        return QuestionItem(
            id = id,
            title = safeTitle,
            content = safeContent,
            username = nickname,
            profileImage = profile,
            createdAt = displayTime,
            tags = tagNames,
            isAnonymous = isAnonymous,
            thumbnailUrl = thumbnailUrl,
            likeCount = likeCount,
            commentCount = commentCount
        )
    }

    suspend fun like(questionId: Long): Result<LikeActionDto> = runCatching {
        val token = AuthStore.accessToken
            ?: error("로그인이 필요합니다")
        val auth = "Bearer $token"

        val res = service.likeQuestion(questionId, auth)
        res.success?.data ?: error("Like failed: ${res.error}")
    }

    suspend fun unlike(questionId: Long): Result<Unit> = runCatching {
        val token = AuthStore.accessToken
            ?: error("로그인이 필요합니다")
        val auth = "Bearer $token"

        val res = service.unlikeQuestion(questionId, auth)
        if (res.resultType == "SUCCESS") Unit else error("Unlike failed: ${res.error}")
    }

    suspend fun fetchQuestionDetail(id: Long): Result<Pair<QuestionItem, List<String>>> = runCatching {
        val res = service.getQuestionDetail(id)
        val dto = res.success?.data ?: error("detail failed: empty body")

        val displayTime = try {
            val odt = java.time.OffsetDateTime.parse(dto.createdAt)
            val local = odt.toInstant().atZone(java.time.ZoneId.systemDefault())
            local.format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))
        } catch (_: Exception) { dto.createdAt }

        val header = QuestionItem(
            id = dto.id,
            title = dto.title,
            content = dto.content,
            username = dto.user.username,
            profileImage = dto.user.profileImage,
            createdAt = displayTime,
            tags = dto.tagList,
            isAnonymous = dto.isAnonymous ?: false,
            thumbnailUrl = dto.thumbnailUrl,
            likeCount = dto.likeCount,
            commentCount = dto.commentCount
        )
        header to (dto.imageUrls ?: emptyList())
    }

    private val ANSWER_TIME_FMT: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")

    private fun AnswerDto.toAnswerItemOrNull(): AnswerItem? {
        val id = this.id ?: return null
        val name = this.user?.username ?: "사용자"
        val profile = this.user?.profileImage
        val anonymous = this.isAnonymous ?: false

        val dispTime = try {
            OffsetDateTime.parse(this.createdAt ?: "")
                .format(ANSWER_TIME_FMT)
        } catch (_: Exception) { this.createdAt ?: "" }

        return AnswerItem(
            id = id,
            content = this.content.orEmpty(),
            createdAt = dispTime,
            authorName = name,
            authorProfileImage = profile,
            isAnonymous = anonymous
        )
    }

    // AnswerItem → CommentItem 변환
    private fun AnswerItem.toCommentItem(likeCount: Int): CommentItem {
        return CommentItem(
            username = if (isAnonymous) "익명의 해결사" else authorName,
            userid = "@-",
            content = content,
            date = createdAt,
            commentCount = 0,
            likeCount = likeCount,
            repostCount = 0,
            quoteCount = 0,
            isAnonymous = isAnonymous
        )
    }

    suspend fun fetchAnswers(questionId: Long): Result<List<AnswerItem>> = runCatching {
        val res = service.getAnswersByQuestion(
            questionId = questionId,
            page = 1,
            size = 50,
            authorization = AuthStore.bearerOrNull()
        )
        val raw = res.success?.data?.comments ?: emptyList()
        android.util.Log.d("QuestionRepo", "comments size=${raw.size} for q=$questionId")
        raw.mapNotNull { it.toAnswerItemOrNull() }
    }

    suspend fun likeComment(questionId: Long, commentId: Long): Result<CommentLikeActionDto> = runCatching {
        val token = AuthStore.accessToken ?: error("로그인이 필요합니다")
        val auth = "Bearer $token"
        val res = service.likeComment(questionId, commentId, auth)
        res.success?.data ?: error("댓글 좋아요 실패: ${res.error}")
    }

    suspend fun unlikeComment(questionId: Long, commentId: Long): Result<Unit> = runCatching {
        val token = AuthStore.accessToken ?: error("로그인이 필요합니다")
        val auth = "Bearer $token"
        val res = service.unlikeComment(questionId, commentId, auth)
        if (res.resultType == "SUCCESS") Unit else error("댓글 좋아요 취소 실패: ${res.error}")
    }

    suspend fun getCommentLikeCount(questionId: Long, commentId: Long): Result<Int> = runCatching {
        val res = service.getCommentLikeCount(questionId, commentId)
        res.success?.data?.likeCount ?: 0
    }

    suspend fun getCommentLikedByMe(questionId: Long, commentId: Long): Result<Boolean> = runCatching {
        val res = service.getCommentLikedByMe(questionId, commentId, AuthStore.bearerOrNull())
        res.success?.data ?: false
    }

    suspend fun postQuestionMultipart(
        title: String,
        content: String,
        tagIds: List<Long>,
        imageUris: List<Uri>,
        anonymous: Boolean?
    ): Result<QuestionItem> = runCatching {
        val titleRb    = title.toRequestBody(TEXT)
        val contentRb  = content.toRequestBody(TEXT)
        val tagIdsJson = tagIds.toString().toRequestBody(TEXT)

        val isAnonStr     = if (anonymous == true) "true" else "false"
        val isAnonymousRb = isAnonStr.toRequestBody(TEXT)

        val parts = imageUris.take(5).mapNotNull { uri ->
            uriToPart(uri, formFieldName = "imageFiles")
        }

        val token = AuthStore.accessToken ?: error("로그인이 필요합니다")
        val authHeader = "Bearer $token"

        val res = service.createQuestion(
            title = titleRb,
            content = contentRb,
            tagIdsJson = tagIdsJson,
            isAnonymous = isAnonymousRb,
            imageFiles = parts,
            auth = authHeader
        )

        val dto = res.success?.data ?: error("서버 응답에 data 없음")
        dto.toDomain()
    }

    private val TEXT: MediaType = "text/plain".toMediaType()

    private fun uriToPart(uri: Uri, formFieldName: String): MultipartBody.Part? {
        return try {
            val fileName = queryFileName(uri) ?: "image_${System.currentTimeMillis()}.jpg"
            val tempFile = copyToTemp(uri, fileName)
            val mime = guessMime(fileName)
            val rb = tempFile.asRequestBody(mime)
            MultipartBody.Part.createFormData(formFieldName, fileName, rb)
        } catch (_: Exception) {
            null
        }
    }

    private fun copyToTemp(uri: Uri, fileName: String): File {
        val input: InputStream = contentResolver.openInputStream(uri)!!
        val ext = fileName.substringAfterLast('.', "jpg")
        val temp = File.createTempFile("upload_", ".$ext")
        temp.outputStream().use { out -> input.copyTo(out) }
        input.close()
        return temp
    }

    private fun queryFileName(uri: Uri): String? {
        var name: String? = null
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && it.moveToFirst()) name = it.getString(idx)
        }
        return name
    }

    private fun guessMime(fileName: String): MediaType {
        return when (fileName.substringAfterLast('.', "").lowercase()) {
            "png" -> "image/png".toMediaType()
            "webp" -> "image/webp".toMediaType()
            "jpg", "jpeg" -> "image/jpeg".toMediaType()
            else -> "application/octet-stream".toMediaType()
        }
    }

    suspend fun getLikeCountViaList(
        questionId: Long,
        page: Int = 1,
        limit: Int = 20
    ): Result<Int> = runCatching {
        val res = service.getQuestions(page, limit)
        val list = res.success?.data ?: emptyList()
        val target = list.firstOrNull { it.id == questionId }
            ?: error("question $questionId not found in list(page=$page)")
        target.likeCount ?: 0
    }

    suspend fun fetchMyInteraction(questionId: Long): Result<QuestionMeDto> = runCatching {
        val res = service.getQuestionMe(questionId, AuthStore.bearerOrNull())
        res.success?.data ?: QuestionMeDto(hasLiked = false, hasCommented = false)
    }

    suspend fun createComment(
        questionId: Long,
        content: String,
        isAnonymous: Boolean
    ): Result<AnswerItem> = runCatching {
        val token = AuthStore.accessToken ?: error("로그인이 필요합니다")
        val auth = "Bearer $token"

        val res = service.postComment(
            questionId = questionId,
            authorization = auth,
            body = CreateCommentRequestDto(content, isAnonymous)
        )
        val dto = res.success?.data ?: error("댓글 등록 실패: ${res.error}")
        dto.toAnswerItemOrNull() ?: error("잘못된 응답")
    }

    suspend fun deleteQuestion(questionId: Long): Result<Unit> = runCatching {
        val auth = AuthStore.bearerOrNull() ?: error("로그인이 필요합니다")
        val res = service.deleteQuestion(questionId, auth)
        if (res.resultType == "SUCCESS") Unit
    }

}