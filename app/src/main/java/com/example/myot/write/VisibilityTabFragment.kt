package com.example.myot.write

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import com.example.myot.R

class VisibilityTabFragment(
    private val isPublicNow: Boolean,
    private val onPick: (Boolean) -> Unit
) : Fragment() {

    companion object {
        fun new(isPublicNow: Boolean, onPick: (Boolean) -> Unit) =
            VisibilityTabFragment(isPublicNow, onPick)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.item_visibility_content, container, false)

        val rowPublic = root.findViewById<View>(R.id.row_public)
        val rowFollowers = root.findViewById<View>(R.id.row_followers)

        val ivPublic = root.findViewById<ImageView>(R.id.iv_public)
        val tvPublic = root.findViewById<TextView>(R.id.tv_public)
        val ivFollowers = root.findViewById<ImageView>(R.id.iv_followers)
        val tvFollowers = root.findViewById<TextView>(R.id.tv_followers)

        fun tint(imageView: ImageView, color: Int) {
            ImageViewCompat.setImageTintList(imageView, ColorStateList.valueOf(color))
        }

        fun applySelected(isPublicSelected: Boolean) {
            val gray = ContextCompat.getColor(requireContext(), R.color.gray3)
            val blue = ContextCompat.getColor(requireContext(), R.color.point_blue)

            // 전체 공개
            tint(ivPublic, if (isPublicSelected) blue else gray)
            tvPublic.setTextColor(if (isPublicSelected) blue else gray)

            // 팔로워에게 공개
            tint(ivFollowers, if (isPublicSelected) gray else blue)
            tvFollowers.setTextColor(if (isPublicSelected) gray else blue)
        }

        // 초기 상태 반영
        applySelected(isPublicNow)

        // 클릭 이벤트 (즉시 반영 후 콜백)
        rowPublic.setOnClickListener {
            applySelected(true)
            onPick(true)
        }
        rowFollowers.setOnClickListener {
            applySelected(false)
            onPick(false)
        }

        return root
    }
}