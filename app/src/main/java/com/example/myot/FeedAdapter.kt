package com.example.myot

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.myot.databinding.ItemFeedImage1Binding
import com.example.myot.databinding.ItemFeedImage2Binding
import com.example.myot.databinding.ItemFeedImage3Binding
import com.example.myot.databinding.ItemFeedImage4Binding
import com.example.myot.databinding.ItemFeedTextOnlyBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class FeedAdapter(private val items: List<FeedItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_TEXT_ONLY = 0
        private const val TYPE_IMAGE_1 = 1
        private const val TYPE_IMAGE_2 = 2
        private const val TYPE_IMAGE_3 = 3
        private const val TYPE_IMAGE_4 = 4
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]
        return when (item.imageUrls.size) {
            0 -> TYPE_TEXT_ONLY
            1 -> TYPE_IMAGE_1
            2 -> TYPE_IMAGE_2
            3 -> TYPE_IMAGE_3
            else -> TYPE_IMAGE_4
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_TEXT_ONLY -> TextOnlyViewHolder(ItemFeedTextOnlyBinding.inflate(inflater, parent, false))
            TYPE_IMAGE_1 -> ImageViewHolder(ItemFeedImage1Binding.inflate(inflater, parent, false))
            TYPE_IMAGE_2 -> ImageViewHolder(ItemFeedImage2Binding.inflate(inflater, parent, false))
            TYPE_IMAGE_3 -> ImageViewHolder(ItemFeedImage3Binding.inflate(inflater, parent, false))
            TYPE_IMAGE_4 -> ImageViewHolder(ItemFeedImage4Binding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is TextOnlyViewHolder -> holder.bind(item)
            is ImageViewHolder -> holder.bind(item)
        }
    }

    override fun getItemCount(): Int = items.size
}
