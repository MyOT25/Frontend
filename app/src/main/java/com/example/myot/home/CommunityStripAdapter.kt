package com.example.myot.home

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myot.R


class CommunityStripAdapter(
    private val onItemClick: (CommunityUi?) -> Unit
) : RecyclerView.Adapter<CommunityStripAdapter.VH>() {

    private val items = mutableListOf<CommunityUi?>()

    fun submit(list: List<CommunityUi>) {
        items.clear()
        items.add(null)
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val iv = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_community, parent, false) as ImageView
        return VH(iv)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    inner class VH(private val iv: ImageView) : RecyclerView.ViewHolder(iv) {
        fun bind(item: CommunityUi?) {
            if (item == null) {
                iv.setImageResource(R.drawable.ic_home_add_community)
                iv.setOnClickListener { onItemClick(null) }
                return
            }

            val fallback = typeToIconRes(item.type)
            if (!item.coverImage.isNullOrBlank()) {
                Glide.with(iv)
                    .load(item.coverImage)
                    .placeholder(fallback)
                    .error(fallback)
                    .centerCrop()
                    .into(iv)
            } else {
                iv.setImageResource(fallback)
            }
            iv.contentDescription = item.name
            iv.setOnClickListener { onItemClick(item) }
        }
    }

    private fun typeToIconRes(type: String): Int = when (type.lowercase()) {
        else      -> R.drawable.ic_home_no_community
    }
}