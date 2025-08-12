package com.example.myot.memorybook

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myot.databinding.ActivityEditMemoryBinding
import com.example.myot.memorybook.SectionUtils.flattenSections
import com.example.myot.memorybook.api.Content
import com.example.myot.memorybook.api.MemoryBookRequest
import com.example.myot.memorybook.api.MemoryBookRetrofitClient
import com.google.gson.Gson

// 메모리북 수정 화면 액티비티
class EditMemoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditMemoryBinding // 뷰 바인딩
    private lateinit var editAdapter: SectionEditAdapter // 리사이클러뷰 어댑터
    private lateinit var viewModel: MemoryBookViewModel // 뷰모델
    private var originalSections: List<Section> = emptyList() // 원래 섹션

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditMemoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ViewModel 생성 (MemoryBookApi를 수동으로 주입)
        val memoryBookApi = MemoryBookRetrofitClient.memoryBookApi
        val factory = MemoryBookViewModelFactory(memoryBookApi)
        viewModel = ViewModelProvider(this, factory)[MemoryBookViewModel::class.java]

        // 기존 섹션 받아오기
        originalSections = intent.getSerializableExtra("sections") as? List<Section> ?: emptyList()
        val flatList = flattenSections(originalSections)

        // 어댑터 설정
        editAdapter = SectionEditAdapter(flatList.toMutableList())
        binding.editRecycler.apply {
            layoutManager = LinearLayoutManager(this@EditMemoryActivity)
            adapter = editAdapter
        }

        // API 응답 관찰
        viewModel.createResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "메모리북 생성 성공!", Toast.LENGTH_SHORT).show()
                finish() // 생성 완료 후 액티비티 종료
            }.onFailure {
                Toast.makeText(this, "에러: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // 저장 버튼 클릭 리스너
        binding.saveButton.setOnClickListener {
            val modifiedList = editAdapter.getModifiedList()

            // 추후 제목 EditText 추가 시 바인딩 처리 필요
            val title = "임시 제목" // 현재는 하드코딩된 임시 제목
            val paragraph = SectionUtils.convertSectionsToParagraph(modifiedList)

            // 서버에 보낼 Request 객체 생성
            val request = MemoryBookRequest(
                targetType = "MUSICAL", // 예시: 뮤지컬
                targetId = 1,           // 예시 ID
                title = title,
                content = Content(paragraph),
                images = emptyList()    // 현재 이미지 없음
            )

            // 디버깅용 JSON 출력
            val requestJson = Gson().toJson(request)
            Log.d("MemoryBookRequest_JSON", requestJson)

            // API 호출
            viewModel.createMemoryBook(request)
        }

        // 새 섹션 추가 버튼 클릭 리스너
        binding.addButton.setOnClickListener {
            val newSection = Section(
                title = "새 항목",
                content = "",
                depth = 0
            )
            editAdapter.addSection(newSection)

            // 추가 후 스크롤 맨 아래로 이동
            binding.editRecycler.post {
                binding.editRecycler.scrollToPosition(editAdapter.itemCount - 1)
            }
        }
    }
}
