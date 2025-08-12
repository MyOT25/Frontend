package com.example.myot.question.adapter

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.myot.R
import com.example.myot.databinding.ItemQuestionCommentBinding
import com.example.myot.databinding.ItemQuestionDetailBinding
import com.example.myot.question.model.AnswerItem
import com.example.myot.question.model.QuestionItem


class QuestionDetailAdapter(
    private val item: QuestionItem,
    private val imageUrls: List<String>,
    private val answers: List<AnswerItem>,

    private val onQuestionLikeClick: (questionId: Long, isLikedNow: Boolean) -> Unit,
    private val getQuestionLiked: (questionId: Long) -> Boolean,
    private val getQuestionLikeCount: (questionId: Long) -> Int,

    private val onAnswerLikeClick: (answerId: Long, isLikedNow: Boolean) -> Unit,
    private val getAnswerLiked: (answerId: Long) -> Boolean,
    private val getAnswerLikeCount: (answerId: Long) -> Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_PADDING = 2
        private const val TYPE_DETAIL = 0
        private const val TYPE_COMMENT = 1
    }

    inner class DetailViewHolder(val binding: ItemQuestionDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.tvDetailTitle.text = item.title
            binding.tvDetailTime.text = item.createdAt

            // 익명 분기 (현재는 항상 실명 표시)
            val isAnonymous = false // 나중에 API에 isAnonymous 오면 여기로 분기

            if (isAnonymous) {
                binding.ivProfile.visibility = View.GONE
                binding.tvUsername.text = "익명 질문"
                binding.tvUsername.setTextColor(ContextCompat.getColor(binding.root.context, R.color.point_green))
            } else {
                binding.ivProfile.visibility = View.VISIBLE
                binding.tvUsername.text = item.username
                binding.tvUsername.setTextColor(ContextCompat.getColor(binding.root.context, R.color.point_purple))
                if (item.profileImage != null) {
                    Glide.with(binding.root)
                        .load(item.profileImage)
                        .circleCrop()
                        .into(binding.ivProfile)
                } else {
                    binding.ivProfile.setImageResource(R.drawable.ic_profile_outline)
                }
            }

            // 내용 + 태그 처리
            val content = item.content
            val tagsText = if (item.tags.isNotEmpty()) item.tags.joinToString(prefix = "\n#", separator = " #") else ""
            val full = content + tagsText
            val spannable = SpannableString(full)
            if (item.tags.isNotEmpty()) {
                item.tags.forEach { tag ->
                    val hash = "#$tag"
                    val start = full.indexOf(hash)
                    if (start >= 0) {
                        spannable.setSpan(
                            ForegroundColorSpan(ContextCompat.getColor(binding.root.context, R.color.point_blue)),
                            start, start + hash.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }
            binding.tvDetailContent.text = spannable

            // 이미지 처리 (기존 로직 그대로)
            if (imageUrls.isNotEmpty()) {
                binding.vpImages.adapter = QuestionImagePagerAdapter(imageUrls)
                binding.vpImages.visibility = View.VISIBLE

                if (imageUrls.size > 1) {
                    binding.imageIndicatorContainer.visibility = View.VISIBLE
                    updateIndicator(0, imageUrls.size)
                    binding.vpImages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            super.onPageSelected(position)
                            updateIndicator(position, imageUrls.size)
                        }
                    })
                } else {
                    binding.imageIndicatorContainer.visibility = View.GONE
                }
            } else {
                binding.vpImages.visibility = View.GONE
                binding.imageIndicatorContainer.visibility = View.GONE
            }

            val likedAtBind = getQuestionLiked(item.id)
            val countAtBind = getQuestionLikeCount(item.id)
            updateLikeUI(countAtBind, likedAtBind)

            val click = View.OnClickListener {
                val current = getQuestionLiked(item.id)
                onQuestionLikeClick(item.id, current)
            }
            binding.ivLike.setOnClickListener(click)
            binding.tvLikeCount.setOnClickListener(click)

            // 댓글 수는 서버 연동 전이면 숨김 유지
            binding.tvCommentCount.visibility = View.GONE
            binding.ivComment.setColorFilter(
                ContextCompat.getColor(binding.root.context, R.color.gray3),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
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
            val context = binding.root.context

            binding.tvLikeCount.text = count.toString()
            binding.tvLikeCount.visibility = if (count == 0) View.GONE else View.VISIBLE

            binding.ivLike.setImageResource(
                if (liked) R.drawable.ic_question_like_selected else R.drawable.ic_question_like_unselected
            )

            val countColor = when {
                liked -> R.color.point_pink
                count > 0 -> R.color.point_pink
                else -> R.color.gray2
            }
            binding.tvLikeCount.setTextColor(ContextCompat.getColor(context, countColor))

            val iconTint = when {
                liked -> null
                count > 0 -> R.color.point_pink
                else -> R.color.gray2
            }

            if (iconTint != null) {
                binding.ivLike.setColorFilter(ContextCompat.getColor(context, iconTint), android.graphics.PorterDuff.Mode.SRC_IN)
            } else {
                binding.ivLike.clearColorFilter()
            }
        }
    }

    inner class CommentViewHolder(val binding: ItemQuestionCommentBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(answer: AnswerItem) {
            val context = binding.root.context

            // 이름/프로필
            val layoutParams = binding.ivProfile.layoutParams as ViewGroup.MarginLayoutParams
            if (answer.isAnonymous) {
                binding.tvName.text = "익명의 해결사"
                binding.tvName.setTextColor(ContextCompat.getColor(context, R.color.point_green))
                binding.ivProfile.setImageResource(R.drawable.ic_a_mark)
                layoutParams.width = context.resources.getDimensionPixelSize(R.dimen.dp_16)
                layoutParams.height = context.resources.getDimensionPixelSize(R.dimen.dp_16)
                layoutParams.topMargin = context.resources.getDimensionPixelSize(R.dimen.dp_3)
            } else {
                binding.tvName.text = answer.authorName
                binding.tvName.setTextColor(ContextCompat.getColor(context, R.color.point_purple))
                binding.ivProfile.setImageResource(R.drawable.ic_profile)
                layoutParams.width = context.resources.getDimensionPixelSize(R.dimen.dp_20)
                layoutParams.height = context.resources.getDimensionPixelSize(R.dimen.dp_20)
                layoutParams.topMargin = context.resources.getDimensionPixelSize(R.dimen.dp_2)
            }
            binding.ivProfile.layoutParams = layoutParams

            // 본문/날짜
            binding.tvContent.text = answer.content
            binding.tvTime.text = answer.createdAt


            val aid = answer.id
            val liked = getAnswerLiked(aid)
            val count = getAnswerLikeCount(aid)
            updateLikeUI(count, liked)

            val click = View.OnClickListener {
                val current = getAnswerLiked(aid)
                onAnswerLikeClick(aid, current)
            }
            binding.ivLike.setOnClickListener(click)
            binding.tvLikeCount.setOnClickListener(click)
        }

        private fun updateLikeUI(count: Int, liked: Boolean) {
            val context = binding.root.context

            binding.tvLikeCount.text = count.toString()
            binding.tvLikeCount.visibility = if (count == 0) View.GONE else View.VISIBLE

            binding.ivLike.setImageResource(
                if (liked) R.drawable.ic_question_like_selected else R.drawable.ic_question_like_unselected
            )

            val countColor = when {
                liked -> R.color.point_pink
                count > 0 -> R.color.point_pink
                else -> R.color.gray2
            }
            binding.tvLikeCount.setTextColor(ContextCompat.getColor(context, countColor))

            val iconTint = when {
                liked -> null
                count > 0 -> R.color.point_pink
                else -> R.color.gray2
            }

            if (iconTint != null) {
                binding.ivLike.setColorFilter(ContextCompat.getColor(context, iconTint), android.graphics.PorterDuff.Mode.SRC_IN)
            } else {
                binding.ivLike.clearColorFilter()
            }
        }
    }


    override fun getItemCount(): Int = 1 + answers.size + 1

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_DETAIL
            itemCount - 1 -> TYPE_PADDING
            else -> TYPE_COMMENT
        }
    }

    inner class PaddingViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_DETAIL -> {
                val binding = ItemQuestionDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                DetailViewHolder(binding)
            }
            TYPE_COMMENT -> {
                val binding = ItemQuestionCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                CommentViewHolder(binding)
            }
            TYPE_PADDING -> {
                // 50dp 높이의 빈 View 생성
                val view = View(parent.context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        parent.context.resources.getDimensionPixelSize(R.dimen.dp_50)
                    )
                }
                PaddingViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DetailViewHolder -> holder.bind()
            is CommentViewHolder -> holder.bind(answers[position - 1])
            is PaddingViewHolder -> Unit
        }
    }

    fun notifyHeaderChanged() {
        notifyItemChanged(0)
    }
}