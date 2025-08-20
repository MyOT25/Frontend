package com.example.myot.feed.adapter

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.R
import com.example.myot.databinding.ItemCommentDetailBinding
import com.example.myot.databinding.ItemCommentReplyBinding
import com.example.myot.feed.model.CommentItem
import com.example.myot.feed.model.FeedItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommentDetailAdapter(
    private val parentComment: CommentItem,
    private val parentFeed: FeedItem,
    private val replies: List<CommentItem> // 대댓글 리스트
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_REPLY = 1
    }

    override fun getItemCount(): Int = 1 + replies.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val binding = ItemCommentDetailBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            HeaderViewHolder(binding)
        } else {
            val binding = ItemCommentReplyBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            ReplyViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.bind(parentFeed, parentComment)
        } else if (holder is ReplyViewHolder) {
            holder.bind(replies[position - 1]) // -1 보정
        }
    }


    inner class HeaderViewHolder(private val binding: ItemCommentDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(feed: FeedItem, comment: CommentItem) {

            binding.tvFeedUserid.text = feed.userHandle
            binding.tvFeedUsername.text = feed.username
            binding.tvFeedContent.text = if (feed.content.length > 31) {
                feed.content.substring(0, 31) + "..."
            } else {
                feed.content
            }

            binding.tvUsername.text = comment.username
            binding.tvDate.text = comment.date
            val loginId = comment.userid
            if (loginId.isBlank()) {
                binding.tvUserid.text = ""
                binding.tvUserid.visibility = View.GONE
            } else {
                binding.tvUserid.text = if (loginId.startsWith("@")) loginId else "@$loginId"
                binding.tvUserid.visibility = View.VISIBLE
            }
            binding.tvContent.text = styleMentionText(comment.content, binding.root.context)

            binding.tvComment.text = comment.commentCount.toString()
            binding.tvLike.text = comment.likeCount.toString()
            binding.tvRepost.text = comment.repostCount.toString()
            binding.tvQuote.text = comment.quoteCount.toString()

            // 추후 답글이 존재하면 안 보이게 해야함
            binding.tvNoReply.visibility = View.VISIBLE

            updateColor(binding.tvLike, binding.ivLike, comment.isLiked, R.color.point_pink)
            updateColor(binding.tvRepost, binding.ivRepost, comment.isReposted, R.color.point_blue)
            updateColor(binding.tvQuote, binding.ivQuote, comment.isQuoted, R.color.point_purple)

            binding.ivBack.setOnClickListener {
                val activity = it.context as? FragmentActivity
                activity?.supportFragmentManager?.popBackStack()
            }
        }

        private fun updateColor(tv: TextView, iv: ImageView, active: Boolean, colorRes: Int) {
            val color = tv.context.getColor(if (active) colorRes else R.color.gray3)
            tv.setTextColor(color)
            iv.setColorFilter(color)
        }

        private fun styleMentionText(text: String, context: Context): SpannableString {
            val spannable = SpannableString(text)
            val regex = Regex("@\\w+")

            regex.findAll(text).forEach { match ->
                val start = match.range.first
                val end = match.range.last + 1

                spannable.setSpan(
                    ForegroundColorSpan(context.getColor(R.color.point_purple)),
                    start, end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    start, end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            return spannable
        }
    }

    inner class ReplyViewHolder(private val binding: ItemCommentReplyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reply: CommentItem) {
            binding.tvUsername.text = reply.username
            val loginId = reply.userid
            if (loginId.isBlank()) {
                binding.tvUserid.text = ""
                binding.tvUserid.visibility = View.GONE
            } else {
                binding.tvUserid.text = if (loginId.startsWith("@")) loginId else "@$loginId"
                binding.tvUserid.visibility = View.VISIBLE
            }
            binding.tvDate.text = reply.date
            binding.tvContent.text = reply.content
        }
    }

}