package com.example.myot.write

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.myot.R
import com.example.myot.databinding.ActivityWriteQuestionBinding
import com.example.myot.question.data.QuestionRepository
import com.example.myot.retrofit2.RetrofitClient
import kotlinx.coroutines.launch

class WriteQuestionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWriteQuestionBinding

    private var isAnonymous = true
    private val selectedImageUris = mutableListOf<Uri>()
    private val repo by lazy {
        QuestionRepository(
            service = RetrofitClient.questionService,
            contentResolver = contentResolver
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 초기 상태: 게시하기 비활성, 이미지 영역 숨김
        setPostEnabled(false)
        binding.scrollImageContainer.isVisible = false

        val tip = "#해시태그로 내 질문을 분류해보세요!"
        val spannable = SpannableString(tip).apply {
            val hashEnd = "#해시태그로".length
            setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this@WriteQuestionActivity, R.color.point_blue)),
                0, hashEnd,
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        binding.tvHashtagTip.text = spannable

        // 제목 입력 감지 → 이미지 영역 & 게시하기 버튼 상태
        binding.etTitle.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val hasTitle = !s.isNullOrBlank()
                binding.scrollImageContainer.isVisible = hasTitle
                setPostEnabled(hasTitle)
            }
        })

        // 본문 입력 감지 → 해시태그 안내 보이기/숨기기
        binding.etContent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tvHashtagTip.isVisible = !s.isNullOrBlank()
            }
        })

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
            val title = binding.etTitle.text?.toString()?.trim().orEmpty()
            val content = binding.etContent.text?.toString()?.trim().orEmpty()
            if (title.isBlank()) return@setOnClickListener
            if (content.isBlank()) return@setOnClickListener

            val tagIds: List<Long> = emptyList()
            val imageUris = selectedImageUris

            setPostEnabled(false)
            lifecycleScope.launch {
                val result = repo.postQuestionMultipart(
                    title = title,
                    content = content,
                    tagIds = tagIds,
                    imageUris = imageUris,
                    anonymous = isAnonymous
                )
                setPostEnabled(true)

                result.onSuccess { created ->
                    val isAnon = created.isAnonymous ?: isAnonymous

                    val intent = Intent().apply {
                        putExtra("createdQuestionId", created.id)
                        putExtra("title", created.title)
                        putExtra("content", created.content)
                        putStringArrayListExtra("tags", ArrayList(created.tags))
                        putExtra("authorName", created.username)
                        putExtra("createdAt", created.createdAt)
                        putExtra("isAnonymous", isAnon)
                    }
                    setResult(RESULT_OK, intent)
                    finish()
                }.onFailure { e ->
                    Toast.makeText(this@WriteQuestionActivity, "등록 실패: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setPostEnabled(enabled: Boolean) {
        binding.tvPost.isEnabled = enabled
        if (enabled) {
            binding.tvPost.setBackgroundResource(R.drawable.bg_write_btn)
            binding.tvPost.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            binding.tvPost.setBackgroundResource(R.drawable.bg_write_btn_disabled)
            binding.tvPost.setTextColor(ContextCompat.getColor(this, R.color.gray2))
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