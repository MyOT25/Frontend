package com.example.myot.ticket.book.ui.adapter

import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myot.R
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.example.myot.databinding.ItemTicketBookBinding
import com.example.myot.ticket.book.model.BookCover

class BookListAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val booksLiveData: LiveData<List<BookCover>>,
    private val onItemClick: (BookCover) -> Unit
) : RecyclerView.Adapter<BookListAdapter.BookListViewHolder>() {

    private var books: List<BookCover> = emptyList()

    init {
        // LiveData를 관찰하여 데이터가 변경될 때마다 어댑터를 업데이트
        booksLiveData.observe(lifecycleOwner, Observer { bookList ->
            books = bookList ?: emptyList()
            notifyDataSetChanged()
        })
    }

    inner class BookListViewHolder(private val binding: ItemTicketBookBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(book: BookCover) {
            binding.tvBookTitle.text = book.title
            Glide.with(binding.ivBookCover.context)
                .load(book.poster)
                .centerCrop()
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
        holder.bind(books[position])
    }

    override fun getItemCount(): Int = books.size

    fun cleanup() {
        booksLiveData.removeObservers(lifecycleOwner)
    }
}