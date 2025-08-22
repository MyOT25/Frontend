package com.example.myot.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.myot.R

class SearchFragment : Fragment() {

    // 인기 피드 뷰페이저
    private lateinit var feedViewPager: ViewPager2
    // 추천 커뮤니티 뷰페이저
    private lateinit var communityViewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        // ViewPager 연결
        feedViewPager = view.findViewById(R.id.feedViewPager)
        communityViewPager = view.findViewById(R.id.communityViewPager)

        // 더미 데이터
        val feedItems = listOf(
            FeedItem("유저1", "텍스트 1", hasPhoto = true),
            FeedItem("유저2", "텍스트 2", hasPhoto = false),
            FeedItem("유저3", "텍스트 3", hasPhoto = true),
            FeedItem("유저4", "텍스트 4", hasPhoto = true),
            FeedItem("유저5", "텍스트 5", hasPhoto = false)

        )
        val communityItems = listOf(
            CommunityItem("킹키부츠", "2938명 가입"),
            CommunityItem("리 미제라블", "200명 가입"),
            CommunityItem("배우 이름", "1000명 가입"),
            CommunityItem("웃는 남자", "1515명 가입")
        )

        // 어댑터 연결
        feedViewPager.adapter = FeedPagerAdapter(feedItems)
        communityViewPager.adapter = CommunityPagerAdapter(communityItems)

        return view
    }

    // 인기 피드 데이터 모델
    data class FeedItem(val userName: String, val content: String, val hasPhoto: Boolean)

    // 커뮤니티 데이터 모델
    data class CommunityItem(val name: String, val members: String)

    // 인기 피드 어댑터
    inner class FeedPagerAdapter(private val items: List<FeedItem>) :
        RecyclerView.Adapter<FeedPagerAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textUserName: TextView = view.findViewById(R.id.textUserName)
            val textFeedContent: TextView = view.findViewById(R.id.textFeedContent)
            val textFeedPhoto: TextView = view.findViewById(R.id.textFeedPhoto)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView = layoutInflater.inflate(R.layout.item_search_feed, parent, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.textUserName.text = item.userName
            holder.textFeedContent.text = item.content
            holder.textFeedPhoto.visibility = if (item.hasPhoto) View.VISIBLE else View.GONE
        }

        override fun getItemCount(): Int = items.size
    }

    // 추천 커뮤니티 어댑터
    inner class CommunityPagerAdapter(private val items: List<CommunityItem>) :
        RecyclerView.Adapter<CommunityPagerAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageCommunity: ImageView = view.findViewById(R.id.imageCommunity)
            val textCommunityName: TextView = view.findViewById(R.id.textCommunityName)
            val textCommunityMembers: TextView = view.findViewById(R.id.textCommunityMembers)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView = layoutInflater.inflate(R.layout.item_search_community, parent, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.textCommunityName.text = item.name
            holder.textCommunityMembers.text = item.members
            // imageCommunity는 서버 이미지 연동 시 Glide/Picasso 등으로 처리 가능
        }

        override fun getItemCount(): Int = items.size
    }
}
