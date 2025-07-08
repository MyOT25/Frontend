package com.example.myot

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.myot.databinding.DialogReviewDetailBinding

class ReviewDetailDialogFragment(
    private val review: ReviewItem
) : DialogFragment() {

    private var _binding: DialogReviewDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        _binding = DialogReviewDetailBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCanceledOnTouchOutside(true)

        bindReviewData()

        return dialog
    }

    @SuppressLint("SetTextI18n")
    private fun bindReviewData() {
        binding.tvUserName.text = review.userName
        binding.tvRating.text = review.rating.toString()
        binding.tvTheater.text = "극장: " + review.theater
        binding.tvSeat.text = "자리: " + review.seat
        val castingTmp = "배우: " + review.casting.joinToString(", ")
        binding.tvCasting.text = castingTmp
        binding.tvShowDate.text = "회차: " + review.showDate
        binding.tvRating.text = review.rating.toString()
        binding.tvContent.text = review.content
        binding.tvDate.text = review.date

        setImages(binding, review.imageUrls)

        if (review.isLiked) {
            binding.ivReviewLike.setImageResource(R.drawable.ic_heart_liked)
        } else {
            binding.ivReviewLike.setImageResource(R.drawable.ic_heart_disliked)
        }

        setLikeClickListener(binding, review)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 이미지 개수 (0~2개) 에 따른 설정
    private fun setImages(binding: DialogReviewDetailBinding, imageUrls: List<String>) {
        when (imageUrls.size) {
            0 -> {
                binding.ivDialogFirst.visibility = View.GONE
                binding.ivDialogSecond.visibility = View.GONE
            }
            1 -> {
                binding.ivDialogFirst.visibility = View.VISIBLE
                binding.ivDialogSecond.visibility = View.GONE

                Glide.with(binding.root.context)
                    .load(imageUrls[0])
                    .placeholder(R.drawable.ic_review_dialog_no_img)
                    .error(R.drawable.ic_review_dialog_no_img)
                    .into(binding.ivDialogFirst)
            }
            else -> { // 2개 이상일 경우
                binding.ivDialogFirst.visibility = View.VISIBLE
                binding.ivDialogSecond.visibility = View.VISIBLE

                Glide.with(binding.root.context)
                    .load(imageUrls[0])
                    .placeholder(R.drawable.ic_review_dialog_no_img)
                    .error(R.drawable.ic_review_dialog_no_img)
                    .into(binding.ivDialogFirst)

                Glide.with(binding.root.context)
                    .load(imageUrls[1])
                    .placeholder(R.drawable.ic_review_dialog_no_img)
                    .error(R.drawable.ic_review_dialog_no_img)
                    .into(binding.ivDialogSecond)
            }
        }
    }

    // "좋아요" 버튼 클릭 이벤트
    @SuppressLint("ResourceAsColor", "SetTextI18n")
    private fun setLikeClickListener(binding: DialogReviewDetailBinding, review: ReviewItem) {
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
}