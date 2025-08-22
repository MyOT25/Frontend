package com.example.myot.question.adapter

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myot.R
import com.example.myot.databinding.ItemQuestionFeedBinding
import com.example.myot.question.model.QuestionItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.floor

class QuestionAdapter(
    private val items: List<QuestionItem>,
    private val onItemClick: (QuestionItem) -> Unit,
    private val onLikeClick: (questionId: Long, isLikedNow: Boolean) -> Unit,
    private val getLiked: (questionId: Long) -> Boolean,
    private val getLikeCount: (questionId: Long) -> Int,
    private val getQuestionCommented: (questionId: Long) -> Boolean
) : RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>() {

    inner class QuestionViewHolder(val binding: ItemQuestionFeedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: QuestionItem) {
            binding.tvTitle.text = item.title
            binding.tvTime.text = getTimeAgo(item.createdAt)

            val contentOnly = item.content.trim().let { if (it.length > 42) it.substring(0, 42) + "..." else it }
            val tagsText = if (item.tags.isNotEmpty()) item.tags.joinToString(prefix = "  #", separator = " #") else ""
            val displayText = contentOnly + tagsText

            val spannable = SpannableString(displayText)
            val ctx = binding.root.context
            val pointBlue = ContextCompat.getColor(ctx, R.color.point_blue)

            val regex = Regex("#[^\\s]+")
            regex.findAll(displayText).forEach { m ->
                val start = m.range.first
                val end = m.range.last + 1
                spannable.setSpan(
                    ForegroundColorSpan(pointBlue),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            binding.tvContent.text = spannable

            // 썸네일 기능 적용
            val thumb = item.thumbnailUrl
            if (!thumb.isNullOrBlank()) {
                binding.ivThumbnail.visibility = View.VISIBLE
                Glide.with(binding.root)
                    .load(thumb)
                    .centerCrop()
                    .into(binding.ivThumbnail)
            } else {
                binding.ivThumbnail.setImageDrawable(null)
                binding.ivThumbnail.visibility = View.INVISIBLE
            }

            // 좋아요 api
            val isLiked = getLiked(item.id)
            val likeCount = getLikeCount(item.id)
            updateLikeUI(likeCount, isLiked)
            binding.ivLike.setOnClickListener { onLikeClick(item.id, isLiked) }
            binding.tvLikeCount.setOnClickListener { onLikeClick(item.id, isLiked) }

            // 답변 api
            val cc = item.commentCount ?: 0
            binding.tvCommentCount.text = cc.toString()
            binding.tvCommentCount.visibility = if (cc == 0) View.GONE else View.VISIBLE

            val commentedByMe = getQuestionCommented(item.id)
            if (commentedByMe) {
                binding.ivComment.setImageResource(R.drawable.ic_question_comment_selected)
                binding.ivComment.clearColorFilter()
            } else {
                binding.ivComment.setImageResource(R.drawable.ic_question_comment)
                val tintRes = if (cc > 0) R.color.point_green else R.color.gray3
                binding.ivComment.setColorFilter(
                    ContextCompat.getColor(binding.root.context, tintRes),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
            binding.root.setOnClickListener { onItemClick(item) }
        }


        private fun updateLikeUI(likeCount: Int, isLiked: Boolean) {
            val context = binding.root.context

            // 숫자 표시 조건
            binding.tvLikeCount.text = likeCount.toString()
            binding.tvLikeCount.visibility = if (likeCount == 0) View.GONE else View.VISIBLE

            // 아이콘 리소스 설정
            binding.ivLike.setImageResource(
                if (isLiked) {
                    R.drawable.ic_question_like_selected
                } else {
                    R.drawable.ic_question_like_unselected
                }
            )

            // 숫자 색상 설정
            val countColor = when {
                isLiked -> R.color.point_pink
                likeCount > 0 -> R.color.point_pink
                else -> R.color.gray2
            }
            binding.tvLikeCount.setTextColor(ContextCompat.getColor(context, countColor))

            val iconTint = when {
                isLiked -> null
                likeCount > 0 -> R.color.point_pink
                else -> R.color.gray2
            }

            if (iconTint != null) {
                binding.ivLike.setColorFilter(ContextCompat.getColor(context, iconTint), android.graphics.PorterDuff.Mode.SRC_IN)
            } else {
                binding.ivLike.clearColorFilter()
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val binding = ItemQuestionFeedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    private fun getTimeAgo(dateStr: String): String {
        val format = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        val postTime = format.parse(dateStr) ?: return ""

        val now = Date()
        val diffMillis = now.time - postTime.time

        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
        val days = TimeUnit.MILLISECONDS.toDays(diffMillis)
        val months = floor(days / 30.0).toInt()
        val years = floor(days / 365.0).toInt()

        return when {
            minutes < 1 -> "방금 전"
            minutes < 60 -> "${minutes}분 전"
            hours < 24 -> "${hours}시간 전"
            days <= 30 -> "${days}일 전"
            days < 365 -> "${months}개월 전"
            else -> "${years}년 전"
        }
    }

}