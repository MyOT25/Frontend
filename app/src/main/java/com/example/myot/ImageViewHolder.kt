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
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.myot.databinding.ItemFeedImage1Binding
import com.example.myot.databinding.ItemFeedImage2Binding
import com.example.myot.databinding.ItemFeedImage3Binding
import com.example.myot.databinding.ItemFeedImage4Binding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.floor

class ImageViewHolder(
    private val binding: ViewBinding,
    private val isDetail: Boolean = false
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: FeedItem, isDetail: Boolean = false, isLastItem: Boolean = false) {
        val context = binding.root.context

        when (binding) {
            is ItemFeedImage1Binding -> bindCommon(binding, item, isDetail, isLastItem)
            is ItemFeedImage2Binding -> bindCommon(binding, item, isDetail, isLastItem)
            is ItemFeedImage3Binding -> bindCommon(binding, item, isDetail, isLastItem)
            is ItemFeedImage4Binding -> bindCommon(binding, item, isDetail, isLastItem)
        }
    }

    private fun <T : ViewBinding> bindCommon(b: T, item: FeedItem, isDetail: Boolean, isLastItem: Boolean) {
        val context = b.root.context
        val tvUsername = b.root.findViewById<View>(R.id.tv_username) as? TextView
        val tvDate = b.root.findViewById<View>(R.id.tv_date) as? TextView
        val tvTime = b.root.findViewById<View>(R.id.tv_time) as? TextView
        val tvContent = b.root.findViewById<View>(R.id.tv_content) as? TextView
        val tvMore = b.root.findViewById<View>(R.id.tv_more) as? TextView
        val tvComment = b.root.findViewById<View>(R.id.tv_comment) as? TextView
        val tvLike = b.root.findViewById<View>(R.id.tv_like) as? TextView
        val tvRepost = b.root.findViewById<View>(R.id.tv_repost) as? TextView
        val tvBookmark = b.root.findViewById<View>(R.id.tv_bookmark) as? TextView
        val ivLike = b.root.findViewById<View>(R.id.iv_like) as? ImageView
        val ivRepost = b.root.findViewById<View>(R.id.iv_repost) as? ImageView
        val ivBookmark = b.root.findViewById<View>(R.id.iv_bookmark) as? ImageView
        val ivOverflow = b.root.findViewById<View>(R.id.iv_overflow)
        val ivCommunity = b.root.findViewById<View>(R.id.iv_community)
        val ivProfile = b.root.findViewById<View>(R.id.iv_profile)


        // 마지막 피드인 경우 구분선 가리기
        val divider = b.root.findViewById<View>(R.id.iv_div_line)
        divider?.visibility = if (isLastItem) View.GONE else View.VISIBLE

        val text = item.content
        val isLongText = text.length > 160
        var isExpanded = false

        tvContent?.text = when {
            isDetail -> text
            isLongText -> text.take(160) + "..."
            else -> text
        }
        tvMore?.visibility = if (!isDetail && isLongText) View.VISIBLE else View.GONE

        tvMore?.setOnClickListener {
            if (!isExpanded) {
                tvContent?.text = text
                tvMore?.visibility = View.GONE
                isExpanded = true
            }
        }

        tvDate?.text = item.date
        tvUsername?.text = item.username
        tvTime?.text = getTimeAgo(item.date)
        tvComment?.text = item.commentCount.toString()
        tvLike?.text = item.likeCount.toString()
        tvRepost?.text = item.repostCount.toString()
        tvBookmark?.text = item.bookmarkCount.toString()

        // 피드백 좋아요, 재게시, 북마크 클릭 이벤트
        updateLikeColor(tvLike, ivLike, item.isLiked)
        updateRepostColor(tvRepost, ivRepost, item.isReposted)
        updateBookmarkColor(tvBookmark, ivBookmark, item.isBookmarked)

        val likeToggle = {
            item.isLiked = !item.isLiked
            item.likeCount += if (item.isLiked) 1 else -1
            tvLike?.text = item.likeCount.toString()
            updateLikeColor(tvLike, ivLike, item.isLiked)
        }
        tvLike?.setOnClickListener { likeToggle() }
        ivLike?.setOnClickListener { likeToggle() }

        val repostToggle = {
            item.isReposted = !item.isReposted
            item.repostCount += if (item.isReposted) 1 else -1
            tvRepost?.text = item.repostCount.toString()
            updateRepostColor(tvRepost, ivRepost, item.isReposted)
        }
        tvRepost?.setOnClickListener { repostToggle() }
        ivRepost?.setOnClickListener { repostToggle() }

        val bookmarkToggle = {
            item.isBookmarked = !item.isBookmarked
            item.bookmarkCount += if (item.isBookmarked) 1 else -1
            tvBookmark?.text = item.bookmarkCount.toString()
            updateBookmarkColor(tvBookmark, ivBookmark, item.isBookmarked)
        }
        tvBookmark?.setOnClickListener { bookmarkToggle() }
        ivBookmark?.setOnClickListener { bookmarkToggle() }

        ivOverflow?.setOnClickListener { showOverflowPopup(it) }
        ivCommunity?.setOnClickListener { showProfilePopup(it) }
        ivProfile?.setOnClickListener { showProfilePopup(it) }

        // 피드백 홀드시 피드백 바텀시프트 이동
        ivLike?.setOnLongClickListener {
            showFeedbackBottomSheet(b.root.context as Activity, "like", item)
            true
        }
        tvLike?.setOnLongClickListener {
            showFeedbackBottomSheet(b.root.context as Activity, "like", item)
            true
        }
        ivRepost?.setOnLongClickListener {
            showFeedbackBottomSheet(b.root.context as Activity, "repost", item)
            true
        }
        tvRepost?.setOnLongClickListener {
            showFeedbackBottomSheet(b.root.context as Activity, "repost", item)
            true
        }
        ivBookmark?.setOnLongClickListener {
            showFeedbackBottomSheet(b.root.context as Activity, "quote", item)
            true
        }
        tvBookmark?.setOnLongClickListener {
            showFeedbackBottomSheet(b.root.context as Activity, "quote", item)
            true
        }


        // --- 인용 피드 처리 ---
        val quoteLayout = b.root.findViewById<ViewGroup>(R.id.layout_quote)

        if (item.quotedFeed != null) {
            val quoted = item.quotedFeed!!
            quoteLayout.visibility = View.VISIBLE
            quoteLayout.removeAllViews()

            val inflater = LayoutInflater.from(b.root.context)
            val layoutResId = when (quoted.imageUrls.size) {
                0 -> R.layout.item_feed_quote_text_only
                1 -> R.layout.item_feed_quote_image1
                2 -> R.layout.item_feed_quote_image2
                3 -> R.layout.item_feed_quote_image3
                4 -> R.layout.item_feed_quote_image4
                else -> R.layout.item_feed_quote_text_only
            }

            val quoteView = inflater.inflate(layoutResId, quoteLayout, false)
            quoteLayout.addView(quoteView)

            val tvQuoteContent = quoteView.findViewById<TextView>(R.id.tv_content)
            val tvQuoteMore = quoteView.findViewById<TextView>(R.id.tv_more)

            val text = quoted.content
            val isLongText = text.length > 160

            if (isDetail) {
                tvQuoteContent?.text = text
                tvQuoteMore?.visibility = View.GONE
            } else if (isLongText) {
                tvQuoteContent?.text = text.take(160) + "..."
                tvQuoteMore?.visibility = View.VISIBLE
                tvQuoteMore?.setOnClickListener {
                    tvQuoteContent?.text = text
                    tvQuoteMore?.visibility = View.GONE
                }
            } else {
                tvQuoteContent?.text = text
                tvQuoteMore?.visibility = View.GONE
            }

            quoted.imageUrls.forEachIndexed { idx, url ->
                val imageId = when (idx) {
                    0 -> R.id.iv_image1
                    1 -> R.id.iv_image2
                    2 -> R.id.iv_image3
                    3 -> R.id.iv_image4
                    else -> null
                }
                imageId?.let {
                    quoteView.findViewById<ImageView>(it)?.let { imageView ->
                        imageView.visibility = View.VISIBLE
                        Glide.with(imageView).load(url).into(imageView)
                    }
                }
            }
        } else {
            quoteLayout.visibility = View.GONE
        }


        binding.root.setOnClickListener {
            val context = binding.root.context
            if (context is FragmentActivity) {
                val fragment = FeedDetailFragment.newInstance(item)
                context.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container_view, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun updateLikeColor(textView: TextView?, imageView: ImageView?, liked: Boolean) {
        val context = textView?.context ?: return
        val color = context.getColor(if (liked) R.color.point_pink else R.color.gray2)
        textView.setTextColor(color)
        imageView?.setColorFilter(color)
    }

    private fun updateRepostColor(textView: TextView?, imageView: ImageView?, reposted: Boolean) {
        val context = textView?.context ?: return
        val color = context.getColor(if (reposted) R.color.point_blue else R.color.gray2)
        textView.setTextColor(color)
        imageView?.setColorFilter(color)
    }

    private fun updateBookmarkColor(textView: TextView?, imageView: ImageView?, bookmarked: Boolean) {
        val context = textView?.context ?: return
        val color = context.getColor(if (bookmarked) R.color.point_purple else R.color.gray2)
        textView.setTextColor(color)
        imageView?.setColorFilter(color)
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
        val offsetX = anchor.width - popupWidth - 50
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
            Toast.makeText(context, "@abcde 프로필 이동", Toast.LENGTH_SHORT).show()
        }
    }

    fun getTimeAgo(dateStr: String): String {
        val format = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        val postTime = format.parse(dateStr) ?: return ""

        val now = Date()
        val diffMillis = now.time - postTime.time

        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
        val days = TimeUnit.MILLISECONDS.toDays(diffMillis)
        val months = floor(days / 30.0).toInt()
        val years = floor(days / 365.0).toInt()

        return when {
            minutes < 1 -> "방금 전"
            minutes < 60 -> "${minutes}분 전"
            hours < 24 -> "${hours}시간 전"
            days <= 30 -> "${days}일 전"
            days < 365 -> "${months}개월 전"
            else -> "${years}년 전"
        }
    }

    private fun showFeedbackBottomSheet(context: Activity, defaultType: String, feedItem: FeedItem) {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.bottomsheet_feed_feedback, null)
        dialog.setContentView(view)

        val tabLayout = view.findViewById<TabLayout>(R.id.tab_feedback)
        val viewPager = view.findViewById<ViewPager2>(R.id.vp_feedback)

        dialog.window?.setDimAmount(0.1f)

        // 피드백 데이터
        val likeUsers = List(10) { "user${it + 1}" }
        val repostUsers = List(5) { "user${it + 11}" }

        val quoteFeeds = listOf(
            FeedItem(
                username = "인용러A",
                community = feedItem.community,
                date = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date()),
                content = "이 피드를 인용해서 작성한 피드입니다!",
                quotedFeed = feedItem
            ),
            FeedItem(
                username = "인용러B",
                community = feedItem.community,
                date = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date()),
                content = "또 다른 사용자가 이 피드를 인용했어요.",
                quotedFeed = feedItem
            )
        )

        val adapter = FeedbackPagerAdapter(
            context as FragmentActivity,
            likeUsers,
            repostUsers,
            quoteFeeds
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

                // peekHeight 330dp
                val peekHeight = (330 * context.resources.displayMetrics.density).toInt()
                behavior.peekHeight = peekHeight

                // 닫을 수 있게 설정
                behavior.isHideable = true
                behavior.skipCollapsed = false
                behavior.isDraggable = true
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED

                // 최대 높이 제한
                val screenHeight = context.resources.displayMetrics.heightPixels
                val maxHeight = (screenHeight * 0.64).toInt()
                it.layoutParams.height = maxHeight
                it.requestLayout()
            }
        }
        dialog.show()
    }
}
