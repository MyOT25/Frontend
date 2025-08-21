package com.example.myot.drawer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.myot.R
import com.example.myot.home.HomeFragment

// 티켓마크 프래그먼트
// - Toolbar 의 ic_exit(뒤로가기 버튼) 클릭 → HomeFragment 로 이동
class TicketMarkFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ticket_mark, container, false)

        // 툴바의 navigationIcon(ic_exit) 클릭 시 HomeFragment 로 이동
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar_ticket_mark)
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, HomeFragment())
                .commit()
        }

        return view
    }
}
