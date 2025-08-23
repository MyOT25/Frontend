package com.example.myot.ticket.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myot.R
import com.example.myot.databinding.ItemTodayTicketCardBinding
import com.example.myot.ticket.model.TicketToday

class TicketTodayAdapter (
    private val items: List<TicketToday>
) : RecyclerView.Adapter<TicketTodayAdapter.TicketTodayViewHolder>() {

    inner class TicketTodayViewHolder(private val binding: ItemTodayTicketCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(ticket: TicketToday) {
            binding.tvTodayTitle.text = ticket.title
            binding.tvTodayTheater.text = ticket.theater
            binding.tvTodayPeriod.text = ticket.period
            binding.tvTodayCasting.text = ticket.cast
            binding.tvTodayRatingAvg.text = ticket.avgRating.toString()
            binding.tvTodayMyRating.text = ticket.myRating.toString()
            Glide.with(binding.ivTodayPoster.context)
                .load(ticket.posterUrl)
                .placeholder(R.drawable.ig_poster_placeholder)
                .error(R.drawable.ig_poster_placeholder)
                .into(binding.ivTodayPoster)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketTodayViewHolder {
        val binding = ItemTodayTicketCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TicketTodayViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: TicketTodayViewHolder, position: Int) {
        holder.bind(items[position])
        holder.itemView.translationX = 0f
    }
}