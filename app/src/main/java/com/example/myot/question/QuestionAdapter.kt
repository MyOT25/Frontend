package com.example.myot.question

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

class QuestionAdapter(private val items: List<QuestionItem>) :
    RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>() {

    inner class QuestionViewHolder(val binding: ItemQuestionFeedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: QuestionItem) {
            binding.tvTitle.text = item.title
            binding.tvTime.text = item.time

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

            // 댓글 수는 고정 (0이면 GONE)
            binding.tvCommentCount.text = item.commentCount.toString()
            binding.tvCommentCount.visibility = if (item.commentCount == 0) View.GONE else View.VISIBLE

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
            if (!item.imageUrl.isNullOrEmpty()) {
                binding.ivThumbnail.visibility = View.VISIBLE
                Glide.with(binding.root)
                    .load(item.imageUrl)
                    .centerCrop()
                    .into(binding.ivThumbnail)
            } else {
                binding.ivThumbnail.visibility = View.INVISIBLE
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


}