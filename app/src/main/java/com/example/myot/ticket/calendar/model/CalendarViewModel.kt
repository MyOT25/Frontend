package com.example.myot.ticket.calendar.model

import android.util.Log
import com.example.myot.retrofit2.AuthStore
import com.example.myot.retrofit2.CalendarService
import com.example.myot.retrofit2.RetrofitClient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Response
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.YearMonth
import java.time.ZoneId

data class RecordCell(
    val postId: Int,
    val musicalTitle: String,
    val imageUrl: String
)

data class CalendarUiState(
    val yearMonth: YearMonth = YearMonth.now(),
    val records: Map<Int, List<RecordCell>> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMsg: String? = null
)

class CalendarViewModel : ViewModel() {

    private val service: CalendarService = RetrofitClient.calendarService

    private val _ui = MutableStateFlow(CalendarUiState())
    val ui: StateFlow<CalendarUiState> = _ui

    fun setMonth(ym: YearMonth) { _ui.update { it.copy(yearMonth = ym) } }
    fun loadCurrentMonth() {
        val ym = _ui.value.yearMonth
        loadMonthInternal(ym.year, ym.monthValue)
    }
    fun refreshCurrentMonth() = loadCurrentMonth()
    fun nextMonth() { setMonth(_ui.value.yearMonth.plusMonths(1)); loadCurrentMonth() }
    fun prevMonth() { setMonth(_ui.value.yearMonth.minusMonths(1)); loadCurrentMonth() }

    private fun loadMonthInternal(year: Int, month: Int) {
        Log.d("CalendarVM", "loadMonthInternal: $year-$month")
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, errorMsg = null) }
            try {
                val authToken = AuthStore.bearerOrThrow() // 매번 최신 토큰
                val res: Response<MonthlyRecordsResponse> =
                    service.getMonthlyRecords(authToken, year, month)

                if (res.isSuccessful) {
                    val body = res.body()
                    val records: Map<Int, List<RecordCell>> =
                        body?.success?.data
                            ?.groupBy { rec ->
                                // 서버가 TZ를 고려하지 않으므로 앞 10자리(yyyy-MM-dd)만 사용
                                LocalDate.parse(rec.watchDate.take(10)).dayOfMonth
                            }
                            ?.mapValues { (_, list) ->
                                list
                                    .distinctBy { it.postId } // 같은 postId 중복 제거
                                    .mapNotNull { rec ->
                                        val url = rec.imageUrls.firstOrNull()?.takeIf { it.isNotBlank() }
                                            ?: return@mapNotNull null
                                        RecordCell(
                                            postId = rec.postId,
                                            musicalTitle = rec.musicalTitle,
                                            imageUrl = url
                                        )
                                    }
                                    .take(3) // 하루 최대 3개
                            }
                            ?.filterValues { it.isNotEmpty() }
                            ?: emptyMap()

                    _ui.update { it.copy(records = records, isLoading = false, errorMsg = null) }
                } else {
                    _ui.update {
                        it.copy(
                            records = emptyMap(),
                            isLoading = false,
                            errorMsg = res.errorBody()?.string() ?: "HTTP ${res.code()}"
                        )
                    }
                }
            } catch (e: Exception) {
                _ui.update { it.copy(records = emptyMap(), isLoading = false, errorMsg = e.message) }
            }
        }
    }
}
