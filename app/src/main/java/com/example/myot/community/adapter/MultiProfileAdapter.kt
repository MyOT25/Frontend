package com.example.myot.community.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.community.model.Profile
import com.example.myot.databinding.ItemMultiProfileBinding

class MultiProfileAdapter(private val profiles: List<Profile>) :
    RecyclerView.Adapter<MultiProfileAdapter.ProfileViewHolder>() {

    inner class ProfileViewHolder(val binding: ItemMultiProfileBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMultiProfileBinding.inflate(inflater, parent, false)
        return ProfileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val profile = profiles[position]
        //holder.binding.ivMultiProfileImage.setImageResource(profile.profileImage)
        holder.binding.tvMultiProfileNickName.text = profile.nickname
        holder.binding.tvMultiProfileIntroduce.text = profile.bio
    }

    override fun getItemCount(): Int = profiles.size
}