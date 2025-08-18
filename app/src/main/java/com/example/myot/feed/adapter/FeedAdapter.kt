package com.example.myot.feed.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.databinding.ItemFeedBinding
import com.example.myot.feed.model.FeedItem

class FeedAdapter(
    private val items: List<FeedItem>,
    private val onDeleteRequest: (Long) -> Unit = {},
    private val onItemClick: ((FeedItem) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemFeedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FeedViewHolder(binding, onItemClick ?: {}, onDeleteRequest)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        val isLastItem = position == itemCount - 1
        (holder as FeedViewHolder).bind(item, isLastItem)
    }

    override fun getItemCount(): Int = items.size
}