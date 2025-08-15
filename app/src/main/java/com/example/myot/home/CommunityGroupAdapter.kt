package com.example.myot.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.R
import com.example.myot.databinding.ItemCommunityGroupBinding
import kotlin.math.min

class CommunityGroupAdapter : RecyclerView.Adapter<CommunityGroupAdapter.CommunityGroupViewHolder>() {

    var isExpanded: Boolean = false
    private var totalItems: Int = 0

    var onCommunityClick: ((Int) -> Unit)? = null

    fun setTotalItems(count: Int) {
        totalItems = count
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

    inner class CommunityGroupViewHolder(private val binding: ItemCommunityGroupBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(isExpanded: Boolean) {
            val context = binding.root.context
            binding.line1.removeAllViews()
            binding.extraLines.removeAllViews()

            val spanCount = 5
            val totalItems = this@CommunityGroupAdapter.totalItems

            fun createIconView(index: Int): View {
                val iv = LayoutInflater.from(context).inflate(R.layout.item_community, null) as ImageView
                val iconSizePx = (75 * context.resources.displayMetrics.density).toInt()
                iv.layoutParams = LinearLayout.LayoutParams(iconSizePx, iconSizePx).apply {
                    marginStart = 1
                    marginEnd = 1
                }

                if (index == 0) {
                    iv.setImageResource(R.drawable.ic_home_add_community)
                } else {
                    iv.setImageResource(R.drawable.ic_home_no_community)
                    iv.setOnClickListener {
                        onCommunityClick?.invoke(index)
                    }
                }

                return iv
            }

            if (binding.line1.childCount == 0) {
                repeat(totalItems) { i ->
                    binding.line1.addView(createIconView(i))
                }
            }

            if (isExpanded && totalItems > spanCount) {
                binding.scrollLine1.isHorizontalScrollBarEnabled = false
                binding.scrollLine1.setOnTouchListener { _, _ -> true }
                val remainingItems = totalItems - spanCount
                val itemsPerRow = spanCount - 1  // +버튼 자리 제외
                val totalRows = (remainingItems + itemsPerRow - 1) / itemsPerRow

                var index = spanCount
                repeat(totalRows) { rowIndex ->
                    val row = LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            topMargin = (-2 * context.resources.displayMetrics.density).toInt()
                        }
                    }
                    val iv = LayoutInflater.from(context).inflate(R.layout.item_community, null) as ImageView
                    val iconSizePx = (75 * context.resources.displayMetrics.density).toInt()
                    iv.layoutParams = LinearLayout.LayoutParams(iconSizePx, iconSizePx).apply {
                        marginStart = 1
                        marginEnd = 1
                    }
                    iv.setImageResource(R.drawable.ic_home_no_community)
                    iv.visibility = View.INVISIBLE
                    row.addView(iv)

                    repeat(itemsPerRow) {
                        if (index < totalItems) {
                            row.addView(createIconView(index++))
                        }
                    }

                    binding.extraLines.addView(row)
                }

                binding.extraLines.visibility = View.VISIBLE

                binding.scrollLine1.post {
                    binding.scrollLine1.scrollTo(0, 0)
                }

            } else {
                binding.scrollLine1.setOnTouchListener(null)
                binding.scrollLine1.isHorizontalScrollBarEnabled = false
                binding.extraLines.visibility = View.GONE
            }
            binding.scrollLine1.visibility = View.VISIBLE
        }
    }
}