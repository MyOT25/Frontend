package com.example.myot

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TocAdapter(
    private var items: List<Section>,
    private val onClick: (Section) -> Unit
) : RecyclerView.Adapter<TocAdapter.TocViewHolder>() {

    inner class TocViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.tocTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TocViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_toc, parent, false)
        return TocViewHolder(view)
    }

    override fun onBindViewHolder(holder: TocViewHolder, position: Int) {
        val section = items[position]
        holder.titleText.text = section.title

        // 들여쓰기 (depth에 따라 마진 적용)
        val params = holder.titleText.layoutParams as ViewGroup.MarginLayoutParams
        params.marginStart = section.depth * 24
        holder.titleText.layoutParams = params

        holder.itemView.setOnClickListener {
            onClick(section)
        }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<Section>) {
        items = newItems
        notifyDataSetChanged()
    }
}