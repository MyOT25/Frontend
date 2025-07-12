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
        private val layoutImageInfo: View = view.findViewById(R.id.layout_image_info)
        private val layoutQuote: ViewGroup = view.findViewById(R.id.layout_quote)

        fun bind(feed: FeedItem) {
            tvUsername.text = feed.username

            // 본문 텍스트 처리
            val isLongText = feed.content.length > 45
            tvContent.text = if (isLongText) feed.content.take(45) + "..." else feed.content
            tvMore.visibility = if (isLongText) View.VISIBLE else View.GONE

            // 이미지 포함 여부 표시
            layoutImageInfo.visibility = if (feed.imageUrls.isNotEmpty()) View.VISIBLE else View.GONE

            // 피드 클릭 시 상세 화면으로 이동
            itemView.setOnClickListener {
                dialog.dismiss()
                val context = itemView.context
                if (context is androidx.fragment.app.FragmentActivity) {
                    val fragment = FeedDetailFragment.newInstance(feed)
                    context.supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container_view, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }

            // 인용 피드 처리
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
                layoutQuoteImageInfo.visibility = if (quoted.imageUrls.isNotEmpty()) View.VISIBLE else View.GONE

                Glide.with(ivQuoteProfile).load(R.drawable.ic_no_profile).into(ivQuoteProfile)

                quoteView.setOnClickListener {
                    dialog.dismiss()
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
            .inflate(R.layout.item_feed_interacrion, parent, false)
        return QuoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        holder.bind(quoteFeeds[position])
    }

    override fun getItemCount(): Int = quoteFeeds.size
}