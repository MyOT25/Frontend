package com.example.myot

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class FeedbackQuoteAdapter(private val quoteFeeds: List<FeedItem>) :
    RecyclerView.Adapter<FeedbackQuoteAdapter.QuoteViewHolder>() {

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
            tvMore.setOnClickListener {
                tvContent.text = feed.content
                tvMore.visibility = View.GONE
            }

            // 기존 뷰 제거 (재활용 방지)
            layoutQuote.removeAllViews()

            // 인용 피드용 레이아웃 선택
            val inflater = LayoutInflater.from(layoutQuote.context)
            val quoteLayoutResId = when (feed.imageUrls.size) {
                0 -> R.layout.item_feed_quote_text_only
                1 -> R.layout.item_feed_quote_image1
                2 -> R.layout.item_feed_quote_image2
                3 -> R.layout.item_feed_quote_image3
                4 -> R.layout.item_feed_quote_image4
                else -> R.layout.item_feed_quote_text_only
            }
            val quoteView = inflater.inflate(quoteLayoutResId, layoutQuote, false)
            layoutQuote.addView(quoteView)

            // 인용 텍스트 바인딩
            val quoteContent = quoteView.findViewById<TextView>(R.id.tv_content)
            val quoteMore = quoteView.findViewById<TextView>(R.id.tv_more)
            val quoteText = feed.content
            val isQuoteLong = quoteText.length > 160

            quoteContent?.text = if (isQuoteLong) quoteText.take(160) + "..." else quoteText
            quoteMore?.visibility = if (isQuoteLong) View.VISIBLE else View.GONE
            quoteMore?.setOnClickListener {
                quoteContent?.text = quoteText
                quoteMore?.visibility = View.GONE
            }

            // 인용 이미지 바인딩
            feed.imageUrls.forEachIndexed { index, url ->
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
