package com.example.myot

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContentAdapter(
    private var items: List<Section>,
    private val onToggle: (Section) -> Unit
) : RecyclerView.Adapter<ContentAdapter.ContentViewHolder>() {

    inner class ContentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.sectionTitle)
        val contentText: TextView = itemView.findViewById(R.id.sectionContent)
        val toggleIcon: ImageView = itemView.findViewById(R.id.arrowIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_section_content, parent, false)
        return ContentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
        val section = items[position]
        holder.titleText.text = section.title
        holder.contentText.text = section.content

        // 들여쓰기
        val params = holder.titleText.layoutParams as ViewGroup.MarginLayoutParams
        params.marginStart = section.depth * 24
        holder.titleText.layoutParams = params

        // 화살표 방향 설정
        // 하나의 화살표 이미지를 회전시켜 방향 변경
        holder.toggleIcon.setImageResource(R.drawable.ic_home_extend)
        holder.toggleIcon.rotation = if (section.isExpanded) 0f else -90f //반시계 방향으로 90도 회전

        holder.toggleIcon.setOnClickListener {
            onToggle(section)
        }

        // 내용 보이기/숨기기
        holder.contentText.visibility = if (section.isExpanded) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<Section>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun getCurrentList(): List<Section> = items
}