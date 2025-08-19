package com.example.myot.feed.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.myot.R
import com.example.myot.databinding.DialogImageViewBinding
import com.example.myot.feed.adapter.FeedbackPagerAdapter
import com.example.myot.feed.model.FeedItem
import com.example.myot.retrofit2.RetrofitClient
import com.example.myot.retrofit2.TokenStore
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageDialogFragment(
    private val imageUrl: String,
    private val feedItem: FeedItem
) : DialogFragment() {

    private lateinit var binding: DialogImageViewBinding
    private var downY = 0f
    private var isDragging = false

    private var isLiked = false
    private var isReposted = false
    private var isQuoted = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), android.R.style.Theme_DeviceDefault_Light_NoActionBar)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = DialogImageViewBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)

        Glide.with(requireContext())
            .load(imageUrl)
            .into(binding.fullImageView)

        val imageView = binding.fullImageView

        imageView.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaY = event.rawY - downY
                    if (deltaY > 0) {
                        isDragging = true
                        imageView.translationY = deltaY

                        val ratio = (1f - (deltaY / 300f)).coerceIn(0f, 1f)

                        binding.feedbackContainer.alpha = ratio
                        binding.ivOverflow.alpha = ratio
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val deltaY = event.rawY - downY
                    if (deltaY > 200) {
                        dismiss()
                    } else {
                        // 이미지 복귀
                        imageView.animate()
                            .translationY(0f)
                            .alpha(1f)
                            .setDuration(200)
                            .start()

                        // 피드백 + 오버플로우 버튼 복귀
                        binding.feedbackContainer.animate()
                            .alpha(1f)
                            .setDuration(200)
                            .start()

                        binding.ivOverflow.animate()
                            .alpha(1f)
                            .setDuration(200)
                            .start()
                    }
                    true
                }
                else -> false
            }
        }

        // 팝업 메뉴 띄우기
        binding.ivOverflow.setOnClickListener {
            showImageOverflowPopup(it)
        }

        // 피드백 기능
        isLiked = feedItem.isLiked
        isReposted = feedItem.isReposted
        isQuoted = feedItem.isQuoted

        updateFeedbackViews()

        setFeedbackListeners(binding.tvLike, binding.ivLike, "like")
        setFeedbackListeners(binding.tvRepost, binding.ivRepost, "repost")
        setFeedbackListeners(binding.tvQuote, binding.ivQuote, "quote")

        return dialog
    }

    private fun setFeedbackListeners(
        textView: TextView,
        imageView: ImageView,
        type: String
    ) {
        val toggleAction = {
            when (type) {
                "like" -> {
                    isLiked = !isLiked
                    feedItem.isLiked = isLiked
                    feedItem.likeCount += if (isLiked) 1 else -1
                }
                "repost" -> {
                    isReposted = !isReposted
                    feedItem.isReposted = isReposted
                    feedItem.repostCount += if (isReposted) 1 else -1
                }
                "quote" -> {
                    isQuoted = !isQuoted
                    feedItem.isQuoted = isQuoted
                    feedItem.quoteCount += if (isQuoted) 1 else -1
                }
            }
            updateFeedbackViews()
        }

        val longClickAction = {
            showFeedbackBottomSheet(requireActivity(), type, feedItem) {
                dismiss()
            }
            true
        }

        textView.setOnClickListener { toggleAction() }
        imageView.setOnClickListener { toggleAction() }

        textView.setOnLongClickListener { longClickAction() }
        imageView.setOnLongClickListener { longClickAction() }
    }

    private fun showFeedbackBottomSheet(context: Activity, defaultType: String, feedItem: FeedItem, onFeedClick: () -> Unit ) {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.bottomsheet_feed_feedback, null)
        dialog.setContentView(view)

        val tabLayout = view.findViewById<TabLayout>(R.id.tab_feedback)
        val viewPager = view.findViewById<ViewPager2>(R.id.vp_feedback)

        dialog.window?.setDimAmount(0.1f)

        // 어댑터: 빈 상태로 시작 (API 응답으로 채움)
        val fa = context as? FragmentActivity ?: return
        val pagerAdapter = FeedbackPagerAdapter(
            fa = fa,
            dialog = dialog,
            onFeedClick = onFeedClick
        )
        viewPager.adapter = pagerAdapter

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

        // 좋아요/재게시 목록 API 호출하여 어댑터에 주입
        fa.lifecycleScope.launch {
            try {
                val raw = TokenStore.loadAccessToken(fa)
                val bearer = raw?.trim()
                    ?.removePrefix("Bearer ")
                    ?.trim()
                    ?.removeSurrounding("\"")
                    ?.let { "Bearer $it" }
                    ?: ""

                if (feedItem.id > 0L) {
                    // 1) 좋아요 목록
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
                                loginId = null, // 좋아요 응답에는 로그인 아이디 없음
                                profileImage = it.profileImage
                            )
                        }
                        pagerAdapter.submitLikeUsers(likeUsersUi)
                    }

                    // 2) 재게시 사용자 목록
                    val repostRes = withContext(Dispatchers.IO) {
                        RetrofitClient.feedService.getRepostedUsers(
                            token = bearer,
                            postId = feedItem.id,
                            page = 1,
                            limit = 20
                        )
                    }
                    if (repostRes.isSuccessful) {
                        val entries = repostRes.body()?.success.orEmpty()
                        val repostUsersUi = entries.mapNotNull { e ->
                            e.user?.let { u ->
                                com.example.myot.feed.model.FeedbackUserUi(
                                    nickname = u.nickname ?: u.loginId ?: "",
                                    loginId = u.loginId,
                                    profileImage = u.profileImage
                                )
                            }
                        }
                        pagerAdapter.submitRepostUsers(repostUsersUi)
                    }
                }
            } catch (_: Exception) {
                // 조용히 실패 처리
            }
        }

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

    private fun updateFeedbackViews() {
        binding.tvLike.text = feedItem.likeCount.toString()
        binding.tvRepost.text = feedItem.repostCount.toString()
        binding.tvQuote.text = feedItem.quoteCount.toString()
        binding.tvComment.text = feedItem.commentCount.toString()

        updateLikeColor(binding.tvLike, binding.ivLike, isLiked)
        updateRepostColor(binding.tvRepost, binding.ivRepost, isReposted)
        updateQuoteColor(binding.tvQuote, binding.ivQuote, isQuoted)
    }

    private fun updateLikeColor(textView: TextView?, imageView: ImageView?, liked: Boolean) {
        val context = textView?.context ?: return
        val color = context.getColor(if (liked) R.color.point_pink else R.color.white)
        textView.setTextColor(color)
        imageView?.setColorFilter(color)
    }

    private fun updateRepostColor(textView: TextView?, imageView: ImageView?, reposted: Boolean) {
        val context = textView?.context ?: return
        val color = context.getColor(if (reposted) R.color.point_blue else R.color.white)
        textView.setTextColor(color)
        imageView?.setColorFilter(color)
    }

    private fun updateQuoteColor(textView: TextView?, imageView: ImageView?, quoted: Boolean) {
        val context = textView?.context ?: return
        val color = context.getColor(if (quoted) R.color.point_purple else R.color.white)
        textView.setTextColor(color)
        imageView?.setColorFilter(color)
    }

    private fun showImageOverflowPopup(anchor: View) {
        val context = anchor.context
        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(R.layout.menu_popup_img, null)

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

        popupWindow.setBackgroundDrawable(null)
        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true
        popupWindow.elevation = 20f

        val offsetX = anchor.width - popupWidth - 20
        val offsetY = anchor.height + 7

        popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, anchorX + offsetX, anchorY + offsetY)

        // 리스너 연결
        popupView.findViewById<View>(R.id.btn_share).setOnClickListener {
            popupWindow.dismiss()
            Toast.makeText(context, "공유 클릭", Toast.LENGTH_SHORT).show()
        }

        popupView.findViewById<View>(R.id.btn_save).setOnClickListener {
            popupWindow.dismiss()
            Toast.makeText(context, "사진 저장 클릭", Toast.LENGTH_SHORT).show()
        }
    }
}