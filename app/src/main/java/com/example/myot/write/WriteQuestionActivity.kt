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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.myot.R
import com.example.myot.databinding.ActivityWriteQuestionBinding

class WriteQuestionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWriteQuestionBinding

    private var isAnonymous = true
    private val selectedImageUris = mutableListOf<Uri>()
    private val MAX_IMAGE_COUNT = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 닫기 기능
        binding.tvCancel.setOnClickListener {
            finish()
        }

        // 팝업 메뉴 기능
        binding.tvAnonymous.setOnClickListener {
            showAnonymousPopup(it)
        }

        // 사진 추가
        binding.btnAddImage.setOnClickListener {
            if (selectedImageUris.size >= MAX_IMAGE_COUNT) {
                return@setOnClickListener
            }
            pickImagesLauncher.launch("image/*")
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

    private val pickImagesLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris != null) {
            for (uri in uris) {
                if (selectedImageUris.size >= MAX_IMAGE_COUNT) break
                selectedImageUris.add(uri)
                addImageThumbnail(uri)
            }
            updateImageCount()
        }
    }

    private fun addImageThumbnail(uri: Uri) {
        val imageContainer = binding.layoutImageContainer

        val imageLayout = LayoutInflater.from(this).inflate(R.layout.item_write_question_img, imageContainer, false)

        val imageView = imageLayout.findViewById<ImageView>(R.id.iv_thumbnail)
        val btnRemove = imageLayout.findViewById<ImageView>(R.id.btn_remove)

        imageView.setImageURI(uri)
        btnRemove.setOnClickListener {
            imageContainer.removeView(imageLayout)
            selectedImageUris.remove(uri)
            updateImageCount()
        }

        imageContainer.addView(imageLayout)
    }

    private fun updateImageCount() {
        binding.tvImageCount.text = "${selectedImageUris.size}/$MAX_IMAGE_COUNT"
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