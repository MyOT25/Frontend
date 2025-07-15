package com.example.myot.question

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myot.databinding.ItemQuestionImagePagerBinding

class QuestionImagePagerAdapter(private val imageUrls: List<String>) :
    RecyclerView.Adapter<QuestionImagePagerAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val view: ImageView) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val imageView = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        return ImageViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        Glide.with(holder.view.context)
            .load(imageUrls[position])
            .into(holder.view)
    }

    override fun getItemCount(): Int = imageUrls.size
}