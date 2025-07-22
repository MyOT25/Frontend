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
            binding.tvTime.text = item.time
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

            // 내용 + 해시태그 컬러 처리
            val spannable = SpannableString(item.content)
            val hashtagPattern = "#\\S+".toRegex()
            hashtagPattern.findAll(item.content).forEach { match ->
                spannable.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(binding.root.context, R.color.point_blue)),
                    match.range.first,
                    match.range.last + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
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

            binding.tvLikeCount.text = likeCount.toString()
            binding.tvLikeCount.visibility = if (likeCount == 0) View.GONE else View.VISIBLE

            binding.ivLike.setImageResource(
                if (isLiked) R.drawable.ic_question_like_selected else R.drawable.ic_question_like_unselected
            )

            val color = if (isLiked) R.color.point_pink else R.color.gray2
            binding.tvLikeCount.setTextColor(ContextCompat.getColor(context, color))
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