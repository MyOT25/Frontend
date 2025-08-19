package com.example.myot.ticket.book.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.databinding.ItemTbRoleBinding
import com.example.myot.ticket.book.model.RoleWithActors
import com.example.myot.ticket.book.model.TBCasting

class TBRoleAdapter(
    private val roles: List<RoleWithActors>,
    private val onScrollProgressChanged: (position: Int, progress: Float) -> Unit
) : RecyclerView.Adapter<TBRoleAdapter.TBRoleViewHolder>() {

    inner class TBRoleViewHolder(private val binding: ItemTbRoleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RoleWithActors, position: Int) {
            binding.tvRoleName.text = item.role

            binding.rvActorList.apply {
                layoutManager = LinearLayoutManager(
                    itemView.context, LinearLayoutManager.HORIZONTAL, false
                )
                adapter = TBActorAdapter(item.actors)
                clearOnScrollListeners()
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)

                        val offset = recyclerView.computeHorizontalScrollOffset()
                        val extent = recyclerView.computeHorizontalScrollExtent()
                        val range = recyclerView.computeHorizontalScrollRange()

                        val progress = if (range > extent) {
                            offset.toFloat() / (range - extent)
                        } else 0f

                        onScrollProgressChanged(position, progress)
                    }
                })
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TBRoleViewHolder {
        val binding =
            ItemTbRoleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TBRoleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TBRoleViewHolder, position: Int) {
        holder.bind(roles[position], position)
    }

    override fun getItemCount(): Int = roles.size
}
