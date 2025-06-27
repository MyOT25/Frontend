package com.example.myot

import android.app.Activity
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.myot.databinding.ItemCommentBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class CommentAdapter(
    private val comments: List<CommentItem>
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(private val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: CommentItem) {
            binding.tvUsername.text = comment.username
            binding.tvContent.text = comment.content
            binding.tvDate.text = comment.date

            binding.tvComment.text = comment.commentCount.toString()
            binding.tvLike.text = comment.likeCount.toString()
            binding.tvRepost.text = comment.repostCount.toString()
            binding.tvBookmark.text = comment.bookmarkCount.toString()

            // 색상 업데이트
            updateLikeColor(binding.tvLike, binding.ivLike, comment.isLiked)
            updateRepostColor(binding.tvRepost, binding.ivRepost, comment.isReposted)
            updateBookmarkColor(binding.tvBookmark, binding.ivBookmark, comment.isBookmarked)

            // 클릭 이벤트
            binding.tvLike.setOnClickListener { toggleLike(comment) }
            binding.ivLike.setOnClickListener { toggleLike(comment) }

            binding.tvRepost.setOnClickListener { toggleRepost(comment) }
            binding.ivRepost.setOnClickListener { toggleRepost(comment) }

            binding.tvBookmark.setOnClickListener { toggleBookmark(comment) }
            binding.ivBookmark.setOnClickListener { toggleBookmark(comment) }

            // 롱클릭 - 피드백 바텀시트
            val context = binding.root.context as Activity
            binding.tvLike.setOnLongClickListener {
                showFeedbackBottomSheet(context, "like")
                true
            }
            binding.ivLike.setOnLongClickListener {
                showFeedbackBottomSheet(context, "like")
                true
            }

            binding.tvRepost.setOnLongClickListener {
                showFeedbackBottomSheet(context, "repost")
                true
            }
            binding.ivRepost.setOnLongClickListener {
                showFeedbackBottomSheet(context, "repost")
                true
            }

            binding.tvBookmark.setOnLongClickListener {
                showFeedbackBottomSheet(context, "quote")
                true
            }
            binding.ivBookmark.setOnLongClickListener {
                showFeedbackBottomSheet(context, "quote")
                true
            }

            // 팝업 메뉴
            binding.ivProfile.setOnClickListener {
                showProfilePopup(it)
            }
            binding.ivOverflow.setOnClickListener {
                showOverflowPopup(it)
            }
        }

        private fun toggleLike(comment: CommentItem) {
            comment.isLiked = !comment.isLiked
            comment.likeCount += if (comment.isLiked) 1 else -1
            notifyItemChanged(adapterPosition)
        }

        private fun toggleRepost(comment: CommentItem) {
            comment.isReposted = !comment.isReposted
            comment.repostCount += if (comment.isReposted) 1 else -1
            notifyItemChanged(adapterPosition)
        }

        private fun toggleBookmark(comment: CommentItem) {
            comment.isBookmarked = !comment.isBookmarked
            comment.bookmarkCount += if (comment.isBookmarked) 1 else -1
            notifyItemChanged(adapterPosition)
        }

        private fun updateLikeColor(tv: TextView, iv: ImageView, liked: Boolean) {
            val color = tv.context.getColor(if (liked) R.color.point_pink else R.color.gray2)
            tv.setTextColor(color)
            iv.setColorFilter(color)
        }

        private fun updateRepostColor(tv: TextView, iv: ImageView, reposted: Boolean) {
            val color = tv.context.getColor(if (reposted) R.color.point_blue else R.color.gray2)
            tv.setTextColor(color)
            iv.setColorFilter(color)
        }

        private fun updateBookmarkColor(tv: TextView, iv: ImageView, bookmarked: Boolean) {
            val color = tv.context.getColor(if (bookmarked) R.color.point_purple else R.color.gray2)
            tv.setTextColor(color)
            iv.setColorFilter(color)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    override fun getItemCount(): Int = comments.size

    private fun showFeedbackBottomSheet(context: Activity, defaultType: String) {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.bottomsheet_feed_feedback, null)
        dialog.setContentView(view)

        val tabLayout = view.findViewById<TabLayout>(R.id.tab_feedback)
        val viewPager = view.findViewById<ViewPager2>(R.id.vp_feedback)

        dialog.window?.setDimAmount(0f)

        // 임시 피드백 데이터
        val feedbackMap = mapOf(
            "like" to List(7) { "user${it + 1}" },
            "repost" to List(3) { "user${it + 8}" },
            "quote" to List(2) { "user${it + 11}" }
        )

        val adapter = FeedbackPagerAdapter(context as FragmentActivity, feedbackMap)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "좋아요"
                1 -> "재게시"
                2 -> "인용"
                else -> ""
            }
        }.attach()

        viewPager.setCurrentItem(
            when (defaultType) {
                "like" -> 0
                "repost" -> 1
                "quote" -> 2
                else -> 0
            }, false
        )

        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it as FrameLayout)
                val peekHeight = (330 * context.resources.displayMetrics.density).toInt()
                behavior.peekHeight = peekHeight
                behavior.isHideable = false
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED

                val screenHeight = context.resources.displayMetrics.heightPixels
                val maxHeight = (screenHeight * 0.64).toInt()
                it.layoutParams.height = maxHeight
                it.requestLayout()
            }
        }
        dialog.show()
    }

    private fun showProfilePopup(anchor: View) {
        val context = anchor.context
        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(R.layout.menu_popup_profile, null)

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupWidth = popupView.measuredWidth

        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        val anchorX = location[0]
        val anchorY = location[1]

        val rootView = (anchor.rootView as? ViewGroup) ?: return
        val dimView = View(context).apply {
            setBackgroundColor(0x22000000.toInt())
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        rootView.addView(dimView)
        popupWindow.setOnDismissListener { rootView.removeView(dimView) }

        popupWindow.setBackgroundDrawable(null)
        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true

        val offsetX = anchor.width - popupWidth + 430
        val offsetY = anchor.height - 10
        popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, anchorX + offsetX, anchorY + offsetY)

        popupView.findViewById<View>(R.id.btn_community).setOnClickListener {
            popupWindow.dismiss()
            Toast.makeText(context, "‘ABC’ 커뮤니티 이동", Toast.LENGTH_SHORT).show()
        }
        popupView.findViewById<View>(R.id.btn_user_profile).setOnClickListener {
            popupWindow.dismiss()
            Toast.makeText(context, "@abcde 프로필 이동", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showOverflowPopup(anchor: View) {
        val context = anchor.context
        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(R.layout.menu_popup_overflow, null)

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupWidth = popupView.measuredWidth

        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        val anchorX = location[0]
        val anchorY = location[1]

        val rootView = (anchor.rootView as? ViewGroup) ?: return
        val dimView = View(context).apply {
            setBackgroundColor(0x22000000.toInt())
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        rootView.addView(dimView)
        popupWindow.setOnDismissListener { rootView.removeView(dimView) }

        popupWindow.setBackgroundDrawable(null)
        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true

        val offsetX = anchor.width - popupWidth - 50
        val offsetY = anchor.height + 7
        popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, anchorX + offsetX, anchorY + offsetY)

        popupView.findViewById<View>(R.id.btn_share).setOnClickListener {
            popupWindow.dismiss()
            Toast.makeText(context, "공유 클릭", Toast.LENGTH_SHORT).show()
        }
        popupView.findViewById<View>(R.id.btn_report).setOnClickListener {
            popupWindow.dismiss()
            Toast.makeText(context, "신고 클릭", Toast.LENGTH_SHORT).show()
        }
        popupView.findViewById<View>(R.id.btn_profile).setOnClickListener {
            popupWindow.dismiss()
            Toast.makeText(context, "프로필 보기 클릭", Toast.LENGTH_SHORT).show()
        }
        popupView.findViewById<View>(R.id.btn_delete).setOnClickListener {
            popupWindow.dismiss()
            Toast.makeText(context, "삭제 클릭", Toast.LENGTH_SHORT).show()
        }
    }

}
