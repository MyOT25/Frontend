package com.example.myot.feed.adapter

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
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
import com.example.myot.R
import com.example.myot.databinding.ItemCommentDetailBinding
import com.example.myot.databinding.ItemCommentReplyBinding
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

class CommentDetailAdapter(
    private val parentComment: CommentItem,
    private val parentFeed: FeedItem,
    private val replies: List<CommentItem> // 대댓글 리스트
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_REPLY = 1
    }

    override fun getItemCount(): Int = 1 + replies.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val binding = ItemCommentDetailBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            HeaderViewHolder(binding)
        } else {
            val binding = ItemCommentReplyBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            ReplyViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.bind(parentFeed, parentComment)
        } else if (holder is ReplyViewHolder) {
            holder.bind(replies[position - 1]) // -1 보정
        }
    }


    inner class HeaderViewHolder(private val binding: ItemCommentDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(feed: FeedItem, comment: CommentItem) {

            binding.tvFeedUsername.text = feed.username
            binding.tvFeedContent.text = if (feed.content.length > 31) {
                feed.content.substring(0, 31) + "..."
            } else {
                feed.content
            }

            binding.tvUsername.text = comment.username
            binding.tvDate.text = comment.date
            binding.tvUserid.text = comment.userid
            binding.tvContent.text = styleMentionText(comment.content, binding.root.context)

            binding.tvComment.text = comment.commentCount.toString()
            binding.tvLike.text = comment.likeCount.toString()
            binding.tvRepost.text = comment.repostCount.toString()
            binding.tvQuote.text = comment.quoteCount.toString()

            // 추후 답글이 존재하면 안 보이게 해야함
            binding.tvNoReply.visibility = View.VISIBLE

            updateColor(binding.tvLike, binding.ivLike, comment.isLiked, R.color.point_pink)
            updateColor(binding.tvRepost, binding.ivRepost, comment.isReposted, R.color.point_blue)
            updateColor(binding.tvQuote, binding.ivQuote, comment.isQuoted, R.color.point_purple)

            binding.tvLike.setOnClickListener { toggleLike(comment) }
            binding.ivLike.setOnClickListener { toggleLike(comment) }
            binding.tvRepost.setOnClickListener { toggleRepost(comment) }
            binding.ivRepost.setOnClickListener { toggleRepost(comment) }
            binding.tvQuote.setOnClickListener { toggleQuote(comment) }
            binding.ivQuote.setOnClickListener { toggleQuote(comment) }

            val context = binding.root.context as Activity
            val quotedFeed = FeedItem(
                username = comment.username,
                community = feed.community,
                date = comment.date,
                content = comment.content
            )

            setFeedbackLongClick(context, binding.tvLike, "like", quotedFeed)
            setFeedbackLongClick(context, binding.ivLike, "like", quotedFeed)
            setFeedbackLongClick(context, binding.tvRepost, "repost", quotedFeed)
            setFeedbackLongClick(context, binding.ivRepost, "repost", quotedFeed)
            setFeedbackLongClick(context, binding.tvQuote, "quote", quotedFeed)
            setFeedbackLongClick(context, binding.ivQuote, "quote", quotedFeed)

            binding.ivProfile.setOnClickListener { showProfilePopup(binding.ivProfile) }
            binding.ivOverflow.setOnClickListener { showOverflowPopup(binding.ivOverflow) }

            binding.ivBack.setOnClickListener {
                val activity = it.context as? FragmentActivity
                activity?.supportFragmentManager?.popBackStack()
            }
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
            val color = tv.context.getColor(if (active) colorRes else R.color.gray3)
            tv.setTextColor(color)
            iv.setColorFilter(color)
        }

        private fun setFeedbackLongClick(context: Activity, view: View, type: String, feedItem: FeedItem) {
            view.setOnLongClickListener {
                showFeedbackBottomSheet(context, type, feedItem)
                true
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
                setBackgroundColor(0x22000000.toInt()) // 연한 어두운 배경
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

            // X 좌표를 anchor의 오른쪽 기준에서 왼쪽으로 이동시킴
            val offsetX = anchor.width - popupWidth - 20
            val offsetY = anchor.height + 7

            popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, anchorX + offsetX, anchorY + offsetY)

            // 버튼 리스너
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

        private fun styleMentionText(text: String, context: Context): SpannableString {
            val spannable = SpannableString(text)
            val regex = Regex("@\\w+")

            regex.findAll(text).forEach { match ->
                val start = match.range.first
                val end = match.range.last + 1

                spannable.setSpan(
                    ForegroundColorSpan(context.getColor(R.color.point_purple)),
                    start, end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    start, end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            return spannable
        }
    }

    inner class ReplyViewHolder(private val binding: ItemCommentReplyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reply: CommentItem) {
            binding.tvUsername.text = reply.username
            binding.tvUserid.text = "@${reply.userid}"
            binding.tvDate.text = reply.date
            binding.tvContent.text = reply.content
        }
    }

}