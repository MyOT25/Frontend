package com.example.myot.drawer.communitymanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.myot.R

// 내 커뮤니티 프래그먼트
// - Toolbar 의 ic_exit(뒤로가기 버튼) 클릭 → CommunityManagerFragment 로 이동
class MyCommunityFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_community, container, false)

        // 툴바의 navigationIcon(ic_exit) 클릭 시 CommunityManagerFragment 로 이동
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_my_community)
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, CommunityManagerFragment())
                .commit()
        }

        return view
    }
}
