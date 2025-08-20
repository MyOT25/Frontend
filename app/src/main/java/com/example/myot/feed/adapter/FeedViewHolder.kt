package com.example.myot.feed.adapter

import android.util.Log

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.myot.retrofit2.TokenStore
import com.example.myot.retrofit2.RetrofitClient
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.example.myot.feed.ui.FeedDetailFragment
import com.example.myot.feed.model.FeedItem
import com.example.myot.feed.ui.ImageDialogFragment
import com.example.myot.profile.ProfileFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.math.floor
import kotlin.math.max

class FeedViewHolder(
    private val binding: ViewBinding,
    private val onItemClick: (FeedItem) -> Unit,
    private val onDeleteRequest: (Long) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private var likeInFlight = false
    private var bookmarkInFlight = false

    fun bind(item: FeedItem, isLastItem: Boolean = false) {
        when (binding) {
            is ItemFeedBinding -> bindViews(binding, item, isLastItem)
            is ItemFeedDetailBinding -> bindViews(binding, item, isLastItem)
        }
    }

    private val DISPLAY_SDF = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())

    private fun <B : ViewBinding> bindViews(
        b: B,
        item: FeedItem,
        isLastItem: Boolean
    ) {
        val root = b.root

        val tvUsername = root.findViewById<TextView>(R.id.tv_username)
        val tvUserId   = root.findViewById<TextView>(R.id.tv_userid)
        val tvContent  = root.findViewById<TextView>(R.id.tv_content)
        val tvDate     = root.findViewById<TextView>(R.id.tv_date)
        val tvComment  = root.findViewById<TextView>(R.id.tv_comment)
        val tvLike     = root.findViewById<TextView>(R.id.tv_like)
        val tvRepost   = root.findViewById<TextView>(R.id.tv_repost)
        val tvQuote    = root.findViewById<TextView>(R.id.tv_quote)

        val ivProfile   = root.findViewById<ImageView>(R.id.iv_profile)
        val ivCommunity = root.findViewById<ImageView>(R.id.iv_community)

        val layoutImageContainer = root.findViewById<View>(R.id.layout_image_container)
        val layoutQuote          = root.findViewById<ViewGroup>(R.id.layout_quote)

        val ivLike     = root.findViewById<ImageView>(R.id.iv_like)
        val ivRepost   = root.findViewById<ImageView>(R.id.iv_repost)
        val ivQuote    = root.findViewById<ImageView>(R.id.iv_quote)
        val ivOverflow = root.findViewById<ImageView>(R.id.iv_overflow)
        val ivDivLine  = root.findViewById<View>(R.id.iv_div_line)

        // 텍스트/카운트
        tvUsername?.text = item.username
        tvContent?.text  = item.content
        tvDate?.text     = item.date

        tvComment?.text  = item.commentCount.toString()
        tvLike?.text     = item.likeCount.toString()
        tvRepost?.text   = item.repostCount.toString()
        tvQuote?.text    = item.bookmarkCount.toString()

        updateLikeColor(tvLike, ivLike, item.isLiked)
        updateBookmarkColor(tvQuote, ivQuote, item.isBookmarked)


        if (item.userHandle.isNullOrBlank()) {
            tvUserId?.apply {
                text = ""
                visibility = View.INVISIBLE
            }
        } else {
            tvUserId?.apply {
                text = item.userHandle
                visibility = View.VISIBLE
            }
        }

        ivProfile?.let { iv ->
            val url = item.profileImageUrl
            if (url.isNullOrBlank()) {
                Glide.with(iv)
                    .load(R.drawable.ic_no_profile)
                    .circleCrop()
                    .into(iv)
            } else {
                Glide.with(iv)
                    .load(url)
                    .placeholder(R.drawable.ic_no_profile)
                    .error(R.drawable.ic_no_profile)
                    .circleCrop()
                    .into(iv)
            }
        }

        ivCommunity?.let { iv ->
            val url = item.communityCoverUrl
            if (url.isNullOrBlank()) {
                Glide.with(iv)
                    .load(R.drawable.ic_no_community)
                    .circleCrop()
                    .into(iv)
            } else {
                Glide.with(iv)
                    .load(url)
                    .placeholder(R.drawable.ic_no_community)
                    .error(R.drawable.ic_no_community)
                    .circleCrop()
                    .into(iv)
            }
        }

        listOf(
            R.id.layout_image1, R.id.layout_image2,
            R.id.layout_image3, R.id.layout_image4
        ).forEach { id -> root.findViewById<View>(id)?.visibility = View.GONE }

        if (item.imageUrls.isEmpty()) {
            layoutImageContainer?.visibility = View.GONE
        } else {
            layoutImageContainer?.visibility = View.VISIBLE
            when (item.imageUrls.size) {
                1 -> root.findViewById<ViewGroup>(R.id.layout_image1)?.let {
                    it.visibility = View.VISIBLE
                    loadImages(it, item.imageUrls, item)
                }
                2 -> root.findViewById<ViewGroup>(R.id.layout_image2)?.let {
                    it.visibility = View.VISIBLE
                    loadImages(it, item.imageUrls, item)
                }
                3 -> root.findViewById<ViewGroup>(R.id.layout_image3)?.let {
                    it.visibility = View.VISIBLE
                    loadImages(it, item.imageUrls, item)
                }
                else -> root.findViewById<ViewGroup>(R.id.layout_image4)?.let {
                    it.visibility = View.VISIBLE
                    loadImages(it, item.imageUrls.take(4), item)
                }
            }
        }

        layoutQuote?.visibility = View.GONE

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

            b.root.setOnClickListener {
                onItemClick(item)
            }
        }

        // 좋아요 기능
        val likeClickListener = View.OnClickListener {
            val tv = tvLike ?: return@OnClickListener
            val iv = ivLike ?: return@OnClickListener
            if (likeInFlight) return@OnClickListener
            likeInFlight = true
            tv.isEnabled = false
            iv.isEnabled = false

            val prevLiked = item.isLiked
            val prevCount = item.likeCount
            item.isLiked = !prevLiked
            item.likeCount = kotlin.math.max(0, prevCount + if (item.isLiked) 1 else -1)
            tv.text = item.likeCount.toString()
            updateLikeColor(tv, iv, item.isLiked)

            val activity = findFragmentActivity(b.root.context)
            if (activity == null) {
                // 롤백: lifecycleOwner 없음
                item.isLiked = prevLiked
                item.likeCount = prevCount
                tv.text = item.likeCount.toString()
                updateLikeColor(tv, iv, item.isLiked)
                likeInFlight = false
                tv.isEnabled = true
                iv.isEnabled = true
                Toast.makeText(b.root.context, "화면 컨텍스트를 찾을 수 없어요.", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            activity.lifecycleScope.launch {
                try {
                    val raw = TokenStore.loadAccessToken(activity)
                    if (raw.isNullOrBlank()) {
                        // 롤백: 로그인 필요
                        item.isLiked = prevLiked
                        item.likeCount = prevCount
                        tv.text = item.likeCount.toString()
                        updateLikeColor(tv, iv, item.isLiked)
                        likeInFlight = false
                        tv.isEnabled = true
                        iv.isEnabled = true
                        android.widget.Toast.makeText(activity, "로그인이 필요합니다.", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        val cleaned = raw.trim()
                            .removePrefix("Bearer ")
                            .trim()
                            .removeSurrounding("\"")
                        val bearer = "Bearer $cleaned"

                        if (item.id <= 0L) {
                            // 롤백: 잘못된 게시글 ID
                            item.isLiked = prevLiked
                            item.likeCount = prevCount
                            tv.text = item.likeCount.toString()
                            updateLikeColor(tv, iv, item.isLiked)
                            likeInFlight = false
                            tv.isEnabled = true
                            iv.isEnabled = true
                            android.widget.Toast.makeText(activity, "유효하지 않은 게시글입니다.", android.widget.Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        val res = withContext(Dispatchers.IO) {
                            RetrofitClient.feedService.toggleLike(bearer, item.id)
                        }
                        if (res.isSuccessful) {
                            // 토글 API 특성상 새로운 상태는 이전 상태의 반대가 되어야 함
                            val newLiked = !prevLiked
                            item.isLiked = newLiked
                            item.likeCount = kotlin.math.max(0, prevCount + if (newLiked) 1 else -1)
                            tv.text = item.likeCount.toString()
                            updateLikeColor(tv, iv, item.isLiked)

                            res.body()?.message?.takeIf { it.isNotBlank() }?.let {
                                android.widget.Toast.makeText(activity, it, android.widget.Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // 롤백: HTTP 에러
                            item.isLiked = prevLiked
                            item.likeCount = prevCount
                            tv.text = item.likeCount.toString()
                            updateLikeColor(tv, iv, item.isLiked)
                            val errMsg = withContext(Dispatchers.IO) { res.errorBody()?.string() }?.take(120) ?: ""
                            android.widget.Toast.makeText(
                                activity,
                                "좋아요 처리 실패 (${res.code()}) ${errMsg}",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    // 롤백: 예외
                    item.isLiked = prevLiked
                    item.likeCount = prevCount
                    tv.text = item.likeCount.toString()
                    updateLikeColor(tv, iv, item.isLiked)
                    android.widget.Toast.makeText(activity, "네트워크 오류: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                } finally {
                    likeInFlight = false
                    tv.isEnabled = true
                    iv.isEnabled = true
                }
            }
        }
        tvLike?.setOnClickListener(likeClickListener)
        ivLike?.setOnClickListener(likeClickListener)

        val repostToggle = {
            item.isReposted = !item.isReposted
            item.repostCount += if (item.isReposted) 1 else -1
            tvRepost?.text = item.repostCount.toString()
            updateRepostColor(tvRepost!!, ivRepost!!, item.isReposted)
        }
        tvRepost?.setOnClickListener { repostToggle() }
        ivRepost?.setOnClickListener { repostToggle() }

        val bookmarkClickListener = View.OnClickListener {
            val tv = tvQuote ?: return@OnClickListener
            val iv = ivQuote ?: return@OnClickListener
            if (bookmarkInFlight) return@OnClickListener
            bookmarkInFlight = true
            tv.isEnabled = false
            iv.isEnabled = false

            val prevBookmarked = item.isBookmarked
            val prevCount = item.bookmarkCount
            // Optimistic UI
            item.isBookmarked = !prevBookmarked
            item.bookmarkCount = kotlin.math.max(0, prevCount + if (item.isBookmarked) 1 else -1)
            tv.text = item.bookmarkCount.toString()
            updateBookmarkColor(tv, iv, item.isBookmarked)

            val activity = findFragmentActivity(b.root.context)
            if (activity == null) {
                // 롤백: lifecycleOwner 없음
                item.isBookmarked = prevBookmarked
                item.bookmarkCount = prevCount
                tv.text = item.bookmarkCount.toString()
                updateBookmarkColor(tv, iv, item.isBookmarked)
                bookmarkInFlight = false
                tv.isEnabled = true
                iv.isEnabled = true
                Toast.makeText(b.root.context, "화면 컨텍스트를 찾을 수 없어요.", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }

            activity.lifecycleScope.launch {
                try {
                    val raw = TokenStore.loadAccessToken(activity)
                    if (raw.isNullOrBlank()) {
                        // 롤백: 로그인 필요
                        item.isBookmarked = prevBookmarked
                        item.bookmarkCount = prevCount
                        tv.text = item.bookmarkCount.toString()
                        updateBookmarkColor(tv, iv, item.isBookmarked)
                        bookmarkInFlight = false
                        tv.isEnabled = true
                        iv.isEnabled = true
                        android.widget.Toast.makeText(activity, "로그인이 필요합니다.", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        val cleaned = raw.trim()
                            .removePrefix("Bearer ")
                            .trim()
                            .removeSurrounding("\"")
                        val bearer = "Bearer $cleaned"

                        if (item.id <= 0L) {
                            // 롤백: 잘못된 게시글 ID
                            item.isBookmarked = prevBookmarked
                            item.bookmarkCount = prevCount
                            tv.text = item.bookmarkCount.toString()
                            updateBookmarkColor(tv, iv, item.isBookmarked)
                            bookmarkInFlight = false
                            tv.isEnabled = true
                            iv.isEnabled = true
                            android.widget.Toast.makeText(activity, "유효하지 않은 게시글입니다.", android.widget.Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        val res = withContext(Dispatchers.IO) {
                            if (!prevBookmarked) RetrofitClient.feedService.addBookmark(bearer, item.id)
                            else RetrofitClient.feedService.deleteBookmark(bearer, item.id)
                        }
                        if (res.isSuccessful) {
                            // 성공: 토글 확정
                            val newBookmarked = !prevBookmarked
                            item.isBookmarked = newBookmarked
                            item.bookmarkCount = kotlin.math.max(0, prevCount + if (newBookmarked) 1 else -1)
                            tv.text = item.bookmarkCount.toString()
                            updateBookmarkColor(tv, iv, item.isBookmarked)

                            res.body()?.message?.takeIf { it.isNotBlank() }?.let {
                                android.widget.Toast.makeText(activity, it, android.widget.Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // 롤백: HTTP 에러
                            item.isBookmarked = prevBookmarked
                            item.bookmarkCount = prevCount
                            tv.text = item.bookmarkCount.toString()
                            updateBookmarkColor(tv, iv, item.isBookmarked)
                            val errMsg = withContext(Dispatchers.IO) { res.errorBody()?.string() }?.take(120) ?: ""
                            android.widget.Toast.makeText(
                                activity,
                                "북마크 처리 실패 (${res.code()}) ${errMsg}",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    // 롤백: 예외
                    item.isBookmarked = prevBookmarked
                    item.bookmarkCount = prevCount
                    tv.text = item.bookmarkCount.toString()
                    updateBookmarkColor(tv, iv, item.isBookmarked)
                    android.widget.Toast.makeText(activity, "네트워크 오류: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                } finally {
                    bookmarkInFlight = false
                    tv.isEnabled = true
                    iv.isEnabled = true
                }
            }
        }
        tvQuote?.setOnClickListener(bookmarkClickListener)
        ivQuote?.setOnClickListener(bookmarkClickListener)

        // 팝업 메뉴 띄우기
        ivOverflow?.setOnClickListener { showOverflowPopup(it, item) }
        ivProfile?.setOnClickListener { showProfilePopup(it) }

        // 롱클릭 피드백
        val context = root.context as Activity
        listOf(
            tvLike to "like",
            ivLike to "like",
            tvRepost to "repost",
            ivRepost to "repost",
            tvQuote to "bookmark",
            ivQuote to "bookmark"
        ).forEach { (view, type) ->
            view?.setOnLongClickListener {
                showFeedbackBottomSheet(context, type, item)
                true
            }
        }

        val isDetail = binding is ItemFeedDetailBinding

        if (isDetail) {
            val backButton = b.root.findViewById<ImageView>(R.id.iv_back)
            backButton?.setOnClickListener {
                (b.root.context as? AppCompatActivity)?.supportFragmentManager?.popBackStack()
            }
        }

        // --- 인용 피드 처리 ---
        if (b is ItemFeedBinding || b is ItemFeedDetailBinding) {
            val quoteLayout = b.root.findViewById<ViewGroup>(R.id.layout_quote)

            if (item.quotedFeed != null) {
                val quoted = item.quotedFeed!!
                quoteLayout.visibility = View.VISIBLE
                quoteLayout.removeAllViews()

                val isDetail = binding is ItemFeedDetailBinding
                val inflater = LayoutInflater.from(b.root.context)
                val layoutResId = if (isDetail) {
                    R.layout.item_feed_detail_quote
                } else {
                    R.layout.item_feed_quote
                }
                val quoteView = inflater.inflate(layoutResId, quoteLayout, false)
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
                        loadImages(layoutImage1 as ViewGroup, quoted.imageUrls, quoted)
                    }
                    2 -> {
                        layoutImage2?.visibility = View.VISIBLE
                        loadImages(layoutImage2 as ViewGroup, quoted.imageUrls, quoted)
                    }
                    3 -> {
                        layoutImage3?.visibility = View.VISIBLE
                        loadImages(layoutImage3 as ViewGroup, quoted.imageUrls, quoted)
                    }
                    4 -> {
                        layoutImage4?.visibility = View.VISIBLE
                        loadImages(layoutImage4 as ViewGroup, quoted.imageUrls, quoted)
                    }
                }
            } else {
                quoteLayout.visibility = View.GONE
            }
        }
    }

    private fun findFragmentActivity(ctx: Context): FragmentActivity? {
        var current: Context? = ctx
        while (current is ContextWrapper) {
            if (current is FragmentActivity) return current
            current = current.baseContext
        }
        return null
    }

    private fun updateLikeColor(textView: TextView?, imageView: ImageView?, liked: Boolean) {
        val context = textView?.context ?: return
        val isDetail = binding is ItemFeedDetailBinding
        val inactiveColor = if (isDetail) R.color.gray3 else R.color.gray2
        val color = context.getColor(if (liked) R.color.point_pink else inactiveColor)
        textView.setTextColor(color)
        imageView?.setColorFilter(color)
    }
    private fun updateRepostColor(textView: TextView?, imageView: ImageView?, reposted: Boolean) {
        val context = textView?.context ?: return
        val isDetail = binding is ItemFeedDetailBinding
        val inactiveColor = if (isDetail) R.color.gray3 else R.color.gray2
        val color = context.getColor(if (reposted) R.color.point_blue else inactiveColor)
        textView.setTextColor(color)
        imageView?.setColorFilter(color)
    }

    private fun updateBookmarkColor(textView: TextView?, imageView: ImageView?, bookmarked: Boolean) {
        val context = textView?.context ?: return
        val isDetail = binding is ItemFeedDetailBinding
        val inactiveColor = if (isDetail) R.color.gray3 else R.color.gray2
        val color = context.getColor(if (bookmarked) R.color.point_purple else inactiveColor)
        textView.setTextColor(color)
        imageView?.setColorFilter(color)
    }

    private fun showOverflowPopup(anchor: View, item: FeedItem) {
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
            onDeleteRequest(item.id)
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

        val offsetX = anchor.width - popupWidth + 380
        val offsetY = anchor.height + 20

        popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, anchorX + offsetX, anchorY + offsetY)

        popupView.findViewById<View>(R.id.btn_community).setOnClickListener {
            popupWindow.dismiss()
            Toast.makeText(context, "‘ABC’ 커뮤니티 이동", Toast.LENGTH_SHORT).show()
        }

        popupView.findViewById<View>(R.id.btn_user_profile).setOnClickListener {
            popupWindow.dismiss()

            val activity = anchor.context as? FragmentActivity ?: return@setOnClickListener
            val fragment = ProfileFragment.newInstance(1L) // 임시 userId = 1 전달
            activity.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun getTimeAgo(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return "방금 전"

        val patterns = listOf(
            "yyyy/MM/dd HH:mm",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
            "yyyy-MM-dd'T'HH:mm:ssZ"
        )

        var postTime: Date? = null
        for (p in patterns) {
            try {
                val fmt = SimpleDateFormat(p, Locale.getDefault()).apply {
                    if (p.contains("'Z'")) timeZone = TimeZone.getTimeZone("UTC")
                }
                postTime = fmt.parse(dateStr)
                if (postTime != null) break
            } catch (_: Exception) {  }
        }

        if (postTime == null) return "방금 전"

        val diffMillis = max(0L, System.currentTimeMillis() - postTime.time)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
        val hours   = TimeUnit.MILLISECONDS.toHours(diffMillis)
        val days    = TimeUnit.MILLISECONDS.toDays(diffMillis)
        val months  = (days / 30)
        val years   = (days / 365)

        return when {
            minutes < 1  -> "방금 전"
            minutes < 60 -> "${minutes}분 전"
            hours   < 24 -> "${hours}시간 전"
            days   <= 30 -> "${days}일 전"
            days   < 365 -> "${months}개월 전"
            else         -> "${years}년 전"
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

        // 어댑터 1회만 생성하고, 이후 submit* 메서드로 채움
        val pagerAdapter = FeedbackPagerAdapter(
            activity,
            dialog
        ) { dialog.dismiss() }
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) { 0 -> "좋아요"; 1 -> "재게시"; 2 -> "인용"; else -> "" }
        }.attach()

        val startIndex = when (defaultType) {
            "like" -> 0
            "repost" -> 1
            "quote", "bookmark" -> 2
            else -> 0
        }
        viewPager.setCurrentItem(startIndex, false)
        viewPager.offscreenPageLimit = 3

        // Lazy-load 플래그
        var likesLoaded = false
        var repostsLoaded = false

        // 공통: 토큰 준비
        suspend fun getBearer(): String {
            val raw = TokenStore.loadAccessToken(activity)
            return raw?.trim()
                ?.removePrefix("Bearer ")
                ?.trim()
                ?.removeSurrounding("\"")
                ?.let { "Bearer $it" }
                ?: ""
        }

        // 좋아요 로딩
        fun loadLikes() {
            if (likesLoaded || feedItem.id <= 0L) return
            activity.lifecycleScope.launch {
                try {
                    val bearer = getBearer()
                    val res = withContext(Dispatchers.IO) {
                        RetrofitClient.feedService.getPostLikes(
                            token = bearer,
                            postId = feedItem.id,
                            page = 1,
                            limit = 20
                        )
                    }
                    if (res.isSuccessful) {
                        val body = res.body()

                        val raw: List<com.example.myot.feed.data.PostLikeUser> =
                            body?.success?.users ?: emptyList()

                        val ui = raw.map { u ->
                            com.example.myot.feed.model.FeedbackUserUi(
                                nickname = u.nickname ?: "",
                                loginId = u.loginId,
                                profileImage = u.profileImage
                            )
                        }

                        pagerAdapter.submitLikeUsers(ui)
                        likesLoaded = true
                    }
                } catch (_: Exception) { }
            }
        }

        // 재게시 로딩
        fun loadReposts() {
            if (repostsLoaded || feedItem.id <= 0L) return
            activity.lifecycleScope.launch {
                try {
                    val bearer = getBearer()
                    val res = withContext(Dispatchers.IO) {
                        RetrofitClient.feedService.getRepostedUsers(
                            token = bearer,
                            postId = feedItem.id,
                            page = 1,
                            limit = 20
                        )
                    }
                    if (res.isSuccessful) {
                        val entries = res.body()?.success.orEmpty()
                        val ui = entries.mapNotNull { e ->
                            val u = e.user ?: return@mapNotNull null
                            com.example.myot.feed.model.FeedbackUserUi(
                                nickname = u.nickname ?: u.loginId ?: "",
                                loginId = u.loginId,
                                profileImage = u.profileImage
                            )
                        }
                        pagerAdapter.submitRepostUsers(ui)
                        repostsLoaded = true
                    }
                } catch (_: Exception) { }
            }
        }

        // 처음 눌러 들어온 탭만 즉시 로딩
        when (defaultType) {
            "like" -> loadLikes()
            "repost" -> loadReposts()
            "quote" -> {} // 필요시 구현
            else -> loadLikes()
        }

        // 옆으로 넘길 때 해당 탭 Lazy-Load
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> loadLikes()
                    1 -> loadReposts()
                }
            }
        })

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

    private fun loadImages(layout: ViewGroup, urls: List<String>, item: FeedItem) {
        val ids = listOf(R.id.iv_image1, R.id.iv_image2, R.id.iv_image3, R.id.iv_image4)
        val radiusPx = (10 * layout.resources.displayMetrics.density).toInt()

        for (i in urls.indices) {
            val iv = layout.findViewById<ImageView?>(ids.getOrNull(i) ?: continue) ?: continue

            Glide.with(iv)
                .load(urls[i])
                .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(radiusPx)))
                .into(iv)

            iv.setOnClickListener {
                (iv.context as? FragmentActivity)?.let { fa ->
                    ImageDialogFragment(imageUrl = urls[i], feedItem = item)
                        .show(fa.supportFragmentManager, "ImageDialog")
                }
            }
        }
    }
}