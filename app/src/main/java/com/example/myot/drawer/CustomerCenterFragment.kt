package com.example.myot.drawer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myot.R

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

//고객센터 프래그먼트
class CustomerCenterFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_customer_center, container, false)
    }
}