package com.example.myot.feed.model

import com.example.myot.feed.data.CommentDto
import java.text.SimpleDateFormat
import java.util.*

fun CommentDto.toCommentItem(): CommentItem {
    val nickname = this.user?.nickname ?: this.user?.username ?: this.user?.loginId ?: ""
    val loginId = this.user?.loginId ?: this.user?.username ?: ""

    return CommentItem(
        username = nickname,
        userid = loginId,
        content = this.content.orEmpty(),
        date = displayDateFromIso(this.createdAt),
        profileImageUrl = this.user?.profileImage,

        // 서버 응답에 카운트/상태 정보가 없으므로 기본값으로 세팅
        commentCount = 0,
        likeCount = 0,
        repostCount = 0,
        quoteCount = 0,

        isLiked = false,
        isReposted = false,
        isQuoted = false,

        isAnonymous = (this.anonymous == true)
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