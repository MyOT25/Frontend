package com.example.myot.ticket.calendar.model

import com.example.myot.retrofit2.AuthStore
import com.example.myot.retrofit2.CalendarService
import com.example.myot.retrofit2.RetrofitClient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.time.LocalDate
import java.time.YearMonth

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
    private var authToken = AuthStore.bearerOrThrow()

    private val _ui = MutableStateFlow(CalendarUiState())
    val ui: StateFlow<CalendarUiState> = _ui

    fun setMonth(ym: YearMonth) { _ui.value = _ui.value.copy(yearMonth = ym) }
    fun loadCurrentMonth() { val ym = _ui.value.yearMonth; loadMonthInternal(ym.year, ym.monthValue) }
    fun nextMonth() { setMonth(_ui.value.yearMonth.plusMonths(1)); loadCurrentMonth() }
    fun prevMonth() { setMonth(_ui.value.yearMonth.minusMonths(1)); loadCurrentMonth() }

    private fun loadMonthInternal(year: Int, month: Int) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(isLoading = true, errorMsg = null)
            try {
                val res: Response<MonthlyRecordsResponse> =
                    service.getMonthlyRecords(authToken, year, month)

                if (res.isSuccessful) {
                    val body = res.body()
                    val records: Map<Int, List<RecordCell>> =
                        if (body?.success?.data != null) {
                            body.success!!.data
                                .groupBy { LocalDate.parse(it.watchDate.substring(0, 10)).dayOfMonth }
                                .mapValues { (_, list) ->
                                    list
                                        .distinctBy { it.postId } // 같은 postId 중복 제거
                                        .mapNotNull { rec ->
                                            val url = rec.imageUrls.firstOrNull()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                                            RecordCell(postId = rec.postId, musicalTitle = rec.musicalTitle, imageUrl = url)
                                        }
                                        .take(3) // ✅ 하루 최대 3개
                                }
                                .filterValues { it.isNotEmpty() }
                        } else emptyMap()

                    _ui.value = _ui.value.copy(records = records, isLoading = false)
                } else {
                    _ui.value = _ui.value.copy(
                        records = emptyMap(), isLoading = false,
                        errorMsg = res.errorBody()?.string() ?: "Unknown error"
                    )
                }
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(
                    records = emptyMap(), isLoading = false, errorMsg = e.message
                )
            }
        }
    }
}