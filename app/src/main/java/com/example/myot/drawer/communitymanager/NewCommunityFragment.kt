package com.example.myot.drawer.communitymanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.myot.R

// 새 커뮤니티 개설 프래그먼트
// - Toolbar 의 ic_exit(뒤로가기 버튼) 클릭 → CommunityManagerFragment 로 이동
class NewCommunityFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_new_community, container, false)

        // 툴바의 navigationIcon(ic_exit) 클릭 시 CommunityManagerFragment 로 이동
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_new_community)
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, CommunityManagerFragment())
                .commit()
        }

        return view
    }
}
