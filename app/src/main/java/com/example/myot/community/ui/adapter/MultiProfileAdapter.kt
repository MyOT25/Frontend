package com.example.myot.community.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myot.community.model.Profile
import com.example.myot.databinding.ItemMultiProfileBinding

class MultiProfileAdapter(
    private var profiles: List<Profile>,
    private val onClick: (Profile) -> Unit
) :
    RecyclerView.Adapter<MultiProfileAdapter.ProfileViewHolder>() {

    inner class ProfileViewHolder(val binding: ItemMultiProfileBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(profile: Profile) {
            binding.tvMultiProfileNickName.text = profile.nickname
            binding.tvMultiProfileIntroduce.text = profile.bio
            binding.layoutMultiProfile.setOnClickListener {
                onClick(profile)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMultiProfileBinding.inflate(inflater, parent, false)
        return ProfileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        holder.bind(profiles[position])
    }

    override fun getItemCount(): Int = profiles.size

    fun updateList(newProfiles: List<Profile>) {
        profiles = newProfiles
        notifyDataSetChanged()
    }
}