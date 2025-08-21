package com.example.myot.ticket.book.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myot.R
import com.example.myot.databinding.ItemTicketBookIndexBinding
import com.example.myot.ticket.book.model.BookIndexData
import com.example.myot.ticket.book.model.BookSeries

class BookIndexAdapter (
    private val lifecycleOwner: LifecycleOwner,
    private val booksIndexLiveData: LiveData<BookIndexData>,
    private val onItemClick: (BookSeries) -> Unit
) : RecyclerView.Adapter<BookIndexAdapter.BookIndexViewHolder>() {

    private var seasons: List<BookSeries> = emptyList()

    init {
        // LiveData를 관찰하여 데이터가 변경될 때마다 어댑터를 업데이트
        booksIndexLiveData.observe(lifecycleOwner, Observer { bookSeries ->
            seasons = bookSeries.series ?: emptyList()
            notifyDataSetChanged()
        })
    }

    inner class BookIndexViewHolder(private val binding: ItemTicketBookIndexBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(season: BookSeries) {
            binding.tvIndexSeason.text = season.label
            Glide.with(binding.ivIndexPoster.context)
                .load(season.poster)
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
        holder.bind(seasons[position])
    }

    override fun getItemCount(): Int = seasons.size

    fun cleanup() {
        booksIndexLiveData.removeObservers(lifecycleOwner)
    }
}