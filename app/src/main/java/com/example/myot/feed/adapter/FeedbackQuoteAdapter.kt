package com.example.myot.feed.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myot.R
import com.example.myot.feed.model.FeedItem
import com.example.myot.feed.ui.FeedDetailFragment
import com.google.android.material.bottomsheet.BottomSheetDialog

class FeedbackQuoteAdapter(
    private val quoteFeeds: List<FeedItem>,
    private val dialog: BottomSheetDialog,
    private val onFeedClick: () -> Unit
) : RecyclerView.Adapter<FeedbackQuoteAdapter.QuoteViewHolder>() {

    inner class QuoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val ivProfile: ImageView = view.findViewById(R.id.iv_profile)
        private val tvUsername: TextView = view.findViewById(R.id.tv_username)
        private val tvUserId: TextView = view.findViewById(R.id.tv_userid)
        private val tvContent: TextView = view.findViewById(R.id.tv_content)
        private val tvMore: TextView = view.findViewById(R.id.tv_more)
        private val layoutImageInfo: View = view.findViewById(R.id.layout_image_info)
        private val layoutQuote: ViewGroup = view.findViewById(R.id.layout_quote)

        fun bind(feed: FeedItem) {
            // 프로필
            if (feed.profileImageUrl.isNullOrBlank()) {
                ivProfile.setImageResource(R.drawable.ic_no_profile)
            } else {
                Glide.with(ivProfile)
                    .load(feed.profileImageUrl)
                    .placeholder(R.drawable.ic_no_profile)
                    .error(R.drawable.ic_no_profile)
                    .circleCrop()
                    .into(ivProfile)
            }

            // 이름/아이디
            tvUsername.text = feed.username
            val handle = feed.userHandle.orEmpty()
            if (handle.isBlank()) {
                tvUserId.visibility = View.INVISIBLE
            } else {
                tvUserId.visibility = View.VISIBLE
                tvUserId.text = if (handle.startsWith("@")) handle else "@$handle"
            }

            // 본문
            val isLongText = feed.content.length > 45
            tvContent.text = if (isLongText) feed.content.take(45) + "..." else feed.content
            tvMore.visibility = if (isLongText) View.VISIBLE else View.GONE

            // 이미지 포함 표시
            layoutImageInfo.visibility = if (feed.imageUrls.isNotEmpty()) View.VISIBLE else View.GONE

            // 상세 이동
            itemView.setOnClickListener {
                dialog.dismiss()
                onFeedClick()
                (itemView.context as? FragmentActivity)?.let { fa ->
                    val fragment = FeedDetailFragment.newInstance(
                        postId = feed.id ?: -1L,
                        fallbackFeedItem = feed
                    )
                    fa.supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container_view, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }

            // 인용 카드(= 바텀시트를 띄운 원본 피드)
            layoutQuote.removeAllViews()
            val quoted = feed.quotedFeed
            if (quoted != null) {
                layoutQuote.visibility = View.VISIBLE
                val quoteView = LayoutInflater.from(layoutQuote.context)
                    .inflate(R.layout.item_feed_interacrion_quote, layoutQuote, false)
                layoutQuote.addView(quoteView)

                val tvQuoteContent = quoteView.findViewById<TextView>(R.id.tv_content)
                val tvQuoteMore = quoteView.findViewById<TextView>(R.id.tv_more)
                val tvQuoteUsername = quoteView.findViewById<TextView>(R.id.tv_username)
                val ivQuoteProfile = quoteView.findViewById<ImageView>(R.id.iv_profile)
                val layoutQuoteImageInfo = quoteView.findViewById<View>(R.id.layout_image_info)

                val quoteText = quoted.content
                val isQuoteLong = quoteText.length > 45
                tvQuoteContent.text = if (isQuoteLong) quoteText.take(45) + "..." else quoteText
                tvQuoteMore.visibility = if (isQuoteLong) View.VISIBLE else View.GONE

                tvQuoteUsername.text = quoted.username
                layoutQuoteImageInfo.visibility =
                    if (quoted.imageUrls.isNotEmpty()) View.VISIBLE else View.GONE

                if (quoted.profileImageUrl.isNullOrBlank()) {
                    ivQuoteProfile.setImageResource(R.drawable.ic_no_profile)
                } else {
                    Glide.with(ivQuoteProfile)
                        .load(quoted.profileImageUrl)
                        .placeholder(R.drawable.ic_no_profile)
                        .error(R.drawable.ic_no_profile)
                        .circleCrop()
                        .into(ivQuoteProfile)
                }

                // 인용 카드 터치 시 원본 상세로
                quoteView.setOnClickListener {
                    dialog.dismiss()
                    onFeedClick()
                    (quoteView.context as? FragmentActivity)?.let { fa ->
                        val fragment = FeedDetailFragment.newInstance(
                            postId = quoted.id ?: -1L,
                            fallbackFeedItem = quoted
                        )
                        fa.supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container_view, fragment)
                            .addToBackStack(null)
                            .commit()
                    }
                }
            } else {
                layoutQuote.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feed_interacrion, parent, false)
        return QuoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        holder.bind(quoteFeeds[position])
    }

    override fun getItemCount(): Int = quoteFeeds.size
}