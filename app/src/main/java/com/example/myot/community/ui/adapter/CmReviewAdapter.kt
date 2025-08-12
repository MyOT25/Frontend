package com.example.myot.community.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.databinding.ItemCmReviewBinding
import android.view.View
import com.bumptech.glide.Glide
import com.example.myot.R
import com.example.myot.community.model.ReviewItem
import com.example.myot.community.ui.ReviewDetailDialogFragment

class CmReviewAdapter (
    private val fragment: Fragment,
    private val reviews: List<ReviewItem>
) : RecyclerView.Adapter<CmReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(private val binding: ItemCmReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(review: ReviewItem) {
            binding.tvUserName.text = review.userName
            binding.tvCasting.text = review.casting.joinToString(", ")
            binding.tvSeat.text = review.seat
            binding.tvRating.text = review.rating.toString()
            binding.tvContent.text = review.content
            binding.tvDate.text = review.date
            if (review.isLiked) {
                binding.ivReviewLike.setImageResource(R.drawable.ic_heart_liked)
            } else {
                binding.ivReviewLike.setImageResource(R.drawable.ic_heart_disliked)
            }
            setLikeClickListener(binding, review)
            setViewMore(binding.tvContent, binding.tvMore)
            setImages(binding, review.imageUrls)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemCmReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position])
        holder.itemView.setOnClickListener {
            val dialog = ReviewDetailDialogFragment(reviews[position])
            dialog.show(fragment.parentFragmentManager, "ReviewDetailDialog")
        }
    }

    override fun getItemCount(): Int = reviews.size

    // 이미지 개수 (0~2개) 에 따른 설정
    private fun setImages(binding: ItemCmReviewBinding, imageUrls: List<String>) {
        when (imageUrls.size) {
            0 -> {
                binding.ivRvFirst.visibility = View.GONE
                binding.ivRvSecond.visibility = View.GONE
            }
            1 -> {
                binding.ivRvFirst.visibility = View.VISIBLE
                binding.ivRvSecond.visibility = View.GONE

                Glide.with(binding.root.context)
                    .load(imageUrls[0])
                    .placeholder(R.drawable.ic_review_rv_no_img)
                    .error(R.drawable.ic_review_rv_no_img)
                    .into(binding.ivRvFirst)
            }
            else -> { // 2개 이상일 경우
                binding.ivRvFirst.visibility = View.VISIBLE
                binding.ivRvSecond.visibility = View.VISIBLE

                Glide.with(binding.root.context)
                    .load(imageUrls[0])
                    .placeholder(R.drawable.ic_review_rv_no_img)
                    .error(R.drawable.ic_review_rv_no_img)
                    .into(binding.ivRvFirst)

                Glide.with(binding.root.context)
                    .load(imageUrls[1])
                    .placeholder(R.drawable.ic_review_rv_no_img)
                    .error(R.drawable.ic_review_rv_no_img)
                    .into(binding.ivRvSecond)
            }
        }
    }

    // "좋아요" 버튼 클릭 이벤트
    @SuppressLint("ResourceAsColor", "SetTextI18n")
    private fun setLikeClickListener(binding: ItemCmReviewBinding, review: ReviewItem) {
        binding.ivReviewLike.setOnClickListener {
            review.isLiked = !review.isLiked
            var likesNum = Integer.parseInt(binding.tvReviewLikes.text.toString())

            if (review.isLiked) {
                binding.ivReviewLike.setImageResource(R.drawable.ic_heart_liked)
                binding.tvReviewLikes.text = (likesNum+1).toString()
                binding.tvReviewLikes.setTextColor(R.color.point_pink)
            } else {
                binding.ivReviewLike.setImageResource(R.drawable.ic_heart_disliked)
                binding.tvReviewLikes.text = (likesNum-1).toString()
                binding.tvReviewLikes.setTextColor(R.color.gray2)
            }
        }
    }

    // "더보기" 설정 및 버튼 클릭 이벤트
    private fun setViewMore(contentTv: TextView, viewMoreTv: TextView) {
        contentTv.post {
            val lineCount = contentTv.layout.lineCount
            if (lineCount > 0) {
                if (contentTv.layout.getEllipsisCount(lineCount - 1) > 3) {
                    // 더보기 표시
                    viewMoreTv.visibility = View.VISIBLE

                    // 더보기 클릭 이벤트
                    viewMoreTv.setOnClickListener {
                        contentTv.maxLines = Int.MAX_VALUE
                        viewMoreTv.visibility = View.GONE
                    }
                } else {
                    viewMoreTv.visibility = View.GONE
                }
            }
        }
    }
}