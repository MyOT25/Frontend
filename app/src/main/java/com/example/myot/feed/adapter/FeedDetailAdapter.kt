package com.example.myot.feed.adapter

import com.bumptech.glide.Glide

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
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
import com.example.myot.comment.ui.CommentDetailFragment
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

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.myot.retrofit2.TokenStore
import com.example.myot.retrofit2.RetrofitClient
class FeedDetailAdapter(
    private val feedItem: FeedItem,
    private val comments: List<CommentItem>,
    private val onDeleteRequest: (Long) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private val underlineHiddenPositions = mutableSetOf<Int>()

    init {
        for (i in 1 until comments.size) {
            if (extractMentionedUserid(comments[i].content) != null) {
                underlineHiddenPositions.add(i - 1)
            }
        }
    }

    private fun extractMentionedUserid(text: String): String? {
        val regex = Regex("@\\w+")
        return regex.find(text)?.value
    }

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
                FeedViewHolder(binding, onDeleteRequest)
            }
            else -> {
                val binding = ItemCommentBinding.inflate(inflater, parent, false)
                CommentViewHolder(binding, feedItem)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CommentViewHolder) {
            val commentPos = position - 1
            val isLast = commentPos == comments.size - 1
            val hideUnderline = underlineHiddenPositions.contains(commentPos)
            holder.bind(comments[commentPos], isLast, hideUnderline)
        } else if (holder is FeedViewHolder) {
            holder.bind(feedItem, isLastItem = false)
        }
    }


    class FeedViewHolder(
        private val binding: ViewBinding,
        private val onDeleteRequest: (Long) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FeedItem, isLastItem: Boolean) {
            if (binding is ItemFeedDetailBinding) {
                com.example.myot.feed.adapter.FeedViewHolder(
                    binding,
                    onItemClick = {},
                    onDeleteRequest = onDeleteRequest
                ).bind(item, isLastItem)
            }
        }
    }

    class CommentViewHolder(
        private val binding: ItemCommentBinding,
        private val feedItem: FeedItem
    ) : RecyclerView.ViewHolder(binding.root) {

        private var isExpanded = false
        private var likeInFlight = false

        fun bind(item: CommentItem, isLast: Boolean, hideUnderline: Boolean)  {
            // 닉네임 / 시간
            binding.tvUsername.text = item.username
            binding.tvDate.text = item.date

            // 로그인 아이디(@ 접두어 처리, 비어있으면 숨김)
            val loginId = item.userid
            if (loginId.isNotBlank()) {
                binding.tvUserid.visibility = View.VISIBLE
                binding.tvUserid.text = if (loginId.startsWith("@")) loginId else "@$loginId"
            } else {
                binding.tvUserid.text = ""
                binding.tvUserid.visibility = View.INVISIBLE
            }

            // 프로필 이미지
            val url = item.profileImageUrl
            if (url.isNullOrBlank()) {
                binding.ivProfile.setImageResource(R.drawable.ic_no_profile)
            } else {
                Glide.with(binding.ivProfile)
                    .load(url)
                    .placeholder(R.drawable.ic_no_profile)
                    .error(R.drawable.ic_no_profile)
                    .circleCrop()
                    .into(binding.ivProfile)
            }

            val text = item.content
            val isLongText = text.length > 160

            val displayText = if (!isExpanded && isLongText) text.take(160) + "..." else text
            binding.tvContent.text = styleMentionText(displayText, binding.root.context)
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
            binding.ivProfile.setOnClickListener { showProfilePopup(binding.ivProfile) }

            binding.viewUnderline.visibility = if (hideUnderline) View.GONE else View.VISIBLE

            binding.root.setOnClickListener {
                val context = binding.root.context
                if (context is FragmentActivity) {
                    val fragment = CommentDetailFragment.newInstance(item, feedItem)
                    context.supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container_view, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
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

        private fun toggleLike(item: CommentItem) {
            if (likeInFlight) return
            likeInFlight = true
            binding.tvLike.isEnabled = false
            binding.ivLike.isEnabled = false

            val prevLiked = item.isLiked
            val prevCount = item.likeCount

            // Optimistic update
            item.isLiked = !prevLiked
            item.likeCount = (prevCount + if (item.isLiked) 1 else -1).coerceAtLeast(0)
            binding.tvLike.text = item.likeCount.toString()
            updateColor(binding.tvLike, binding.ivLike, item.isLiked, R.color.point_pink)

            // TODO: 댓글 좋아요 API가 정해지면 여기서 네트워크 호출 후 성공/실패에 따라 유지/롤백 처리
            // 현재는 UI 일관성/중복 클릭 방지 목적의 가드만 제공

            likeInFlight = false
            binding.tvLike.isEnabled = true
            binding.ivLike.isEnabled = true
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

            val activity = context as? FragmentActivity ?: return

            val pagerAdapter = FeedbackPagerAdapter(
                activity,
                dialog
            ) { dialog.dismiss() }
            viewPager.adapter = pagerAdapter

            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> "좋아요"
                    1 -> "재게시"
                    2 -> "인용"
                    else -> ""
                }
            }.attach()

            val startIndex = when (defaultType) {
                "like" -> 0
                "repost" -> 1
                "quote", "bookmark" -> 2
                else -> 0
            }
            viewPager.setCurrentItem(startIndex, false)
            viewPager.offscreenPageLimit = 3

            // API 호출
            activity.lifecycleScope.launch {
                try {
                    val raw = TokenStore.loadAccessToken(activity)
                    val bearer = raw?.trim()
                        ?.removePrefix("Bearer ")
                        ?.trim()
                        ?.removeSurrounding("\"")
                        ?.let { "Bearer $it" } ?: ""

                    if (feedItem.id <= 0L) return@launch

                    // 1) 좋아요 사용자
                    val likesRes = withContext(Dispatchers.IO) {
                        RetrofitClient.feedService.getPostLikes(
                            token = bearer,
                            postId = feedItem.id,
                            page = 1,
                            limit = 20
                        )
                    }
                    if (likesRes.isSuccessful) {
                        val users = likesRes.body()?.success?.users.orEmpty()

                        val likeUsersUi = users.map {
                            com.example.myot.feed.model.FeedbackUserUi(
                                nickname = it.nickname ?: "",
                                loginId = null, // 좋아요 응답엔 loginId 없음
                                profileImage = it.profileImage
                            )
                        }
                        pagerAdapter.submitLikeUsers(likeUsersUi)
                    }

                    // 2) 재게시 사용자
                    val repostRes = withContext(Dispatchers.IO) {
                        RetrofitClient.feedService.getRepostedUsers(
                            token = bearer,
                            postId = feedItem.id,
                            page = 1,
                            limit = 20
                        )
                    }
                    if (repostRes.isSuccessful) {
                        val entries = repostRes.body()?.success ?: emptyList()
                        val repostUsersUi = entries.mapNotNull { entry ->
                            val u = entry.user ?: return@mapNotNull null
                            com.example.myot.feed.model.FeedbackUserUi(
                                nickname = u.nickname ?: u.loginId ?: "",
                                loginId = u.loginId,
                                profileImage = u.profileImage
                            )
                        }
                        pagerAdapter.submitRepostUsers(repostUsersUi)
                    }
                } catch (_: Exception) {
                    // 조용히 무시(원하면 토스트)
                }
            }

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
