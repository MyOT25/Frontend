package com.example.myot.feed.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myot.R
import com.example.myot.feed.model.FeedbackUserUi

class FeedbackAdapter(private var users: List<FeedbackUserUi>) :
    RecyclerView.Adapter<FeedbackAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProfile = itemView.findViewById<ImageView?>(R.id.iv_profile)
        val tvUsername = itemView.findViewById<TextView>(R.id.tv_username)
        val tvUserid = itemView.findViewById<TextView>(R.id.tv_userid)
        val tvDescription = itemView.findViewById<TextView>(R.id.tv_description)
        val btnFollow = itemView.findViewById<TextView>(R.id.tv_follow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feedback, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val u = users[position]

        holder.tvUsername.text = u.nickname

        val id = u.loginId?.trim().orEmpty()
        if (id.isBlank()) {
            holder.tvUserid.visibility = View.INVISIBLE
        } else {
            holder.tvUserid.visibility = View.VISIBLE
            holder.tvUserid.text = if (id.startsWith("@")) id else "@$id"
        }

        holder.tvDescription.visibility = View.INVISIBLE

        holder.ivProfile?.let { iv ->
            if (u.profileImage.isNullOrBlank()) {
                iv.setImageResource(R.drawable.ic_no_profile)
            } else {
                Glide.with(iv)
                    .load(u.profileImage)
                    .placeholder(R.drawable.ic_no_profile)
                    .error(R.drawable.ic_no_profile)
                    .circleCrop()
                    .into(iv)
            }
        }

        var following = false
        updateFollowButton(holder.btnFollow, following)

        holder.btnFollow.setOnClickListener {
            following = !following
            updateFollowButton(holder.btnFollow, following)
        }
    }

    private fun updateFollowButton(btn: TextView, following: Boolean) {
        if (following) {
            btn.text = "팔로잉"
            btn.setBackgroundResource(R.drawable.btn_following_selector)
            btn.setTextColor(btn.context.getColor(R.color.gray2))
        } else {
            btn.text = "팔로우"
            btn.setBackgroundResource(R.drawable.btn_follow_selector)
            btn.setTextColor(btn.context.getColor(R.color.btn_follow_text_selector))
        }
    }
}