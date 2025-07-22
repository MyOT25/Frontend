package com.example.myot.memorybook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import androidx.core.widget.addTextChangedListener
import com.example.myot.R

//수정 기능을 위한 어댑터
class SectionEditAdapter(
    private val items: MutableList<Section> // 수정 가능한 섹션 리스트
) : RecyclerView.Adapter<SectionEditAdapter.SectionEditViewHolder>() {

    // ViewHolder 클래스: 각 항목의 뷰를 참조
    inner class SectionEditViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleEdit: EditText = itemView.findViewById(R.id.editTitle)         // 제목 입력 필드
        val contentEdit: EditText = itemView.findViewById(R.id.editContent)     // 내용 입력 필드
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton) // 삭제 버튼
    }

    // 새로운 ViewHolder 생성 (XML 레이아웃 inflate)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionEditViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_section_edit, parent, false)
        return SectionEditViewHolder(view)
    }

    // ViewHolder에 데이터 바인딩
    override fun onBindViewHolder(holder: SectionEditViewHolder, position: Int) {
        val section = items[position]

        // 제목/내용 설정
        holder.titleEdit.setText(section.title)
        holder.contentEdit.setText(section.content)

        // EditText 값 변경 → Section 객체에 즉시 반영
        holder.titleEdit.addTextChangedListener {
            section.title = it.toString()
        }
        holder.contentEdit.addTextChangedListener {
            section.content = it.toString()
        }

        // 삭제 버튼 클릭 시 항목 제거
        holder.deleteButton.setOnClickListener {
            items.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemCount)
        }
    }

    // 전체 항목 수 반환
    override fun getItemCount(): Int = items.size

    // 수정 완료 시 최종 리스트 반환
    fun getModifiedList(): List<Section> = items

    // 새 섹션 추가 함수 (Add 버튼에서 호출)
    fun addSection(section: Section) {
        items.add(section)
        notifyItemInserted(items.size - 1)
    }
}
