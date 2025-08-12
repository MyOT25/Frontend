package com.example.myot.drawer.communitymanager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myot.R

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

//내 커뮤니티 프래그먼트
class MyCommunityFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_community, container, false)
    }
}