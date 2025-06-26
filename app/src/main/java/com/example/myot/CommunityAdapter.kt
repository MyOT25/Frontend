package com.example.myot

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.databinding.ItemCommunityBinding

// 커뮤니티 항목 데이터 클래스
data class CommunityItem(
    val iconRes: Int,
    val isAddButton: Boolean,
    val isPlaceholder: Boolean = false    // 기본값 false
)

class CommunityAdapter(
    private val items: List<CommunityItem>,
    private val onClick: (CommunityItem) -> Unit
) : RecyclerView.Adapter<CommunityAdapter.VH>() {

    inner class VH(val binding: ItemCommunityBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CommunityItem) {
            if (item.isPlaceholder) {
                binding.ivIcon.visibility = View.INVISIBLE
                binding.root.isClickable = false
                return
            }

            binding.ivIcon.visibility = View.VISIBLE
            val res = if (item.isAddButton)
                R.drawable.ic_home_add_community
            else
                item.iconRes

            binding.ivIcon.setImageResource(res)
            binding.root.isClickable = true
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemCommunityBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(b)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(items[position])
}
