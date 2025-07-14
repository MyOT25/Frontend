package com.example.myot.question

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.R
import com.example.myot.databinding.ItemQuestionDetailBinding

class QuestionDetailAdapter(private val item: QuestionItem) :
    RecyclerView.Adapter<QuestionDetailAdapter.DetailViewHolder>() {

    inner class DetailViewHolder(val binding: ItemQuestionDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.tvDetailTitle.text = item.title

            // ✅ 좋아요 초기값 및 클릭 이벤트
            var isLiked = false
            var likeCount = item.likeCount
            updateLikeUI(likeCount, isLiked)

            val likeClickListener = View.OnClickListener {
                isLiked = !isLiked
                likeCount = if (isLiked) likeCount + 1 else likeCount - 1
                updateLikeUI(likeCount, isLiked)
            }
            binding.ivLike.setOnClickListener(likeClickListener)
            binding.tvLikeCount.setOnClickListener(likeClickListener)

            // ✅ 댓글 수 표시 및 GONE 처리
            binding.tvCommentCount.text = item.commentCount.toString()
            binding.tvCommentCount.visibility =
                if (item.commentCount == 0) View.GONE else View.VISIBLE

            // ✅ 해시태그 컬러 처리
            val spannable = SpannableString(item.content)
            val hashtagPattern = "#\\S+".toRegex()
            hashtagPattern.findAll(item.content).forEach { match ->
                spannable.setSpan(
                    ForegroundColorSpan(
                        ContextCompat.getColor(binding.root.context, R.color.point_blue)
                    ),
                    match.range.first,
                    match.range.last + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            binding.tvDetailContent.text = spannable
        }

        private fun updateLikeUI(count: Int, liked: Boolean) {
            binding.tvLikeCount.text = count.toString()
            binding.tvLikeCount.visibility = if (count == 0) View.GONE else View.VISIBLE
            binding.ivLike.setImageResource(
                if (liked) R.drawable.ic_question_like_selected else R.drawable.ic_question_like_unselected
            )
            binding.tvLikeCount.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (liked) R.color.point_pink else R.color.gray2
                )
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val binding = ItemQuestionDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = 1  // 지금은 질문 하나만 보이게
}