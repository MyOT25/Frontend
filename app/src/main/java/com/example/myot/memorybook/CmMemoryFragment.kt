package com.example.myot.memorybook

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myot.databinding.FragmentCmMemoryBinding

//메모리북 프래그먼트
class CmMemoryFragment : Fragment() {

    private var _binding: FragmentCmMemoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MemoryBookViewModel

    private lateinit var contentAdapter: ContentAdapter
    private lateinit var tocAdapter: TocAdapter

    private val EDIT_REQUEST_CODE = 1001 // 수정 요청 코드

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCmMemoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[MemoryBookViewModel::class.java]

        // 콘텐츠 어댑터
        contentAdapter = ContentAdapter(emptyList()) { section ->
            section.isExpanded = !section.isExpanded
            val flatList = flattenSections(viewModel.sectionList.value ?: emptyList())
            contentAdapter.submitList(flatList)
            tocAdapter.submitList(flatList)
            binding.memoryContentRecycler.post {
                binding.memoryContentRecycler.adapter?.notifyDataSetChanged()
            }
        }

        // 목차 어댑터
        tocAdapter = TocAdapter(emptyList()) { section ->
            val flatList = contentAdapter.getCurrentList()
            val index = flatList.indexOfFirst { it.id == section.id }

            if (index != -1) {
                binding.memoryContentRecycler.post {
                    binding.memoryContentRecycler.scrollToPosition(index)
                }
            }
        }

        // 콘텐츠 RecyclerView 설정
        binding.memoryContentRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = contentAdapter
            setHasFixedSize(false)              // wrap_content 안정화
            isNestedScrollingEnabled = false    // NestedScrollView와 충돌 방지
        }

        // 목차 RecyclerView 설정
        binding.memoryTocRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tocAdapter
            setHasFixedSize(false)
            isNestedScrollingEnabled = false
        }

        // ViewModel에서 데이터 옵저빙 → 리스트 갱신
        viewModel.sectionList.observe(viewLifecycleOwner) { sectionList ->
            val flatList = flattenSections(sectionList)
            contentAdapter.submitList(flatList)
            tocAdapter.submitList(flatList)

            binding.memoryContentRecycler.post {
                binding.memoryContentRecycler.adapter?.notifyDataSetChanged()
            }
            binding.memoryTocRecycler.post {
                binding.memoryTocRecycler.adapter?.notifyDataSetChanged()
            }
        }

        // 수정 버튼 클릭 → EditMemoryActivity 실행
        binding.memoryEditButton.setOnClickListener {
            val currentSections = viewModel.sectionList.value ?: emptyList()
            val intent = Intent(requireContext(), EditMemoryActivity::class.java).apply {
                putExtra("sections", ArrayList(currentSections)) // Section은 Serializable 구현 필요
            }
            startActivityForResult(intent, EDIT_REQUEST_CODE)
        }
    }

    // 수정 후 결과 반영
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EDIT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val modifiedSections = data?.getSerializableExtra("modifiedSections") as? List<Section>
            if (modifiedSections != null) {
                viewModel.replaceAllSections(modifiedSections)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
