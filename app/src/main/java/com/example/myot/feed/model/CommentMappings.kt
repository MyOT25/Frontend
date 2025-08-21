package com.example.myot.feed.model

import com.example.myot.feed.data.CommentDto
import com.example.myot.feed.data.CreateCommentSuccess
import java.text.SimpleDateFormat
import java.util.*

fun CommentDto.toCommentItem(): CommentItem {
    val nickname = this.user?.nickname ?: this.user?.username ?: this.user?.loginId ?: ""
    val loginId = this.user?.loginId ?: this.user?.username ?: ""

    return CommentItem(
        id = this.id, // ← 서버에 id 있으면 그대로
        username = nickname,
        userid = loginId,
        content = this.content.orEmpty(),
        date = displayDateFromIso(this.createdAt),
        profileImageUrl = this.user?.profileImage,
        commentCount = 0, likeCount = 0, repostCount = 0, quoteCount = 0,
        isLiked = false, isReposted = false, isQuoted = false,
        isAnonymous = (this.anonymous == true)
    )
}

fun CreateCommentSuccess.toCommentItem(): CommentItem {
    val loginIdText = this.user?.loginId?.takeIf { !it.isNullOrBlank() }
    val handle = loginIdText?.let { "@$it" }
    return CommentItem(
        id = this.id, // ← 생성된 댓글 id
        username = this.user?.nickname ?: loginIdText ?: "익명",
        userid = handle ?: "",
        content = this.content.orEmpty(),
        date = this.createdAt ?: "",
        profileImageUrl = this.user?.profileImage,
        commentCount = 0, likeCount = 0, repostCount = 0, quoteCount = 0,
        isLiked = false, isReposted = false, isQuoted = false
    )
}

private fun displayDateFromIso(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX"
    )
    for (p in patterns) {
        try {
            val fmt = SimpleDateFormat(p, Locale.getDefault()).apply {
                if (p.contains("'Z'")) timeZone = TimeZone.getTimeZone("UTC")
            }
            val dt = fmt.parse(iso) ?: continue
            return SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(dt)
        } catch (_: Exception) {}
    }
    return iso
}
