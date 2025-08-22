package com.example.myot.write

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.myot.R
import com.example.myot.databinding.ActivityWriteFeedBinding
import com.example.myot.retrofit2.PostCreateRequest
import com.example.myot.retrofit2.RetrofitClient
import com.example.myot.retrofit2.TokenStore
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class WriteFeedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWriteFeedBinding
    private val selectedImageUris = mutableListOf<Uri>()
    private var isPublic = true
    private var selectedCommunity = CommunityOption(-1, "커뮤니티명")
    private val communityOptions = mutableListOf<CommunityOption>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWriteFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateImageThumbnails()

        renderChips()

        validatePost()

        binding.tvCancel.setOnClickListener {
            finish()
        }

        binding.tvVisibility.setOnClickListener {
            openBottomSheet(PublishBottomSheet.Tab.VISIBILITY)
        }
        binding.tvCommunity.setOnClickListener {
            openBottomSheet(PublishBottomSheet.Tab.COMMUNITY)
        }

        binding.etContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validatePost()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.tvPost.setOnClickListener {
            submitPost()
        }

        loadMyCommunities()
    }

    private fun submitPost() {
        val content = binding.etContent.text?.toString()?.trim().orEmpty()
        val hasCommunity = selectedCommunity.id != -1L
        if (content.isBlank() || !hasCommunity) {
            validatePost()
            return
        }

        setPostEnabled(false)
        binding.tvPost.text = "업로드 중…"

        val visibility = if (isPublic) "public" else "friends"
        val imageUrls = selectedImageUris.map { it.toString() }

        val body = PostCreateRequest(
            content = content,
            postImages = imageUrls,
            communityId = selectedCommunity.id,
            visibility = visibility
        )

        lifecycleScope.launch {
            try {
                val raw = TokenStore.loadAccessToken(this@WriteFeedActivity)
                if (raw.isNullOrBlank()) {
                    restorePostButton(); return@launch
                }
                val bearer = "Bearer " + raw.trim().removePrefix("Bearer ").trim().removeSurrounding("\"")

                val res = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    RetrofitClient.postService.createPost(bearer, body)
                }

                if (res.isSuccessful && res.body()?.resultType == "SUCCESS") {
                    finish()
                } else {
                    restorePostButton()
                }
            } catch (e: Exception) {
                restorePostButton()
            }
        }
    }

    private fun restorePostButton() {
        binding.tvPost.text = "게시하기"
        validatePost()
    }

    private fun openBottomSheet(initial: PublishBottomSheet.Tab) {
        if (communityOptions.isEmpty()) loadMyCommunities()

        PublishBottomSheet(
            initialTab = initial,
            isPublicNow = isPublic,
            communities = communityOptions.toList(),
            selectedCommunityId = selectedCommunity.id,
            listener = object : PublishBottomSheet.Listener {
                override fun onSelectVisibility(isPublic: Boolean) {
                    this@WriteFeedActivity.isPublic = isPublic
                    renderChips()
                    validatePost()
                }
                override fun onSelectCommunity(option: CommunityOption) {
                    selectedCommunity = option
                    renderChips()
                    validatePost()
                }
            }
        ).show(supportFragmentManager, "publish_sheet")
    }

    private fun loadMyCommunities() {
        val service = com.example.myot.retrofit2.RetrofitClient.communityService

        lifecycleScope.launch {
            try {
                val raw = com.example.myot.retrofit2.TokenStore.loadAccessToken(this@WriteFeedActivity)
                if (raw.isNullOrBlank()) return@launch
                val bearer = "Bearer " + raw.trim().removePrefix("Bearer ").trim().removeSurrounding("\"")

                val res = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    service.getMyCommunities(bearer)
                }
                if (!res.isSuccessful) return@launch
                val body = res.body()
                val mine = if (body?.success == true) body.communities else emptyList()

                communityOptions.clear()
                communityOptions.addAll(mine.map { it.toOption() })
            } catch (_: Exception) { }
        }
    }

    private fun renderChips() {
        binding.tvVisibility.text = if (isPublic) "전체 공개" else "팔로워 공개"
        binding.tvCommunity.text = selectedCommunity.name
    }

    private fun updateImageThumbnails() {
        binding.layoutImageContainer.removeAllViews()

        // 1. 이미지 추가 버튼을 먼저 추가
        val addButtonView = LayoutInflater.from(this)
            .inflate(R.layout.item_write_add_feed_img, binding.layoutImageContainer, false)
        val tvCount = addButtonView.findViewById<TextView>(R.id.tv_image_count)
        tvCount.text = "${selectedImageUris.size}/4"

        addButtonView.setOnClickListener {
            imagePickerLauncher.launch(arrayOf("image/*"))
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

    private fun validatePost() {
        val hasContent = binding.etContent.text?.isNotBlank() == true
        val hasCommunity = selectedCommunity.id != -1L
        setPostEnabled(hasContent && hasCommunity)
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

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            if (uris != null) {

                uris.forEach { uri ->
                    try {
                        contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (_: SecurityException) {
                    }
                }
                val remaining = 4 - selectedImageUris.size
                selectedImageUris.addAll(uris.take(remaining))
                updateImageThumbnails()
            }
        }
}