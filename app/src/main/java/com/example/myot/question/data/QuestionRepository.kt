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
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
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
        // 필수 필드 없으면 아이템 스킵
        val safeTitle = title ?: return null
        val safeContent = content ?: ""
        val nickname = user?.nickname ?: "사용자"
        val profile = user?.profileImage

        val displayTime = try {
            val src = createdAt ?: return null
            java.time.OffsetDateTime.parse(src)
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))
        } catch (_: Exception) {
            createdAt ?: return null
        }

        val tagNames: List<String> =
            (questionTags ?: emptyList()).mapNotNull { it.tag?.tagName }.filter { it.isNotBlank() }

        return QuestionItem(
            id = id,
            title = safeTitle,
            content = safeContent,
            username = nickname,
            profileImage = profile,
            createdAt = displayTime,
            tags = tagNames
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

    suspend fun getLikeCount(questionId: Long): Result<Int> {
        return try {
            val res = service.getQuestionLikeCount(questionId)
            val count = res.success?.data?.likeCount
                ?: return Result.failure(IllegalStateException("Count failed: empty body or not success (${res.error})"))
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchQuestionDetail(id: Long): Result<Pair<QuestionItem, List<String>>> {
        return try {
            val res = service.getQuestionDetail(id)
            val dto = res.success?.data
                ?: return Result.failure(IllegalStateException("detail failed: empty body or not success (${res.error})"))

            val displayTime = try {
                OffsetDateTime.parse(dto.createdAt)
                    .format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))
            } catch (_: Exception) {
                dto.createdAt
            }

            val header = QuestionItem(
                id = dto.id,
                title = dto.title,
                content = dto.content,
                username = dto.user.username,
                profileImage = dto.user.profileImage,
                createdAt = displayTime,
                tags = dto.tagList
            )
            Result.success(header to dto.imageUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private val ANSWER_TIME_FMT: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")

    private fun AnswerDto.toAnswerItemOrNull(): AnswerItem? {
        val id = this.id ?: return null
        val name = this.user?.username ?: "사용자"
        val profile = this.user?.profileImage

        val dispTime = try {
            OffsetDateTime.parse(this.createdAt ?: "")
                .format(ANSWER_TIME_FMT)
        } catch (_: Exception) {
            this.createdAt ?: ""
        }

        return AnswerItem(
            id = id,
            content = this.content.orEmpty(),
            createdAt = dispTime,
            authorName = name,
            authorProfileImage = profile,
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

    suspend fun fetchAnswerComments(questionId: Long): Result<List<CommentItem>> = runCatching {
        val answers: List<AnswerItem> = fetchAnswers(questionId).getOrThrow()

        val likeMap: Map<Long, Int> = coroutineScope {
            answers.map { a ->
                async { a.id to fetchAnswerLikeCount(a.id).getOrElse { 0 } }
            }.awaitAll().toMap()
        }

        answers.map { a -> a.toCommentItem(likeMap[a.id] ?: 0) }
    }

    suspend fun fetchAnswers(questionId: Long): Result<List<AnswerItem>> = runCatching {
        val res = service.getAnswersByQuestion(questionId)
        val raw = res.success?.data ?: emptyList()
        raw.mapNotNull { it.toAnswerItemOrNull() }
    }

    suspend fun fetchAnswerLikeCount(answerId: Long): Result<Int> = runCatching {
        val res = service.getAnswerLikeCount(answerId)
        res.success?.data?.likeCount ?: 0
    }

    suspend fun likeAnswer(answerId: Long): Result<AnswerLikeActionDto> = runCatching {
        val token = AuthStore.accessToken ?: error("로그인이 필요합니다")
        val auth = "Bearer $token"
        val res = service.likeAnswer(answerId, auth)
        res.success?.data ?: error("답변 좋아요 실패: ${res.error}")
    }

    suspend fun unlikeAnswer(answerId: Long): Result<Unit> = runCatching {
        val token = AuthStore.accessToken ?: error("로그인이 필요합니다")
        val auth = "Bearer $token"
        val res = service.unlikeAnswer(answerId, auth)
        if (res.resultType == "SUCCESS") Unit else error("답변 좋아요 취소 실패: ${res.error}")
    }

    suspend fun getAnswerLikeCount(answerId: Long): Result<Int> = runCatching {
        val res = service.getAnswerLikeCount(answerId)
        res.success?.data?.likeCount ?: 0
    }

    suspend fun postQuestionMultipart(
        title: String,
        content: String,
        tagIds: List<Long>,
        imageUris: List<Uri>,
        anonymous: Boolean?
    ): Result<QuestionItem> = runCatching {
        val titleRb = title.toRequestBody(TEXT)
        val contentRb = content.toRequestBody(TEXT)
        val tagIdsJson = tagIds.toString().toRequestBody(TEXT)
        val anonymousRb = anonymous.toString().toRequestBody(TEXT)

        val parts = imageUris.take(5).mapNotNull { uri ->
            uriToPart(uri, formFieldName = "imageFiles")
        }

        val res = service.createQuestion(
            title = titleRb,
            content = contentRb,
            tagIdsJson = tagIdsJson,
            anonymous = anonymousRb,
            imageFiles = parts,
            auth = AuthStore.bearerOrThrow()
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
}