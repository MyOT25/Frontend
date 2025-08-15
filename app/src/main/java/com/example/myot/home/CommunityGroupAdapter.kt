package com.example.myot.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myot.R
import com.example.myot.databinding.ItemCommunityGroupBinding
import kotlin.math.min

class CommunityGroupAdapter : RecyclerView.Adapter<CommunityGroupAdapter.CommunityGroupViewHolder>() {

    var isExpanded: Boolean = false

    // UI용 데이터 (커버 포함)
    private val items = mutableListOf<CommunityUi>()

    var onCommunityClick: ((index: Int, item: CommunityUi?) -> Unit)? = null

    fun setItems(list: List<CommunityUi>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityGroupViewHolder {
        val binding = ItemCommunityGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommunityGroupViewHolder(binding)
    }

    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(holder: CommunityGroupViewHolder, position: Int) {
        holder.bind(isExpanded)
    }

    inner class CommunityGroupViewHolder(private val binding: ItemCommunityGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val spanCount = 5

        private fun typeToIconRes(type: String): Int = when (type.lowercase()) {
            "musical" -> R.drawable.ic_home_no_community
            "actor"   -> R.drawable.ic_home_no_community
            else      -> R.drawable.ic_home_no_community
        }

        fun bind(isExpanded: Boolean) {
            val context = binding.root.context
            binding.line1.removeAllViews()
            binding.extraLines.removeAllViews()

            // total = + 버튼(0) + 실제 커뮤니티들
            val totalItems = 1 + items.size

            fun createIconView(index: Int): View {
                val iv = LayoutInflater.from(context).inflate(R.layout.item_community, null) as ImageView
                val iconSizePx = (75 * context.resources.displayMetrics.density).toInt()
                iv.layoutParams = LinearLayout.LayoutParams(iconSizePx, iconSizePx).apply {
                    marginStart = 1
                    marginEnd = 1
                }

                if (index == 0) {
                    iv.setImageResource(R.drawable.ic_home_add_community)
                    iv.setOnClickListener { onCommunityClick?.invoke(0, null) }
                } else {
                    val item = items[index - 1]
                    if (!item.coverImage.isNullOrBlank()) {
                        Glide.with(iv).load(item.coverImage)
                            .placeholder(typeToIconRes(item.type))
                            .error(typeToIconRes(item.type))
                            .centerCrop()
                            .into(iv)
                    } else {
                        iv.setImageResource(typeToIconRes(item.type))
                    }
                    iv.contentDescription = item.name
                    iv.setOnClickListener { onCommunityClick?.invoke(index, item) }
                }
                return iv
            }

            val countForLine1 = if (isExpanded) min(totalItems, spanCount) else totalItems
            repeat(countForLine1) { i -> binding.line1.addView(createIconView(i)) }

            if (isExpanded && totalItems > spanCount) {
                binding.scrollLine1.isHorizontalScrollBarEnabled = false
                binding.scrollLine1.setOnTouchListener { _, _ -> true }

                val remaining = totalItems - spanCount
                val itemsPerRow = spanCount - 1 // +버튼 자리 비움
                val rows = (remaining + itemsPerRow - 1) / itemsPerRow

                var idx = spanCount
                repeat(rows) {
                    val row = LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            topMargin = (-2 * context.resources.displayMetrics.density).toInt()
                        }
                    }
                    val dummy = LayoutInflater.from(context).inflate(R.layout.item_community, null) as ImageView
                    val iconSizePx = (75 * context.resources.displayMetrics.density).toInt()
                    dummy.layoutParams = LinearLayout.LayoutParams(iconSizePx, iconSizePx).apply {
                        marginStart = 1
                        marginEnd = 1
                    }
                    dummy.setImageResource(R.drawable.ic_home_add_community)
                    dummy.visibility = View.INVISIBLE
                    row.addView(dummy)

                    repeat(itemsPerRow) {
                        if (idx < totalItems) row.addView(createIconView(idx++))
                    }
                    binding.extraLines.addView(row)
                }

                binding.extraLines.visibility = View.VISIBLE
                binding.scrollLine1.post { binding.scrollLine1.scrollTo(0, 0) }
            } else {
                binding.scrollLine1.setOnTouchListener(null)
                binding.scrollLine1.isHorizontalScrollBarEnabled = false
                binding.extraLines.visibility = View.GONE
            }
            binding.scrollLine1.visibility = View.VISIBLE
        }
    }
}