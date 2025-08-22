package com.example.myot.question.adapter

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
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
    private val getAnswerLikeCount: (answerId: Long) -> Int,
    private val getQuestionCommented: (questionId: Long) -> Boolean,
    private val onDeleteClick: (questionId: Long) -> Unit
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

            // 익명 분기
            val isAnonymous = item.isAnonymous
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

            binding.tvDetailContent.text = buildHashtagSpannable(binding.root.context, full)

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

            binding.ivOverflow.setOnClickListener { v ->
                showOverflowPopup(v)
            }
        }

        private fun buildHashtagSpannable(ctx: android.content.Context, text: String): SpannableString {
            val sp = SpannableString(text)
            val color = ContextCompat.getColor(ctx, R.color.point_blue)
            val regex = Regex("#[^\\s]+")

            regex.findAll(text).forEach { m ->
                val start = m.range.first
                val end = m.range.last + 1
                sp.setSpan(
                    ForegroundColorSpan(color),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            return sp
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

        private fun showOverflowPopup(anchor: View) {
            val context = anchor.context
            val inflater = LayoutInflater.from(context)
            val popupView = inflater.inflate(R.layout.menu_popup_question, null)

            val popupWindow = PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            )

            // 팝업뷰 먼저 측정
            popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            val popupWidth = popupView.measuredWidth

            // anchor 위치 가져오기
            val location = IntArray(2)
            anchor.getLocationOnScreen(location)
            val anchorX = location[0]
            val anchorY = location[1]

            // 배경 어둡게
            val rootView = (anchor.rootView as? ViewGroup) ?: return
            val dimView = View(context).apply {
                setBackgroundColor(0x22000000.toInt())
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            rootView.addView(dimView)
            popupWindow.setOnDismissListener { rootView.removeView(dimView) }

            popupWindow.setBackgroundDrawable(null)
            popupWindow.isOutsideTouchable = true
            popupWindow.isFocusable = true
            popupWindow.elevation = 20f

            val offsetX = anchor.width - popupWidth - 20
            val offsetY = anchor.height + 7

            popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, anchorX + offsetX, anchorY + offsetY)

            // 버튼 리스너
            popupView.findViewById<View>(R.id.btn_share).setOnClickListener {
                popupWindow.dismiss()
            }
            popupView.findViewById<View>(R.id.btn_delete).setOnClickListener {
                popupWindow.dismiss()
                onDeleteClick(item.id)
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