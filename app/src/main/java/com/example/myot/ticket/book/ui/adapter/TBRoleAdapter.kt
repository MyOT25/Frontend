package com.example.myot.ticket.book.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.databinding.ItemTbRoleBinding
import com.example.myot.ticket.book.model.TBRole

class TBRoleAdapter(
    private val roles: List<TBRole>,
    private val onScrollProgressChanged: (position: Int, progress: Float) -> Unit
) : RecyclerView.Adapter<TBRoleAdapter.TBRoleViewHolder>() {

    inner class TBRoleViewHolder(private val binding: ItemTbRoleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(role: TBRole, position: Int) {
            binding.tvRoleName.text = role.roleName

            binding.rvActorList.apply {
                layoutManager =
                    LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
                adapter = TBActorAdapter(role.actors)
                clearOnScrollListeners()
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)

                        val offset = recyclerView.computeHorizontalScrollOffset()
                        val extent = recyclerView.computeHorizontalScrollExtent()
                        val range = recyclerView.computeHorizontalScrollRange()

                        val progress = offset.toFloat() / (range - extent)

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
