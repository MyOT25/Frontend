package com.example.myot.feed.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.R

class FeedbackAdapter(private val users: List<String>) :
    RecyclerView.Adapter<FeedbackAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUsername = itemView.findViewById<TextView>(R.id.tv_username)
        val tvUserid = itemView.findViewById<TextView>(R.id.tv_userid)
        val tvDescription = itemView.findViewById<TextView>(R.id.tv_description)
        val btnFollow = itemView.findViewById<Button>(R.id.btn_follow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feedback, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val name = users[position]
        holder.tvUsername.text = "$name"
        holder.tvUserid.text = "@$name"
        holder.tvDescription.text = "설명설명설명설명설명설명"

        // 예시: 팔로우 버튼 토글
        var isFollowing = false
        holder.btnFollow.setOnClickListener {
            isFollowing = !isFollowing
            holder.btnFollow.text = if (isFollowing) "팔로잉" else "팔로우"
            holder.btnFollow.isSelected = isFollowing
        }
    }
}