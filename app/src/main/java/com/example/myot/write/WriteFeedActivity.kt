package com.example.myot.write

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.myot.R
import com.example.myot.databinding.ActivityWriteFeedBinding
import java.util.*

class WriteFeedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWriteFeedBinding
    private val selectedImageUris = mutableListOf<Uri>()
    private var isPublic = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateImageThumbnails()

        binding.tvVisibility.setOnClickListener {
            showVisibilityPopup(it)
        }

        binding.tvCancel.setOnClickListener {
            finish()
        }

        binding.tvPost.setOnClickListener {


            val resultIntent = Intent().apply {
                putExtra("isPublic", isPublic)
                putExtra("content", binding.etContent.text.toString())
                putStringArrayListExtra("imageUrls", selectedImageUris.map { it.toString() } as ArrayList<String>)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun updateImageThumbnails() {
        binding.layoutImageContainer.removeAllViews()

        // 1. 이미지 추가 버튼을 먼저 추가
        val addButtonView = LayoutInflater.from(this)
            .inflate(R.layout.item_write_add_feed_img, binding.layoutImageContainer, false)
        val tvCount = addButtonView.findViewById<TextView>(R.id.tv_image_count)
        tvCount.text = "${selectedImageUris.size}/4"

        addButtonView.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.layoutImageContainer.addView(addButtonView)

        // 2. 그다음 이미지들을 오른쪽에 추가
        selectedImageUris.forEach { uri ->
            val thumbnailView = LayoutInflater.from(this)
                .inflate(R.layout.item_write_feed_img, binding.layoutImageContainer, false)

            val ivThumbnail = thumbnailView.findViewById<ImageView>(R.id.iv_thumbnail)
            val btnDelete = thumbnailView.findViewById<ImageView>(R.id.btn_remove)

            ivThumbnail.setImageURI(uri)

            thumbnailView.tag = uri
            btnDelete.setOnClickListener {
                selectedImageUris.remove(uri)
                updateImageThumbnails()
            }

            binding.layoutImageContainer.addView(thumbnailView)
        }
    }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            if (uris != null) {
                val remaining = 4 - selectedImageUris.size
                selectedImageUris.addAll(uris.take(remaining))
                updateImageThumbnails()
            }
        }

    private fun showVisibilityPopup(anchor: View) {
        val context = anchor.context
        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(R.layout.menu_popup_visibility, null)

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupWidth = popupView.measuredWidth

        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        val anchorX = location[0]
        val anchorY = location[1]

        val rootView = (anchor.rootView as? ViewGroup) ?: return
        val dimView = View(context).apply {
            setBackgroundColor(0x22000000.toInt())
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        rootView.addView(dimView)
        popupWindow.setOnDismissListener { rootView.removeView(dimView) }

        popupWindow.setBackgroundDrawable(null)
        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true
        popupWindow.elevation = 20f

        val offsetX = anchor.width - popupWidth + 90
        val offsetY = anchor.height - 165

        popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, anchorX + offsetX, anchorY + offsetY)

        // 항목 클릭 이벤트
        popupView.findViewById<View>(R.id.btn_set_public).setOnClickListener {
            popupWindow.dismiss()
            isPublic = true
            binding.tvVisibility.text = "전체 공개"
        }

        popupView.findViewById<View>(R.id.btn_set_friends).setOnClickListener {
            popupWindow.dismiss()
            isPublic = false
            binding.tvVisibility.text = "친구 공개"
        }
    }
}