package com.example.myot.feed.adapter

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
import com.example.myot.R
import com.example.myot.databinding.*
import com.example.myot.feed.model.CommentItem
import com.example.myot.feed.model.FeedItem
import com.example.myot.profile.ProfileFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                val binding = ItemFeedDetailBinding.inflate(inflater, parent, false)
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
            holder.bind(feedItem, isLastItem = false)
        } else if (holder is CommentViewHolder) {
            val commentPos = position - 1
            val isLast = commentPos == comments.size - 1
            holder.bind(comments[commentPos], isLast)
        }
    }

    class FeedViewHolder(private val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FeedItem, isLastItem: Boolean) {
            if (binding is ItemFeedDetailBinding) {
                com.example.myot.feed.adapter.FeedViewHolder(binding).bind(item, isLastItem)
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
            binding.tvQuote.text = item.quoteCount.toString()

            updateColor(binding.tvLike, binding.ivLike, item.isLiked, R.color.point_pink)
            updateColor(binding.tvRepost, binding.ivRepost, item.isReposted, R.color.point_blue)
            updateColor(binding.tvQuote, binding.ivQuote, item.isQuoted, R.color.point_purple)

            binding.tvLike.setOnClickListener { toggleLike(item) }
            binding.ivLike.setOnClickListener { toggleLike(item) }
            binding.tvRepost.setOnClickListener { toggleRepost(item) }
            binding.ivRepost.setOnClickListener { toggleRepost(item) }
            binding.tvQuote.setOnClickListener { toggleQuote(item) }
            binding.ivQuote.setOnClickListener { toggleQuote(item) }

            val context = binding.root.context as Activity
            val quotedCommentFeed = FeedItem(
                username = item.username,
                community = feedItem.community,
                date = item.date,
                content = item.content
            )

            setFeedbackLongClick(context, binding.tvLike, "like", quotedCommentFeed)
            setFeedbackLongClick(context, binding.ivLike, "like", quotedCommentFeed)
            setFeedbackLongClick(context, binding.tvRepost, "repost", quotedCommentFeed)
            setFeedbackLongClick(context, binding.ivRepost, "repost", quotedCommentFeed)
            setFeedbackLongClick(context, binding.tvQuote, "quote", quotedCommentFeed)
            setFeedbackLongClick(context, binding.ivQuote, "quote", quotedCommentFeed)

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

        private fun toggleQuote(item: CommentItem) {
            item.isQuoted = !item.isQuoted
            item.quoteCount += if (item.isQuoted) 1 else -1
            binding.tvQuote.text = item.quoteCount.toString()
            updateColor(binding.tvQuote, binding.ivQuote, item.isQuoted, R.color.point_purple)
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

                val activity = anchor.context as? FragmentActivity ?: return@setOnClickListener
                val transaction = activity.supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragment_container_view, ProfileFragment())
                transaction.addToBackStack(null)
                transaction.commit()
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

            // 댓글 피드백 더미 데이터
            val likeUsers = List(8) { "user${it + 1}" }
            val repostUsers = List(3) { "user${it + 11}" }


            val quoteFeeds = listOf(
                FeedItem(
                    username = "댓글인용러1",
                    community = feedItem.community,
                    date = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date()),
                    content = "이 댓글을 인용한 피드입니다.",
                    quotedFeed = feedItem
                ),
                FeedItem(
                    username = "댓글인용러2",
                    community = feedItem.community,
                    date = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date()),
                    content = "이 댓글을 인용한 또다른 피드입니다.".repeat(5),
                    quotedFeed = feedItem
                )

            )

            val adapter = FeedbackPagerAdapter(
                context as FragmentActivity,
                likeUsers,
                repostUsers,
                quoteFeeds,
                dialog
            ) {
                dialog.dismiss()
            }
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

                    val screenHeight = context.resources.displayMetrics.heightPixels
                    val maxHeight = (screenHeight * 0.97).toInt()
                    it.layoutParams.height = maxHeight
                    it.requestLayout()

                    behavior.peekHeight = (500 * context.resources.displayMetrics.density).toInt()
                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    behavior.skipCollapsed = false
                    behavior.isDraggable = true
                    behavior.isHideable = true
                }
            }
            dialog.show()
        }


    }
}
