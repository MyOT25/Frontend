package com.example.myot.feed.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myot.R
import com.example.myot.feed.model.FeedbackUserUi
import com.example.myot.retrofit2.TokenStore
import com.example.myot.retrofit2.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FeedbackAdapter(
    private var users: List<FeedbackUserUi>,
    private val activity: FragmentActivity
) : RecyclerView.Adapter<FeedbackAdapter.ViewHolder>() {

    private val followCache = mutableMapOf<Long, Boolean>()

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
            holder.tvUserid.text = ""
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

        val uid = u.userId
        if (uid == null || uid <= 0L) {
            // userId 없으면 버튼 숨김
            holder.btnFollow.visibility = View.GONE
            return
        } else {
            holder.btnFollow.visibility = View.VISIBLE
        }

        val cached = followCache[uid]
        if (cached == null) {
            // 조회 중 UI
            holder.btnFollow.isEnabled = false
            holder.btnFollow.text = "확인중"
            holder.btnFollow.setBackgroundResource(R.drawable.btn_follow_selector)
            holder.btnFollow.setTextColor(holder.btnFollow.context.getColor(R.color.btn_follow_text_selector))

            activity.lifecycleScope.launch {
                val bearer = loadBearer() ?: run {
                    applyFollowUi(holder.btnFollow, false)
                    holder.btnFollow.isEnabled = true
                    return@launch
                }
                try {
                    val res = withContext(Dispatchers.IO) {
                        RetrofitClient.profileService.isFollowing(bearer, uid)
                    }
                    val isFollowing = res.body()?.success?.isFollowing == true
                    followCache[uid] = isFollowing
                    // 바인딩 유효성 체크
                    if (holder.bindingAdapterPosition != RecyclerView.NO_POSITION &&
                        holder.bindingAdapterPosition < users.size &&
                        users[holder.bindingAdapterPosition].userId == uid) {
                        applyFollowUi(holder.btnFollow, isFollowing)
                    }
                } catch (_: Exception) {
                    applyFollowUi(holder.btnFollow, false)
                } finally {
                    holder.btnFollow.isEnabled = true
                }
            }
        } else {
            applyFollowUi(holder.btnFollow, cached)
            holder.btnFollow.isEnabled = true
        }

        holder.btnFollow.setOnClickListener {
            val current = followCache[uid] ?: false
            val next = !current

            applyFollowUi(holder.btnFollow, next)
            holder.btnFollow.isEnabled = false

            activity.lifecycleScope.launch {
                val bearer = loadBearer()
                if (bearer == null) {
                    applyFollowUi(holder.btnFollow, current)
                    holder.btnFollow.isEnabled = true
                    Toast.makeText(holder.itemView.context, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val ok = withContext(Dispatchers.IO) {
                    try {
                        if (next) {
                            RetrofitClient.profileService.follow(bearer, uid).isSuccessful
                        } else {
                            RetrofitClient.profileService.unfollow(bearer, uid).isSuccessful
                        }
                    } catch (_: Exception) { false }
                }

                if (ok) {
                    followCache[uid] = next
                } else {
                    applyFollowUi(holder.btnFollow, current)
                    Toast.makeText(holder.itemView.context, "팔로우 처리 실패", Toast.LENGTH_SHORT).show()
                }
                holder.btnFollow.isEnabled = true
            }
        }
    }

    private suspend fun loadBearer(): String? {
        val raw = TokenStore.loadAccessToken(activity) ?: return null
        return raw.trim()
            .removePrefix("Bearer ")
            .trim()
            .removeSurrounding("\"")
            .let { "Bearer $it" }
    }

    private fun applyFollowUi(btn: TextView, following: Boolean) {
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