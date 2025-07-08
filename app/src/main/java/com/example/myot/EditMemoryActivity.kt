package com.example.myot

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myot.databinding.ActivityEditMemoryBinding

// CmMemoryFragment의 '수정' 기능을 위한 액티비티
// 수정 버튼 클릭 시 이 액티비티로 이동하여 목차 및 내용을 수정 가능

class EditMemoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditMemoryBinding
    private lateinit var editAdapter: SectionEditAdapter
    private var originalSections: List<Section> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditMemoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 기존 섹션 리스트 받아오기
        originalSections = intent.getSerializableExtra("sections") as? List<Section> ?: emptyList()

        // 평탄화된 리스트로 어댑터 초기화
        val flatList = flattenSections(originalSections)
        editAdapter = SectionEditAdapter(flatList.toMutableList())

        // RecyclerView 연결
        binding.editRecycler.apply {
            layoutManager = LinearLayoutManager(this@EditMemoryActivity)
            adapter = editAdapter
        }

        // 저장 버튼 클릭 → 수정된 리스트 반환
        binding.saveButton.setOnClickListener {
            val modifiedList = editAdapter.getModifiedList()
            val resultIntent = Intent().apply {
                putExtra("modifiedSections", ArrayList(modifiedList)) // Serializable 필요
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        // 새 항목 추가 버튼 클릭 시 → 리스트에 항목 추가
        binding.addButton.setOnClickListener {
            val newSection = Section(
                title = "새 항목",
                content = "",
                depth = 0
            )
            editAdapter.addSection(newSection)

            // 마지막 위치로 스크롤
            binding.editRecycler.post {
                binding.editRecycler.scrollToPosition(editAdapter.itemCount - 1)
            }
        }
    }
}
