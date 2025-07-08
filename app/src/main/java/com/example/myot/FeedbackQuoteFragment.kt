package com.example.myot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class FeedbackQuoteFragment : Fragment() {

    private lateinit var quoteFeeds: List<FeedItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        quoteFeeds = requireArguments().getParcelableArrayList(ARG_QUOTES)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.item_feedback_text_only, container, false)

        // 여기선 예제로 첫 번째 인용 피드만 표시
        val feedItem = quoteFeeds[0]

        val tvUsername = view.findViewById<TextView>(R.id.tv_username)
        val tvContent = view.findViewById<TextView>(R.id.tv_content)
        val layoutQuote = view.findViewById<FrameLayout>(R.id.layout_quote)

        tvUsername.text = feedItem.username
        tvContent.text = feedItem.content

        if (feedItem.quotedFeed != null) {
            layoutQuote.visibility = View.VISIBLE
            layoutQuote.removeAllViews()

            val quoted = feedItem.quotedFeed!!
            val quoteInflater = LayoutInflater.from(requireContext())
            val quoteView = when (quoted.imageUrls.size) {
                0 -> quoteInflater.inflate(R.layout.item_feed_quote_text_only, layoutQuote, false)
                1 -> quoteInflater.inflate(R.layout.item_feed_quote_image1, layoutQuote, false)
                2 -> quoteInflater.inflate(R.layout.item_feed_quote_image2, layoutQuote, false)
                3 -> quoteInflater.inflate(R.layout.item_feed_quote_image3, layoutQuote, false)
                else -> quoteInflater.inflate(R.layout.item_feed_quote_image4, layoutQuote, false)
            }
            layoutQuote.addView(quoteView)

            val tvQuoteContent = quoteView.findViewById<TextView>(R.id.tv_content)
            tvQuoteContent?.text = quoted.content
        } else {
            layoutQuote.visibility = View.GONE
        }

        return view
    }

    companion object {
        private const val ARG_QUOTES = "arg_quotes"

        fun newInstance(quotes: List<FeedItem>): FeedbackQuoteFragment {
            val fragment = FeedbackQuoteFragment()
            val bundle = Bundle().apply {
                putParcelableArrayList(ARG_QUOTES, ArrayList(quotes))
            }
            fragment.arguments = bundle
            return fragment
        }
    }
}
