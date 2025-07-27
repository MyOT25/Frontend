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
    private val onItemClick: (QuestionItem) -> Unit
) : RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>() {

    inner class QuestionViewHolder(val binding: ItemQuestionFeedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: QuestionItem) {
            binding.tvTitle.text = item.title
            binding.tvTime.text = getTimeAgo(item.time)

            var isLiked = false
            var likeCount = item.likeCount
            updateLikeUI(likeCount, isLiked)

            // 좋아요 클릭 이벤트 (아이콘 + 숫자)
            val likeClickListener = View.OnClickListener {
                isLiked = !isLiked
                likeCount = if (isLiked) likeCount + 1 else likeCount - 1
                updateLikeUI(likeCount, isLiked)
            }

            binding.ivLike.setOnClickListener(likeClickListener)
            binding.tvLikeCount.setOnClickListener(likeClickListener)

            binding.tvCommentCount.text = item.commentCount.toString()

            if (item.commentCount == 0) {
                binding.tvCommentCount.visibility = View.GONE
                binding.ivComment.setColorFilter(
                    ContextCompat.getColor(binding.root.context, R.color.gray3),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            } else {
                binding.tvCommentCount.visibility = View.VISIBLE
                binding.ivComment.setColorFilter(
                    ContextCompat.getColor(binding.root.context, R.color.point_green),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }

            // 내용 + 해시태그 처리
            val fullText = item.content
            val hashtagRegex = "#\\S+".toRegex()
            val hashtags = hashtagRegex.findAll(fullText).map { it.value }.toList()

            var contentOnly = fullText.replace(hashtagRegex, "").trim()

            if (contentOnly.length > 32) {
                contentOnly = contentOnly.substring(0, 32) + "..."
            }

            val displayText = buildString {
                append(contentOnly)
                if (hashtags.isNotEmpty()) {
                    append("  ")
                    append(hashtags.joinToString(" "))
                }
            }

            val spannable = SpannableString(displayText)
            val startOfTags = displayText.indexOfFirst { it == '#' }
            if (startOfTags != -1) {
                for (hashtag in hashtags) {
                    val start = displayText.indexOf(hashtag, startIndex = startOfTags)
                    if (start != -1) {
                        spannable.setSpan(
                            ForegroundColorSpan(ContextCompat.getColor(binding.root.context, R.color.point_blue)),
                            start,
                            start + hashtag.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }
            binding.tvContent.text = spannable


            // 이미지 처리
            if (!item.imageUrls.isNullOrEmpty()) {
                binding.ivThumbnail.visibility = View.VISIBLE
                Glide.with(binding.root)
                    .load(item.imageUrls[0]) // 첫 번째 이미지 사용
                    .centerCrop()
                    .into(binding.ivThumbnail)
            } else {
                binding.ivThumbnail.visibility = View.INVISIBLE
            }

            // 피드 클릭 이벤트
            binding.root.setOnClickListener {
                onItemClick(item)
            }

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