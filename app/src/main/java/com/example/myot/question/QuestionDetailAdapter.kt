package com.example.myot.question

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.myot.R
import com.example.myot.databinding.ItemQuestionCommentBinding
import com.example.myot.databinding.ItemQuestionDetailBinding
import com.example.myot.feed.CommentItem

class QuestionDetailAdapter(
    private val item: QuestionItem,
    private val comments: List<CommentItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_DETAIL = 0
        private const val TYPE_COMMENT = 1
    }

    inner class DetailViewHolder(val binding: ItemQuestionDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.tvDetailTitle.text = item.title
            binding.tvDetailTime.text = item.time

            val imageList = item.imageUrls ?: emptyList()

            if (imageList.isNotEmpty()) {
                binding.vpImages.adapter = QuestionImagePagerAdapter(imageList)
                binding.vpImages.visibility = View.VISIBLE

                if (imageList.size > 1) {
                    binding.imageIndicatorContainer.visibility = View.VISIBLE
                    updateIndicator(0, imageList.size)
                    binding.vpImages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            super.onPageSelected(position)
                            updateIndicator(position, imageList.size)
                        }
                    })
                } else {
                    binding.imageIndicatorContainer.visibility = View.GONE
                }
            } else {
                binding.vpImages.visibility = View.GONE
                binding.imageIndicatorContainer.visibility = View.GONE
            }

            // 좋아요
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

            // 댓글 수
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
            binding.tvDetailContent.text = spannable


            // ViewPager 이미지 처리
            item.imageUrls?.let { imageList ->
                if (imageList.isNotEmpty()) {
                    binding.vpImages.adapter = QuestionImagePagerAdapter(imageList)
                    binding.vpImages.visibility = View.VISIBLE
                } else {
                    binding.vpImages.visibility = View.GONE
                }
            } ?: run {
                binding.vpImages.visibility = View.GONE
            }
        }

        private fun updateIndicator(position: Int, itemCount: Int) {
            val indicator = binding.imageIndicator
            val container = binding.imageIndicatorContainer

            container.post {
                val totalWidth = container.width
                val indicatorWidth = totalWidth / itemCount
                val indicatorX = indicatorWidth * position

                val layoutParams = indicator.layoutParams
                layoutParams.width = indicatorWidth
                indicator.layoutParams = layoutParams
                indicator.translationX = indicatorX.toFloat()
            }
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

    inner class CommentViewHolder(val binding: ItemQuestionCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: CommentItem) {
            binding.tvName.text = comment.username
            binding.tvContent.text = comment.content
            binding.tvTime.text = comment.date

            binding.tvCommentCount.text = comment.commentCount.toString()
            if (comment.commentCount == 0) {
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

            var isLiked = false
            var likeCount = comment.likeCount
            updateLikeUI(likeCount, isLiked)

            val likeClickListener = View.OnClickListener {
                isLiked = !isLiked
                likeCount = if (isLiked) likeCount + 1 else likeCount - 1
                updateLikeUI(likeCount, isLiked)
            }
            binding.ivLike.setOnClickListener(likeClickListener)
            binding.tvLikeCount.setOnClickListener(likeClickListener)
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

    override fun getItemCount(): Int = 1 + comments.size

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_DETAIL else TYPE_COMMENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_DETAIL) {
            val binding = ItemQuestionDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            DetailViewHolder(binding)
        } else {
            val binding = ItemQuestionCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            CommentViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DetailViewHolder) {
            holder.bind()
        } else if (holder is CommentViewHolder) {
            holder.bind(comments[position - 1])
        }
    }
}