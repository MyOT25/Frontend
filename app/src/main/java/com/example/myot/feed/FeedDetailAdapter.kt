package com.example.myot.feed

import android.app.Activity
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.ViewPager2
import com.example.myot.feed.CommentItem
import com.example.myot.feed.ImageViewHolder
import com.example.myot.R
import com.example.myot.feed.TextOnlyViewHolder
import com.example.myot.databinding.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class FeedDetailAdapter(
    private val feedItem: FeedItem,
    private val comments: List<CommentItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_FEED = 0
        private const val TYPE_COMMENT = 1
    }

    override fun getItemCount(): Int = 1 + comments.size

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_FEED else TYPE_COMMENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_FEED -> {
                val binding = when (feedItem.imageUrls.size) {
                    0 -> ItemFeedTextOnlyBinding.inflate(inflater, parent, false)
                    1 -> ItemFeedImage1Binding.inflate(inflater, parent, false)
                    2 -> ItemFeedImage2Binding.inflate(inflater, parent, false)
                    3 -> ItemFeedImage3Binding.inflate(inflater, parent, false)
                    else -> ItemFeedImage4Binding.inflate(inflater, parent, false)
                }
                FeedViewHolder(binding)
            }
            else -> {
                val binding = ItemCommentBinding.inflate(inflater, parent, false)
                CommentViewHolder(binding, feedItem)
            }

        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FeedViewHolder) {
            holder.bind(feedItem, true)
        } else if (holder is CommentViewHolder) {
            val commentPos = position - 1
            val isLast = commentPos == comments.size - 1
            holder.bind(comments[commentPos], isLast)
        }
    }

    class FeedViewHolder(private val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FeedItem, isDetail: Boolean) {
            when (binding) {
                is ItemFeedTextOnlyBinding -> TextOnlyViewHolder(binding).bind(item, isDetail)
                is ItemFeedImage1Binding -> ImageViewHolder(binding).bind(item, isDetail)
                is ItemFeedImage2Binding -> ImageViewHolder(binding).bind(item, isDetail)
                is ItemFeedImage3Binding -> ImageViewHolder(binding).bind(item, isDetail)
                is ItemFeedImage4Binding -> ImageViewHolder(binding).bind(item, isDetail)
            }
        }
    }

    class CommentViewHolder(
        private val binding: ItemCommentBinding,
        private val feedItem: FeedItem
    ) : RecyclerView.ViewHolder(binding.root) {

        private var isExpanded = false

        fun bind(item: CommentItem, isLast: Boolean) {
            binding.tvUsername.text = item.username
            binding.tvDate.text = item.date

            val text = item.content
            val isLongText = text.length > 160

            binding.tvContent.text = if (!isExpanded && isLongText) text.take(160) + "..." else text
            binding.tvMore.visibility = if (isLongText && !isExpanded) View.VISIBLE else View.GONE

            binding.tvMore.setOnClickListener {
                isExpanded = true
                binding.tvContent.text = text
                binding.tvMore.visibility = View.GONE
            }

            binding.tvComment.text = item.commentCount.toString()
            binding.tvLike.text = item.likeCount.toString()
            binding.tvRepost.text = item.repostCount.toString()
            binding.tvBookmark.text = item.bookmarkCount.toString()

            updateColor(binding.tvLike, binding.ivLike, item.isLiked, R.color.point_pink)
            updateColor(binding.tvRepost, binding.ivRepost, item.isReposted, R.color.point_blue)
            updateColor(binding.tvBookmark, binding.ivBookmark, item.isBookmarked, R.color.point_purple)

            binding.tvLike.setOnClickListener { toggleLike(item) }
            binding.ivLike.setOnClickListener { toggleLike(item) }
            binding.tvRepost.setOnClickListener { toggleRepost(item) }
            binding.ivRepost.setOnClickListener { toggleRepost(item) }
            binding.tvBookmark.setOnClickListener { toggleBookmark(item) }
            binding.ivBookmark.setOnClickListener { toggleBookmark(item) }

            val context = binding.root.context as Activity
            setFeedbackLongClick(context, binding.tvLike, "like", feedItem)
            setFeedbackLongClick(context, binding.ivLike, "like", feedItem)
            setFeedbackLongClick(context, binding.tvRepost, "repost", feedItem)
            setFeedbackLongClick(context, binding.ivRepost, "repost", feedItem)
            setFeedbackLongClick(context, binding.tvBookmark, "quote", feedItem)
            setFeedbackLongClick(context, binding.ivBookmark, "quote", feedItem)


            binding.ivOverflow.setOnClickListener { showOverflowPopup(binding.ivOverflow) }
            binding.ivProfile.setOnClickListener { showProfilePopup(binding.ivProfile) }

            // 마지막 댓글이면 직선 숨기기
            binding.viewLine.visibility = if (isLast) View.GONE else View.VISIBLE
        }

        private fun toggleLike(item: CommentItem) {
            item.isLiked = !item.isLiked
            item.likeCount += if (item.isLiked) 1 else -1
            binding.tvLike.text = item.likeCount.toString()
            updateColor(binding.tvLike, binding.ivLike, item.isLiked, R.color.point_pink)
        }

        private fun toggleRepost(item: CommentItem) {
            item.isReposted = !item.isReposted
            item.repostCount += if (item.isReposted) 1 else -1
            binding.tvRepost.text = item.repostCount.toString()
            updateColor(binding.tvRepost, binding.ivRepost, item.isReposted, R.color.point_blue)
        }

        private fun toggleBookmark(item: CommentItem) {
            item.isBookmarked = !item.isBookmarked
            item.bookmarkCount += if (item.isBookmarked) 1 else -1
            binding.tvBookmark.text = item.bookmarkCount.toString()
            updateColor(binding.tvBookmark, binding.ivBookmark, item.isBookmarked, R.color.point_purple)
        }

        private fun updateColor(tv: TextView, iv: ImageView, active: Boolean, colorRes: Int) {
            val color = tv.context.getColor(if (active) colorRes else R.color.gray2)
            tv.setTextColor(color)
            iv.setColorFilter(color)
        }

        private fun setFeedbackLongClick(context: Activity, view: View, type: String, feedItem: FeedItem) {
            view.setOnLongClickListener {
                showFeedbackBottomSheet(context, type, feedItem)
                true
            }
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

            popupWindow.elevation = 20f

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

            popupWindow.elevation = 20f

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

        private fun showFeedbackBottomSheet(context: Activity, defaultType: String, feedItem: FeedItem) {
            val dialog = BottomSheetDialog(context)
            val view = LayoutInflater.from(context).inflate(R.layout.bottomsheet_feed_feedback, null)
            dialog.setContentView(view)

            val tabLayout = view.findViewById<TabLayout>(R.id.tab_feedback)
            val viewPager = view.findViewById<ViewPager2>(R.id.vp_feedback)
            dialog.window?.setDimAmount(0.1f)

            // 더미 데이터
            val likeUsers = List(7) { "user${it + 1}" }
            val repostUsers = List(3) { "user${it + 8}" }
            val quoteFeeds = listOf(
                FeedItem(
                    username = "인용유저1",
                    community = "커뮤니티A",
                    date = "2025/07/09 19:00",
                    content = "이 피드를 인용한 유저1의 글",
                    quotedFeed = feedItem
                ),
                FeedItem(
                    username = "인용유저2",
                    community = "커뮤니티B",
                    date = "2025/07/09 19:10",
                    content = "이 피드를 인용한 유저2의 글입니다.".repeat(10),
                    quotedFeed = feedItem
                )
            )

            val adapter = FeedbackPagerAdapter(
                context as FragmentActivity,
                likeUsers,
                repostUsers,
                quoteFeeds,
                dialog
            )
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

                    behavior.peekHeight = (330 * context.resources.displayMetrics.density).toInt()
                    behavior.isHideable = true
                    behavior.skipCollapsed = false
                    behavior.isDraggable = true
                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED

                    it.layoutParams.height = (context.resources.displayMetrics.heightPixels * 0.64).toInt()
                    it.requestLayout()
                }
            }

            dialog.show()
        }

    }
}
