package com.example.myot

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.databinding.ItemFilteringCheckboxBinding

class ReviewFilterAdapter(private val items: List<ReviewFilterItem>) : RecyclerView.Adapter<ReviewFilterAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemFilteringCheckboxBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemFilteringCheckboxBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvName.text = item.role

        // 체크박스들 동적으로 추가
        holder.binding.checkboxContainer.removeAllViews()
        item.castings.forEach { label ->
            val checkBox = CheckBox(holder.itemView.context).apply {
                text = label
            }
            holder.binding.checkboxContainer.addView(checkBox)
        }
    }

    override fun getItemCount() = items.size
}