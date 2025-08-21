package com.example.myot.ticket.book.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myot.R
import com.example.myot.databinding.ItemTbActorBinding
import com.example.myot.ticket.book.model.TBCasting

class TBActorAdapter(private val actors: List<TBCasting>) :
    RecyclerView.Adapter<TBActorAdapter.TBActorViewHolder>() {

    inner class TBActorViewHolder(private val binding: ItemTbActorBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(actor: TBCasting) {
            binding.tvActorName.text = actor.actor.name
            binding.tvActorCount.text = "${actor.myCount} / ${actor.performanceCount}"
            Glide.with(binding.ivActor.context)
                .load(actor.actor.image)
                .circleCrop()
                .error(R.drawable.ic_profile)
                .placeholder(R.drawable.ic_profile)
                .into(binding.ivActor)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TBActorViewHolder {
        val binding = ItemTbActorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TBActorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TBActorViewHolder, position: Int) {
        holder.bind(actors[position])
    }

    override fun getItemCount(): Int = actors.size
}