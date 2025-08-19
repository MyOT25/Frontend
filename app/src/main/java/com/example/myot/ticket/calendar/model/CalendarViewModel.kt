package com.example.myot.ticket.calendar.model

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myot.retrofit2.AuthStore
import com.example.myot.retrofit2.RetrofitClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(): ViewModel() {

    private val service = RetrofitClient.calendarService // 또는 적절한 서비스명
    val token = AuthStore.bearerOrThrow()

    // Calendar 인스턴스를 한 번만 생성하여 재사용
    private val calendar = Calendar.getInstance().apply {
        // 시간대 및 로케일 명시적 설정 (필요시)
        timeZone = TimeZone.getDefault()
        // firstDayOfWeek = Calendar.SUNDAY // 주의 시작을 일요일로 설정
    }

    // 현재 날짜 정보를 Calendar에서 가져와서 초기화
    private val _currentYear = MutableStateFlow(calendar.get(Calendar.YEAR))
    val currentYear: StateFlow<Int> = _currentYear

    private val _currentMonth = MutableStateFlow(calendar.get(Calendar.MONTH))
    val currentMonth: StateFlow<Int> = _currentMonth

    // 달력 데이터
    private val _calendarData = MutableStateFlow<Map<String, List<CalendarEntry>>>(emptyMap())
    val calendarData: StateFlow<Map<String, List<CalendarEntry>>> = _calendarData

    // 달력 일자들
    private val _calendarDays = MutableStateFlow<List<CalendarDay>>(emptyList())
    val calendarDays: StateFlow<List<CalendarDay>> = _calendarDays

    // 로딩 상태
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // 에러 메시지
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // 성공 메시지
    private val _successMessage = MutableLiveData<String>()
    val successMessage: LiveData<String> = _successMessage

    // 기록 저장 상태
    private val _recordSaved = MutableLiveData<Boolean>()
    val recordSaved: LiveData<Boolean> get() = _recordSaved

    init {
        initializeCalendar()
    }

    private fun initializeCalendar() {
        // Calendar 인스턴스 초기화 및 검증
        validateCalendarInstance()
        generateCalendarDays()
        loadCalendarData()
    }

    /**
     * Calendar 인스턴스가 제대로 초기화되었는지 검증
     */
    private fun validateCalendarInstance() {
        try {
            // Calendar가 제대로 초기화되었는지 확인
            val testYear = calendar.get(Calendar.YEAR)
            val testMonth = calendar.get(Calendar.MONTH)

            // 유효한 범위인지 검증
            if (testYear < 1900 || testYear > 2100) {
                throw IllegalStateException("Invalid year: $testYear")
            }

            if (testMonth < 0 || testMonth > 11) {
                throw IllegalStateException("Invalid month: $testMonth")
            }

            println("Calendar initialized successfully - Year: $testYear, Month: ${testMonth + 1}")

        } catch (e: Exception) {
            _error.value = "Calendar 초기화 실패: ${e.message}"
            e.printStackTrace()
        }
    }

    fun navigateMonth(direction: Int) {
        val year = _currentYear.value
        val month = _currentMonth.value

        var newMonth = month + direction
        var newYear = year

        if (newMonth > 11) {
            newMonth = 0
            newYear++
        } else if (newMonth < 0) {
            newMonth = 11
            newYear--
        }

        _currentYear.value = newYear
        _currentMonth.value = newMonth

        generateCalendarDays()
        loadCalendarData()
    }

    fun getMonthTitle(): String {
        return "${_currentMonth.value + 1}월"
    }

    private fun generateCalendarDays() {
        val year = _currentYear.value
        val month = _currentMonth.value
        val data = _calendarData.value

        val days = mutableListOf<CalendarDay>()

        // 요일 헤더 추가
        val dayHeaders = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        dayHeaders.forEach { header ->
            days.add(CalendarDay(header, true, false, emptyList()))
        }

        // 해당 월의 첫 번째 날
        calendar.set(year, month, 1)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // 이전 달의 빈 공간
        repeat(firstDayOfWeek) {
            days.add(CalendarDay("", false, false, emptyList()))
        }

        // 현재 달의 날짜들
        for (day in 1..daysInMonth) {
            val dateKey = formatDate(year, month, day)
            val dayData = data[dateKey] ?: emptyList()
            val isToday = isToday(year, month, day)
            days.add(CalendarDay(day.toString(), false, isToday, dayData))
        }

        _calendarDays.value = days
    }

    private fun isToday(year: Int, month: Int, day: Int): Boolean {
        return try {
            // 새로운 Calendar 인스턴스로 오늘 날짜 확인
            val today = Calendar.getInstance().apply {
                // 현재 시스템 시간으로 설정
                timeInMillis = System.currentTimeMillis()
            }

            today.get(Calendar.YEAR) == year &&
                    today.get(Calendar.MONTH) == month &&
                    today.get(Calendar.DAY_OF_MONTH) == day
        } catch (e: Exception) {
            // Calendar 관련 오류 발생시 false 반환
            e.printStackTrace()
            false
        }
    }

    /**
     * 특정 날짜의 Calendar 인스턴스 생성
     */
    private fun createCalendarForDate(year: Int, month: Int, day: Int): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            // 시간 필드 초기화
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    /**
     * 날짜 포맷팅 유틸리티
     */
    private fun formatDate(year: Int, month: Int, day: Int): String {
        return String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, day)
    }

    private fun loadCalendarData() {
        val year = _currentYear.value
        val month = _currentMonth.value

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = service.getMonthlyRecords(
                    token,
                    year = year,
                    month = month + 1
                )

                if (response.isSuccessful) {
                    val records = response.body()?.success?.data //?: emptyMap()
                    _calendarData.value = records as Map<String, List<CalendarEntry>>   // TODO: 여기 다시 확인
                    generateCalendarDays()
                } else {
                    _error.value = "데이터 로딩 실패: ${response.message()}"
                    _calendarData.value = emptyMap()
                    generateCalendarDays()
                }
            } catch (e: Exception) {
                _error.value = "네트워크 오류: ${e.message}"
                _calendarData.value = emptyMap()
                generateCalendarDays()
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getDayEntries(date: String): List<CalendarEntry> {
        val year = _currentYear.value
        val month = _currentMonth.value

        if (date.isEmpty()) return emptyList()

        val dateKey = formatDate(year, month, date.toIntOrNull() ?: return emptyList())
        return _calendarData.value[dateKey] ?: emptyList()
    }

    fun refreshData() {
        loadCalendarData()
    }

    /**
     * 특정 날짜로 이동
     */
    fun navigateToDate(year: Int, month: Int) {
        if (year < 1900 || year > 2100 || month < 0 || month > 11) {
            _error.value = "잘못된 날짜입니다."
            return
        }

        _currentYear.value = year
        _currentMonth.value = month
        generateCalendarDays()
        loadCalendarData()
    }

    /**
     * 오늘 날짜로 이동
     */
    fun navigateToToday() {
        val today = Calendar.getInstance()
        navigateToDate(
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH)
        )
    }

    // 메시지 클리어 함수들
    fun clearErrorMessage() {
        _error.value = ""
    }

    fun clearSuccessMessage() {
        _successMessage.value = ""
    }
}