package com.example.myot.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myot.R

class CommunitySuggestionAdapter(
    private val onClick: (CommunityTypeItem) -> Unit
) : RecyclerView.Adapter<CommunitySuggestionAdapter.VH>() {

    private val items = mutableListOf<CommunityTypeItem>()

    fun submit(list: List<CommunityTypeItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        private val ivCover = v.findViewById<ImageView>(R.id.ivCover)
        private val tvName = v.findViewById<TextView>(R.id.tvName)
        private val tvMembers = v.findViewById<TextView>(R.id.tvMembers)

        fun bind(item: CommunityTypeItem) {
            Glide.with(itemView).load(item.coverImage)
                .placeholder(R.drawable.bg_thumbnail_gray5)
                .into(ivCover)

            tvName.text = item.communityName
            tvMembers.text = "${item.memberCount}명 가입"

            itemView.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_community_suggestion, parent, false)
        return VH(v)
    }
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])
    override fun getItemCount(): Int = items.size
}