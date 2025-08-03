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
import com.example.myot.databinding.ActivityWriteQuestionBinding

class WriteQuestionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWriteQuestionBinding

    private var isAnonymous = true
    private val selectedImageUris = mutableListOf<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateImageThumbnails()

        // 닫기 기능
        binding.tvCancel.setOnClickListener {
            finish()
        }

        // 팝업 메뉴 기능
        binding.tvAnonymous.setOnClickListener {
            showAnonymousPopup(it)
        }

        // 글쓰기
        binding.tvPost.setOnClickListener {
            val content = binding.etContent.text.toString()

            val resultIntent = Intent().apply {
                putExtra("isAnonymous", isAnonymous)
                putExtra("content", content)
                putStringArrayListExtra(
                    "imageUrls",
                    selectedImageUris.map { it.toString() } as ArrayList<String>
                )
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun updateImageThumbnails() {
        binding.layoutImageContainer.removeAllViews()

        // 1. 이미지 추가 버튼을 먼저 추가
        val addButtonView = LayoutInflater.from(this)
            .inflate(R.layout.item_write_add_question_img, binding.layoutImageContainer, false)
        val tvCount = addButtonView.findViewById<TextView>(R.id.tv_image_count)
        tvCount.text = "${selectedImageUris.size}/5"

        addButtonView.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.layoutImageContainer.addView(addButtonView)

        // 2. 그다음 이미지들을 오른쪽에 추가
        selectedImageUris.forEach { uri ->
            val thumbnailView = LayoutInflater.from(this)
                .inflate(R.layout.item_write_question_img, binding.layoutImageContainer, false)

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
                val remaining = 5 - selectedImageUris.size
                selectedImageUris.addAll(uris.take(remaining))
                updateImageThumbnails()
            }
        }

    private fun showAnonymousPopup(anchor: View) {
        val context = anchor.context
        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(R.layout.menu_popup_anonymous, null)

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
        popupView.findViewById<View>(R.id.btn_set_anonymous).setOnClickListener {
            popupWindow.dismiss()
            isAnonymous = true
            binding.tvAnonymous.text = "익명 질문"
        }

        popupView.findViewById<View>(R.id.btn_set_realname).setOnClickListener {
            popupWindow.dismiss()
            isAnonymous = false
            binding.tvAnonymous.text = "공개 질문"
        }
    }
}