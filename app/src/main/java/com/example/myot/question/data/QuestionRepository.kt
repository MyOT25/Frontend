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

class QuestionRepository(
    private val service: QuestionService
) {
    private val viewTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")

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
        val token = AuthStore.accessToken ?: error("로그인이 필요합니다")
        val res = service.likeQuestion(questionId, AuthStore.bearer())
        val data = res.success?.data ?: error("Like failed: ${res.error}")
        data
    }

    suspend fun unlike(questionId: Long): Result<Unit> = runCatching {
        val token = AuthStore.accessToken ?: error("로그인이 필요합니다")
        val res = service.unlikeQuestion(questionId, AuthStore.bearer())
        if (res.resultType != "SUCCESS") error("Unlike failed: ${res.error}")
        Unit
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
}