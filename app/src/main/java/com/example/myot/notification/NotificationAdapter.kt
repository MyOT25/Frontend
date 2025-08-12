package com.example.myot.notification

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.R
import com.example.myot.databinding.ItemNotificationBinding

class NotificationAdapter(
    private val items: MutableList<NotificationItem>,
    private val onItemClick: (position: Int) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NotificationItem) = with(binding) {
            tvMessage.text = item.message

            viewDot.visibility = if (item.isNew) View.VISIBLE else View.GONE

            root.setBackgroundResource(
                if (item.isNew) R.drawable.bg_notification_new
                else android.R.color.transparent
            )

            root.setOnClickListener {
                val pos = absoluteAdapterPosition
                if (pos != RecyclerView.NO_POSITION && item.isNew) {
                    item.isNew = false
                    notifyItemChanged(pos)
                    onItemClick(pos)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    // 필요하면 외부에서 갱신할 때 쓰는 헬퍼
    fun notifyItemRead(position: Int) {
        if (position in items.indices) notifyItemChanged(position)
    }
}