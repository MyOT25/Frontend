package com.example.myot.ticket.book.ui.adapter

import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myot.R
import android.view.ViewGroup
import com.example.myot.databinding.ItemTicketBookBinding
import com.example.myot.ticket.book.model.BookCover

class BookListAdapter (
    private val items: List<BookCover>,
    private val onItemClick: (BookCover) -> Unit
) : RecyclerView.Adapter<BookListAdapter.BookListViewHolder>() {

    inner class BookListViewHolder(private val binding: ItemTicketBookBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(book: BookCover) {
            binding.tvBookTitle.text = book.title
            Glide.with(binding.ivBookCover.context)
                .load(book.poster)
                .placeholder(R.drawable.ig_poster_placeholder)
                .into(binding.ivBookCover)

            binding.ivBookCover.setOnClickListener {
                onItemClick(book)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookListViewHolder {
        val binding = ItemTicketBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookListViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}