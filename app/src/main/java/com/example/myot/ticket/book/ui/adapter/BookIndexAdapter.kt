package com.example.myot.ticket.book.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myot.R
import com.example.myot.databinding.ItemTicketBookIndexBinding
import com.example.myot.ticket.book.model.BookIndex

class BookIndexAdapter (
    private val items: List<BookIndex>,
    private val onItemClick: (BookIndex) -> Unit
) : RecyclerView.Adapter<BookIndexAdapter.BookIndexViewHolder>() {

    inner class BookIndexViewHolder(private val binding: ItemTicketBookIndexBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(season: BookIndex) {
            binding.tvIndexSeason.text = season.season
            Glide.with(binding.ivIndexPoster.context)
                .load(season.posterUrl)
                .placeholder(R.drawable.ig_poster_placeholder)
                .into(binding.ivIndexPoster)

            binding.ivIndexPoster.setOnClickListener {
                onItemClick(season)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookIndexViewHolder {
        val binding = ItemTicketBookIndexBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookIndexViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookIndexViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}