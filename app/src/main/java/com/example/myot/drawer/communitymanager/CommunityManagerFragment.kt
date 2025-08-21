package com.example.myot.drawer.communitymanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myot.R
import com.example.myot.home.HomeFragment

// 커뮤니티 관리 프래그먼트
// - "내 커뮤니티" 클릭 → MyCommunityFragment
// - "새 커뮤니티 개설" 클릭 → NewCommunityFragment
// - "뒤로가기(ic_exit)" 클릭 → MainActivity의 홈 화면(HomeFragment) 복귀
class CommunityManagerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_community_manager, container, false)

        // "내 커뮤니티" 클릭 시 이동
        val myCommunityText = view.findViewById<TextView>(R.id.my_community)
        myCommunityText.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, MyCommunityFragment())
                .addToBackStack(null)
                .commit()
        }

        // "새 커뮤니티 개설" 클릭 시 이동
        val newCommunityText = view.findViewById<TextView>(R.id.new_community)
        newCommunityText.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, NewCommunityFragment())
                .addToBackStack(null)
                .commit()
        }

        // "뒤로가기" 아이콘 클릭 시 홈으로 복귀
        val exitIcon = view.findViewById<ImageView>(R.id.ic_exit)
        exitIcon.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, HomeFragment()) // 홈 화면으로 이동
                .commit()
        }

        return view
    }
}
