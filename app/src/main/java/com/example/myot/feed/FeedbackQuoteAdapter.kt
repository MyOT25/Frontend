package com.example.myot.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myot.R
import com.google.android.material.bottomsheet.BottomSheetDialog

class FeedbackQuoteAdapter(
    private val quoteFeeds: List<FeedItem>,
    private val dialog: BottomSheetDialog
) : RecyclerView.Adapter<FeedbackQuoteAdapter.QuoteViewHolder>() {

    inner class QuoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvUsername: TextView = view.findViewById(R.id.tv_username)
        private val tvContent: TextView = view.findViewById(R.id.tv_content)
        private val tvMore: TextView = view.findViewById(R.id.tv_more)
        private val layoutQuote: ViewGroup = view.findViewById(R.id.layout_quote)

        fun bind(feed: FeedItem) {
            tvUsername.text = feed.username

            // 본문 텍스트 처리
            val isLongText = feed.content.length > 160
            tvContent.text = if (isLongText) feed.content.take(160) + "..." else feed.content
            tvMore.visibility = if (isLongText) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                dialog.dismiss()  // 바텀시트 닫기
                val context = itemView.context
                if (context is androidx.fragment.app.FragmentActivity) {
                    val fragment = FeedDetailFragment.newInstance(feed)
                    context.supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container_view, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }

            // --- 인용 피드 처리 시작 ---
            layoutQuote.removeAllViews()

            val quoted = feed.quotedFeed
            if (quoted != null) {
                layoutQuote.visibility = View.VISIBLE

                val inflater = LayoutInflater.from(layoutQuote.context)
                val quoteLayoutResId = when (quoted.imageUrls.size) {
                    0 -> R.layout.item_feed_quote_text_only
                    1 -> R.layout.item_feed_quote_image1
                    2 -> R.layout.item_feed_quote_image2
                    3 -> R.layout.item_feed_quote_image3
                    else -> R.layout.item_feed_quote_image4
                }

                val quoteView = inflater.inflate(quoteLayoutResId, layoutQuote, false)
                layoutQuote.addView(quoteView)

                // 텍스트 바인딩
                val tvQuoteContent = quoteView.findViewById<TextView>(R.id.tv_content)
                val tvQuoteMore = quoteView.findViewById<TextView>(R.id.tv_more)

                val quoteText = quoted.content
                val isQuoteLong = when {
                    quoted.imageUrls.isNotEmpty() -> quoteText.length > 50
                    else -> quoteText.length > 105
                }

                tvQuoteContent?.text = if (isQuoteLong) quoteText.take(if (quoted.imageUrls.isNotEmpty()) 50 else 105) + "..." else quoteText
                tvQuoteMore?.visibility = if (isQuoteLong) View.VISIBLE else View.GONE

                // 이미지 바인딩
                quoted.imageUrls.forEachIndexed { index, url ->
                    val imageId = when (index) {
                        0 -> R.id.iv_image1
                        1 -> R.id.iv_image2
                        2 -> R.id.iv_image3
                        3 -> R.id.iv_image4
                        else -> null
                    }
                    imageId?.let {
                        val iv = quoteView.findViewById<ImageView>(it)
                        iv?.visibility = View.VISIBLE
                        Glide.with(iv).load(url).into(iv)
                    }
                }

                // 유저 정보
                val tvQuoteUsername = quoteView.findViewById<TextView>(R.id.tv_username)
                val ivQuoteProfile = quoteView.findViewById<ImageView>(R.id.iv_profile)
                val ivQuoteCommunity = quoteView.findViewById<ImageView>(R.id.iv_community)

                tvQuoteUsername?.text = quoted.username
                Glide.with(ivQuoteCommunity).load(R.drawable.ic_no_community).into(ivQuoteCommunity)
                Glide.with(ivQuoteProfile).load(R.drawable.ic_no_profile).into(ivQuoteProfile)

                // 인용된 피드 선택
                quoteView.setOnClickListener {
                    dialog.dismiss()  // 바텀시트 닫기
                    val context = quoteView.context
                    if (context is androidx.fragment.app.FragmentActivity) {
                        val fragment = FeedDetailFragment.newInstance(quoted)
                        context.supportFragmentManager.beginTransaction()
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
            .inflate(R.layout.item_feedback_text_only, parent, false)
        return QuoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        holder.bind(quoteFeeds[position])
    }

    override fun getItemCount(): Int = quoteFeeds.size
}
