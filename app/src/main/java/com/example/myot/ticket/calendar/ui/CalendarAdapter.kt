package com.example.myot.ticket.calendar.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myot.R
import com.example.myot.databinding.ItemCalendarDayBinding
import com.example.myot.ticket.calendar.model.CalendarDay
import com.example.myot.ticket.calendar.model.CalendarEntry

class CalendarAdapter(
    private val onDayClick: (String, List<CalendarEntry>) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private var days = listOf<CalendarDay>()

    fun updateDays(newDays: List<CalendarDay>) {
        days = newDays
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val binding = ItemCalendarDayBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CalendarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.bind(days[position])
    }

    override fun getItemCount(): Int = days.size

    inner class CalendarViewHolder(
        private val binding: ItemCalendarDayBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(day: CalendarDay) {
            binding.apply {
                tvDay.text = day.text

                // 헤더인 경우
                if (day.isHeader) {
                    tvDay.textSize = 12f
                    tvDay.alpha = 0.7f
                    layoutImages.isVisible = false
                    root.setOnClickListener(null)
                    root.setBackgroundResource(0)
                    return
                }

                // 빈 날짜인 경우
                if (day.text.isEmpty()) {
                    tvDay.text = ""
                    layoutImages.isVisible = false
                    root.setOnClickListener(null)
                    root.setBackgroundResource(0)
                    return
                }

                // 일반 날짜
                tvDay.textSize = 14f
                tvDay.alpha = 1f

                // 오늘 날짜 하이라이트
//                if (day.isToday) {
//                    root.setBackgroundResource(R.drawable.bg_today)
//                } else {
//                    root.setBackgroundResource(R.drawable.bg_calendar_day)
//                }

                // 이미지 표시
                setupImages(day.entries)

                // 클릭 리스너
                root.setOnClickListener {
                    onDayClick(day.text, day.entries)
                }
            }
        }

        private fun setupImages(entries: List<CalendarEntry>) {
            binding.apply {
                layoutImages.isVisible = entries.isNotEmpty()

                // 모든 이미지뷰 초기화
                listOf(ivImage1, ivImage2, ivImage3).forEach { iv ->
                    iv.isVisible = false
                }

                // 최대 3개 이미지 표시
                entries.take(3).forEachIndexed { index, entry ->
                    val imageView = when (index) {
                        0 -> ivImage1
                        1 -> ivImage2
                        2 -> ivImage3
                        else -> return@forEachIndexed
                    }

                    imageView.isVisible = true

                    // 이미지 로드 (Glide 사용)
                    if (entry.imageUri.startsWith("content://") || entry.imageUri.startsWith("file://")) {
                        Glide.with(imageView.context)
                            .load(Uri.parse(entry.imageUri))
                            .centerCrop()
                            .into(imageView)
                    } else {
                        // URL 또는 기타 형태의 이미지
                        Glide.with(imageView.context)
                            .load(entry.imageUri)
                            .centerCrop()
                            .placeholder(R.drawable.ig_poster_placeholder)
                            .into(imageView)
                    }
                }
            }
        }
    }
}