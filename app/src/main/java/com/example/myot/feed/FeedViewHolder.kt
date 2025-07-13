package com.example.myot.feed

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
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.myot.R
import com.example.myot.databinding.ItemFeedBinding
import com.example.myot.databinding.ItemFeedDetailBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.floor

class FeedViewHolder(
    private val binding: ViewBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: FeedItem, isLastItem: Boolean = false) {
        when (binding) {
            is ItemFeedBinding -> bindViews(binding, item, isLastItem)
            is ItemFeedDetailBinding -> bindViews(binding, item, isLastItem)
        }
    }

    private fun <B : ViewBinding> bindViews(
        b: B,
        item: FeedItem,
        isLastItem: Boolean
    ) {
        val root = b.root

        // 공통 뷰 바인딩
        val tvUsername = root.findViewById<TextView>(R.id.tv_username)
        val tvDate = root.findViewById<TextView>(R.id.tv_date)
        val tvComment = root.findViewById<TextView>(R.id.tv_comment)
        val tvLike = root.findViewById<TextView>(R.id.tv_like)
        val tvRepost = root.findViewById<TextView>(R.id.tv_repost)
        val tvQuote = root.findViewById<TextView>(R.id.tv_quote)
        val tvContent = root.findViewById<TextView>(R.id.tv_content)
        val ivLike = root.findViewById<ImageView>(R.id.iv_like)
        val ivRepost = root.findViewById<ImageView>(R.id.iv_repost)
        val ivQuote = root.findViewById<ImageView>(R.id.iv_quote)
        val ivOverflow = root.findViewById<ImageView>(R.id.iv_overflow)
        val ivProfile = root.findViewById<ImageView>(R.id.iv_profile)
        val ivDivLine = root.findViewById<View>(R.id.iv_div_line)

        // 데이터 설정
        tvUsername?.text = item.username
        tvDate?.text = item.date
        tvComment?.text = item.commentCount.toString()
        tvLike?.text = item.likeCount.toString()
        tvRepost?.text = item.repostCount.toString()
        tvQuote?.text = item.quoteCount.toString()
        tvContent?.text = item.content

        // 이미지 처리
        listOf(
            R.id.layout_image1,
            R.id.layout_image2,
            R.id.layout_image3,
            R.id.layout_image4
        ).forEach { id -> root.findViewById<ViewGroup>(id)?.visibility = View.GONE }

        when (item.imageUrls.size) {
            1 -> root.findViewById<ViewGroup>(R.id.layout_image1)?.let {
                it.visibility = View.VISIBLE
                loadImages(it, item.imageUrls)
            }
            2 -> root.findViewById<ViewGroup>(R.id.layout_image2)?.let {
                it.visibility = View.VISIBLE
                loadImages(it, item.imageUrls)
            }
            3 -> root.findViewById<ViewGroup>(R.id.layout_image3)?.let {
                it.visibility = View.VISIBLE
                loadImages(it, item.imageUrls)
            }
            4 -> root.findViewById<ViewGroup>(R.id.layout_image4)?.let {
                it.visibility = View.VISIBLE
                loadImages(it, item.imageUrls)
            }
        }

        // 추가 요소는 ItemFeedBinding인 경우만 처리
        if (b is ItemFeedBinding) {
            val tvTime = root.findViewById<TextView>(R.id.tv_time)
            val tvMore = root.findViewById<TextView>(R.id.tv_more)
            val ivCommunity = root.findViewById<ImageView>(R.id.iv_community)

            val isLongText = item.content.length > 160
            var isExpanded = false

            // 최하단 피드일 경우 윤곽선 제거
            ivDivLine?.visibility = if (isLastItem) View.GONE else View.VISIBLE

            // 더 보기 제약
            tvContent?.text = if (isLongText) item.content.take(160) + "..." else item.content
            tvMore?.visibility = if (isLongText) View.VISIBLE else View.GONE

            // 더 보기 클릭 이벤트
            tvMore?.setOnClickListener {
                if (!isExpanded) {
                    tvContent?.text = item.content
                    tvMore.visibility = View.GONE
                    isExpanded = true
                }
            }

            // 시간 처리
            tvTime?.text = getTimeAgo(item.date)

            // 팝업 메뉴 띄우기
            ivCommunity?.setOnClickListener { showProfilePopup(it) }

            root.setOnClickListener {
                val context = b.root.context
                if (context is FragmentActivity) {
                    val fragment = FeedDetailFragment.newInstance(item)
                    context.supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container_view, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        }

        // 좋아요/제개시/인용 클릭 이벤트
        val likeToggle = {
            item.isLiked = !item.isLiked
            item.likeCount += if (item.isLiked) 1 else -1
            tvLike?.text = item.likeCount.toString()
            updateLikeColor(tvLike!!, ivLike!!, item.isLiked)
        }
        tvLike?.setOnClickListener { likeToggle() }
        ivLike?.setOnClickListener { likeToggle() }

        val repostToggle = {
            item.isReposted = !item.isReposted
            item.repostCount += if (item.isReposted) 1 else -1
            tvRepost?.text = item.repostCount.toString()
            updateRepostColor(tvRepost!!, ivRepost!!, item.isReposted)
        }
        tvRepost?.setOnClickListener { repostToggle() }
        ivRepost?.setOnClickListener { repostToggle() }

        val quoteToggle = {
            item.isQuoted = !item.isQuoted
            item.quoteCount += if (item.isQuoted) 1 else -1
            tvQuote?.text = item.quoteCount.toString()
            updateQuoteColor(tvQuote!!, ivQuote!!, item.isQuoted)
        }
        tvQuote?.setOnClickListener { quoteToggle() }
        ivQuote?.setOnClickListener { quoteToggle() }

        // 팝업 메뉴 띄우기
        ivOverflow?.setOnClickListener { showOverflowPopup(it) }
        ivProfile?.setOnClickListener { showProfilePopup(it) }

        // 롱클릭 피드백
        val context = root.context as Activity
        listOf(
            tvLike to "like",
            ivLike to "like",
            tvRepost to "repost",
            ivRepost to "repost",
            tvQuote to "quote",
            ivQuote to "quote"
        ).forEach { (view, type) ->
            view?.setOnLongClickListener {
                showFeedbackBottomSheet(context, type, item)
                true
            }
        }

        // --- 인용 피드 처리 ---
        if (b is ItemFeedBinding || b is ItemFeedDetailBinding) {
            val quoteLayout = b.root.findViewById<ViewGroup>(R.id.layout_quote)

            if (item.quotedFeed != null) {
                val quoted = item.quotedFeed!!
                quoteLayout.visibility = View.VISIBLE
                quoteLayout.removeAllViews()

                val inflater = LayoutInflater.from(b.root.context)
                val quoteView = inflater.inflate(R.layout.item_feed_quote, quoteLayout, false)
                quoteLayout.addView(quoteView)

                // 텍스트 설정
                quoteView.findViewById<TextView>(R.id.tv_username)?.text = quoted.username
                quoteView.findViewById<TextView>(R.id.tv_content)?.let { tv ->
                    val maxLength = if (quoted.imageUrls.isEmpty()) 105 else 50
                    val isLongText = quoted.content.length > maxLength
                    tv.text = quoted.content.take(maxLength) + if (isLongText) "..." else ""
                    quoteView.findViewById<TextView>(R.id.tv_more)?.visibility =
                        if (isLongText) View.VISIBLE else View.GONE
                }

                // 이미지 레이아웃 초기화 및 적용
                val layoutImage1 = quoteView.findViewById<View>(R.id.layout_image1)
                val layoutImage2 = quoteView.findViewById<View>(R.id.layout_image2)
                val layoutImage3 = quoteView.findViewById<View>(R.id.layout_image3)
                val layoutImage4 = quoteView.findViewById<View>(R.id.layout_image4)

                layoutImage1?.visibility = View.GONE
                layoutImage2?.visibility = View.GONE
                layoutImage3?.visibility = View.GONE
                layoutImage4?.visibility = View.GONE

                when (quoted.imageUrls.size) {
                    1 -> {
                        layoutImage1?.visibility = View.VISIBLE
                        loadImages(layoutImage1 as ViewGroup, quoted.imageUrls)
                    }
                    2 -> {
                        layoutImage2?.visibility = View.VISIBLE
                        loadImages(layoutImage2 as ViewGroup, quoted.imageUrls)
                    }
                    3 -> {
                        layoutImage3?.visibility = View.VISIBLE
                        loadImages(layoutImage3 as ViewGroup, quoted.imageUrls)
                    }
                    4 -> {
                        layoutImage4?.visibility = View.VISIBLE
                        loadImages(layoutImage4 as ViewGroup, quoted.imageUrls)
                    }
                }

                // 인용 피드 클릭 시 디테일 이동
                quoteView.setOnClickListener {
                    val context = b.root.context
                    if (context is FragmentActivity) {
                        val fragment = FeedDetailFragment.newInstance(quoted)
                        context.supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container_view, fragment)
                            .addToBackStack(null)
                            .commit()
                    }
                }

            } else {
                quoteLayout.visibility = View.GONE
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

    private fun updateQuoteColor(textView: TextView?, imageView: ImageView?, quoted: Boolean) {
        val context = textView?.context ?: return
        val color = context.getColor(if (quoted) R.color.point_purple else R.color.gray2)
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

    private fun getTimeAgo(dateStr: String): String {
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

        // 피드백 더미 데이터
        val likeUsers = List(17) { "user${it + 1}" }
        val repostUsers = List(22) { "user${it + 11}" }
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
                content = "또 다른 사용자가 이 피드를 인용했어요.".repeat(10),
                quotedFeed = feedItem
            ),
            FeedItem(
                username = "인용러C",
                community = feedItem.community,
                date = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date()),
                content = "인용했어요",
                imageUrls = listOf(
                    "https://picsum.photos/300/200?random=2",
                    "https://picsum.photos/300/200?random=3",
                    "https://picsum.photos/300/200?random=3",
                    "https://picsum.photos/300/200?random=3"
                ),
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

                val screenHeight = context.resources.displayMetrics.heightPixels
                val maxHeight = (screenHeight * 0.64).toInt()
                it.layoutParams.height = maxHeight
                it.requestLayout()

                behavior.peekHeight = (330 * context.resources.displayMetrics.density).toInt()
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                behavior.skipCollapsed = false
                behavior.isDraggable = true
                behavior.isHideable = true
            }
        }
        dialog.show()
    }

    private fun loadImages(layout: ViewGroup, urls: List<String>) {
        val ids = listOf(
            R.id.iv_image1,
            R.id.iv_image2,
            R.id.iv_image3,
            R.id.iv_image4
        )

        val cornerRadius = 10

        for (i in urls.indices) {
            val iv = layout.findViewById<ImageView>(ids[i])

            Glide.with(iv)
                .load(urls[i])
                .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(cornerRadius)))
                .into(iv)
        }
    }
}