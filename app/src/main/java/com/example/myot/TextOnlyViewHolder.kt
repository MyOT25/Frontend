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
import com.bumptech.glide.Glide
import com.example.myot.databinding.ItemFeedTextOnlyBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.floor

class TextOnlyViewHolder(private val binding: ItemFeedTextOnlyBinding,
                         private val isDetail: Boolean = false) :
    RecyclerView.ViewHolder(binding.root) {

    private var isExpanded = false

    fun bind(item: FeedItem, isDetail: Boolean = false) {
        binding.tvUsername.text = item.username
        binding.tvDate.text = item.date
        binding.tvTime.text = getTimeAgo(item.date)
        binding.tvComment.text = item.commentCount.toString()
        binding.tvLike.text = item.likeCount.toString()
        binding.tvRepost.text = item.repostCount.toString()
        binding.tvBookmark.text = item.bookmarkCount.toString()

        val text = item.content
        val isLongText = text.length > 160
        var isExpanded = false

        binding.tvContent.text = when {
            isDetail -> text
            isLongText -> text.take(160) + "..."
            else -> text
        }

        binding.tvMore.visibility = if (!isDetail && isLongText) View.VISIBLE else View.GONE

        binding.tvMore.setOnClickListener {
            if (!isExpanded) {
                binding.tvContent.text = text
                binding.tvMore.visibility = View.GONE
                isExpanded = true
            }
        }
        // 초기 색상 설정
        updateLikeColor(binding.tvLike, binding.ivLike, item.isLiked)
        updateRepostColor(binding.tvRepost, binding.ivRepost, item.isReposted)
        updateBookmarkColor(binding.tvBookmark, binding.ivBookmark, item.isBookmarked)

        // 좋아요 토글
        val likeToggle = {
            item.isLiked = !item.isLiked
            item.likeCount += if (item.isLiked) 1 else -1
            binding.tvLike.text = item.likeCount.toString()
            updateLikeColor(binding.tvLike, binding.ivLike, item.isLiked)
        }
        binding.tvLike.setOnClickListener { likeToggle() }
        binding.ivLike.setOnClickListener { likeToggle() }

        // 리포스트 토글
        val repostToggle = {
            item.isReposted = !item.isReposted
            item.repostCount += if (item.isReposted) 1 else -1
            binding.tvRepost.text = item.repostCount.toString()
            updateRepostColor(binding.tvRepost, binding.ivRepost, item.isReposted)
        }
        binding.tvRepost.setOnClickListener { repostToggle() }
        binding.ivRepost.setOnClickListener { repostToggle() }

        // 북마크 토글
        val bookmarkToggle = {
            item.isBookmarked = !item.isBookmarked
            item.bookmarkCount += if (item.isBookmarked) 1 else -1
            binding.tvBookmark.text = item.bookmarkCount.toString()
            updateBookmarkColor(binding.tvBookmark, binding.ivBookmark, item.isBookmarked)
        }
        binding.tvBookmark.setOnClickListener { bookmarkToggle() }
        binding.ivBookmark.setOnClickListener { bookmarkToggle() }

        // 팝업 관련
        binding.ivOverflow.setOnClickListener {
            showOverflowPopup(it)
        }
        binding.ivCommunity.setOnClickListener {
            showProfilePopup(it)
        }
        binding.ivProfile.setOnClickListener {
            showProfilePopup(it)
        }

        // 피드백 홀드시 피드백 바텀시프트 이동
        binding.ivLike.setOnLongClickListener {
            showFeedbackBottomSheet(binding.root.context as android.app.Activity, "like")
            isDetail
        }
        binding.tvLike.setOnLongClickListener {
            showFeedbackBottomSheet(binding.root.context as android.app.Activity, "like")
            isDetail
        }

        binding.ivRepost.setOnLongClickListener {
            showFeedbackBottomSheet(binding.root.context as android.app.Activity, "repost")
            isDetail
        }
        binding.tvRepost.setOnLongClickListener {
            showFeedbackBottomSheet(binding.root.context as android.app.Activity, "repost")
            isDetail
        }

        binding.ivBookmark.setOnLongClickListener {
            showFeedbackBottomSheet(binding.root.context as android.app.Activity, "quote")
            isDetail
        }
        binding.tvBookmark.setOnLongClickListener {
            showFeedbackBottomSheet(binding.root.context as android.app.Activity, "quote")
            isDetail
        }

        // 인용 피드 관련 처리
        val quoteLayout = binding.layoutQuote

        if (item.quotedFeed != null) {
            val quoted = item.quotedFeed!!
            quoteLayout.visibility = View.VISIBLE
            quoteLayout.removeAllViews()

            val inflater = LayoutInflater.from(binding.root.context)
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

        // 피드 클릭
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

    private fun updateLikeColor(textView: TextView, imageView: ImageView, liked: Boolean) {
        val color = textView.context.getColor(if (liked) R.color.point_pink else R.color.gray2)
        textView.setTextColor(color)
        imageView.setColorFilter(color)
    }

    private fun updateRepostColor(textView: TextView, imageView: ImageView, reposted: Boolean) {
        val color = textView.context.getColor(if (reposted) R.color.point_blue else R.color.gray2)
        textView.setTextColor(color)
        imageView.setColorFilter(color)
    }

    private fun updateBookmarkColor(textView: TextView, imageView: ImageView, bookmarked: Boolean) {
        val color = textView.context.getColor(if (bookmarked) R.color.point_purple else R.color.gray2)
        textView.setTextColor(color)
        imageView.setColorFilter(color)
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

    private fun showFeedbackBottomSheet(context: Activity, defaultType: String) {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.bottomsheet_feed_feedback, null)
        dialog.setContentView(view)

        val tabLayout = view.findViewById<TabLayout>(R.id.tab_feedback)
        val viewPager = view.findViewById<ViewPager2>(R.id.vp_feedback)

        dialog.window?.setDimAmount(0.1f)

        // 피드백 데이터
        val feedbackMap = mapOf(
            "like" to List(11) { "user${it + 1}" },
            "repost" to List(3) { "user${it + 4}" },
            "quote" to List(2) { "user${it + 2}" }
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

                // 닫을 수 있게 설정
                behavior.isHideable = true
                behavior.skipCollapsed = false
                behavior.isDraggable = true
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED

                val screenHeight = context.resources.displayMetrics.heightPixels
                val maxHeight = (screenHeight * 0.64).toInt()
                it.layoutParams.height = maxHeight
                it.requestLayout()
            }
        }

        dialog.show()
    }

}
