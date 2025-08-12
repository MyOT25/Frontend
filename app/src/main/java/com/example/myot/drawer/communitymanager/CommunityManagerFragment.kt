package com.example.myot.drawer.communitymanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myot.R

//커뮤니티 관리 프래그먼트
// 내 커뮤니티 / 새 커뮤니티 개설로 나뉨
class CommunityManagerFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_community_manager, container, false)
    }
}