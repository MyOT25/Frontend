package com.example.myot.write

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myot.R
import com.example.myot.retrofit2.CommunityService
import com.example.myot.retrofit2.RetrofitClient
import com.example.myot.retrofit2.TokenStore
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CommunityTabFragment(
    private val items: List<CommunityOption>,
    private val selectedId: Long?,
    private val onPick: (CommunityOption) -> Unit
) : Fragment() {

    companion object {
        fun new(
            items: List<CommunityOption>,
            selectedId: Long?,
            onPick: (CommunityOption) -> Unit
        ) = CommunityTabFragment(items, selectedId, onPick)
    }

    // 썸네일 캐시 (id -> url)
    private val coverCache = mutableMapOf<Long, String?>()
    private val communityService: CommunityService by lazy { RetrofitClient.communityService }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val rv = RecyclerView(requireContext())
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = CommunityAdapter(
            items = items,
            selectedId = selectedId,
            onClick = onPick,
            getCover = ::fetchCoverUrl,
            coverCache = coverCache
        )
        rv.setPadding(0, 4, 0, 24)
        rv.clipToPadding = false
        return rv
    }

    private fun fetchCoverUrl(option: CommunityOption, callback: (String?) -> Unit) {
        val id = option.id
        coverCache[id]?.let { callback(it); return }

        viewLifecycleOwner.lifecycleScope.launch {
            val raw = TokenStore.loadAccessToken(requireContext())
            if (raw.isNullOrBlank()) { callback(null); return@launch }
            val bearer = "Bearer " + raw.trim().removePrefix("Bearer ").trim().removeSurrounding("\"")

            runCatching {
                withContext(kotlinx.coroutines.Dispatchers.IO) {
                    communityService.getCommunityDetail(bearer, option.type, id.toInt())
                }
            }.onSuccess { res ->
                val url = if (res.isSuccessful) {
                    res.body()?.community?.coverImage
                } else null
                coverCache[id] = url
                callback(url)
            }.onFailure {
                coverCache[id] = null
                callback(null)
            }
        }
    }


    private class CommunityAdapter(
        private val items: List<CommunityOption>,
        selectedId: Long?,
        private val onClick: (CommunityOption) -> Unit,
        private val getCover: (CommunityOption, (String?) -> Unit) -> Unit,
        private val coverCache: MutableMap<Long, String?>
    ) : RecyclerView.Adapter<CommunityAdapter.CommunityVH>() {

        private var checkedPos: Int = items.indexOfFirst { it.id == selectedId }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommunityVH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_community_option, parent, false)
            return CommunityVH(v)
        }

        override fun onBindViewHolder(holder: CommunityVH, position: Int) {
            val item = items[position]
            val selected = position == checkedPos

            // 타입에 따른 설명 보장
            val desc = when (item.type.lowercase()) {
                "musical" -> "관극 커뮤니티"
                "actor"   -> "배우 커뮤니티"
                else      -> item.desc.orEmpty()
            }

            val cachedCover = coverCache[item.id]
            holder.bind(item, desc, selected, cachedCover)

            // 썸네일이 없으면 상세 조회로 지연 로딩
            if (cachedCover.isNullOrBlank()) {
                getCover(item) {
                    val pos = holder.bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) notifyItemChanged(pos)
                }
            }

            holder.itemView.setOnClickListener {
                val prev = checkedPos
                checkedPos = holder.bindingAdapterPosition
                if (prev != -1) notifyItemChanged(prev)
                notifyItemChanged(checkedPos)
                onClick(item)
            }
        }

        override fun getItemCount() = items.size

        inner class CommunityVH(view: View) : RecyclerView.ViewHolder(view) {
            private val ivCover: ImageView = view.findViewById(R.id.iv_cover)
            private val tvName : TextView  = view.findViewById(R.id.tv_name)
            private val tvDesc : TextView  = view.findViewById(R.id.tv_desc)
            private val ivCheck: ImageView = view.findViewById(R.id.iv_check)

            fun bind(
                item: CommunityOption,
                descText: String,
                selected: Boolean,
                cachedCover: String?
            ) {
                tvName.text = item.name
                tvDesc.text = descText
                tvDesc.visibility = if (descText.isBlank()) View.GONE else View.VISIBLE

                val cover = cachedCover ?: item.imageUrl
                if (cover.isNullOrBlank()) {
                    ivCover.setImageResource(R.drawable.ic_no_community)
                } else {
                    Glide.with(ivCover).load(cover).circleCrop().into(ivCover)
                }

                ivCheck.visibility = if (selected) View.VISIBLE else View.GONE
            }
        }
    }
}