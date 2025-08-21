package com.example.myot.ticket.book.model

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myot.retrofit2.AuthStore
import com.example.myot.retrofit2.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

class BookViewModel: ViewModel() {

    private val service = RetrofitClient.bookService
    val token = AuthStore.bearerOrThrow()

    private val _bookCovers = MutableLiveData<List<BookCover>>()
    val bookCovers: LiveData<List<BookCover>> = _bookCovers

    private val _bookIndexes = MutableLiveData<BookIndexData>()
    val bookIndexes: LiveData<BookIndexData> = _bookIndexes

    private val _bookDetails = MutableLiveData<BookDetailData>()
    val bookDetails: LiveData<BookDetailData> get() = _bookDetails

    private val _seatData = MutableLiveData<SeatData>()
    val seatData: LiveData<SeatData> get() = _seatData

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun setBookCover() {
        viewModelScope.launch {
            try {
                val response = service.getMyBookCovers(token)
                if (response.body() != null) {
                    _bookCovers.value = response.body()!!.success.data
                } else {
                    _error.value = "응답 실패: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "오류: ${e.message}"
            }
        }
    }

    fun setBookIndex(musicalId: Int) {
        viewModelScope.launch {
            try {
                val response = service.getMyBookSesons(token, musicalId)
                if (response.isSuccessful && response.body() != null) {
                    _bookIndexes.value = response.body()!!.success.data
                } else {
                    _error.value = "응답 실패: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "오류: ${e.message}"
            }
        }
    }

    fun fetchTicketBookDetail(musicalId: Int) {
        viewModelScope.launch {
            try {
                val response = service.getTicketBookCount(token, musicalId)

                Log.d("BookViewModel", "📡 response: $response")
                Log.d("BookViewModel", "📡 response body: ${response.body()}")

                if (response.isSuccessful && response.body()?.success != null) {
                    _bookDetails.value = response.body()!!.success!!.data
                } else {
                    _error.postValue("데이터를 불러오지 못했습니다.")
                }
            } catch (e: Exception) {
                _error.postValue(e.message)
            }
        }
    }
    @Volatile private var useAssetsForSeats: Boolean = false
    fun enableSeatAssetsMode(enable: Boolean) { useAssetsForSeats = enable }

    fun fetchSeatData(context: android.content.Context, musicalId: Int) {
        viewModelScope.launch {
            if (useAssetsForSeats) {
                val seats = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    loadHighlightedSeatsFromAssets(context)
                }
                // theaterId는 임시값(예: 1 = 세종대극장). 필요 시 매핑 바꾸세요.
                _seatData.value = SeatData(theaterId = 1, seats = seats)
                return@launch
            }
            try {
                val response = service.getSeatData(token, musicalId)
                if (response.isSuccessful && response.body()?.success != null) {
                    val data = response.body()!!.success!!.data
                    // seats가 비어있으면 assets 폴백
                    val seats = data.seats.takeIf { it.isNotEmpty() }
                        ?: withContext(Dispatchers.IO) { loadHighlightedSeatsFromAssets(context) }
                    _seatData.value = data.copy(seats = seats)
                } else {
                    // API 실패 → assets 폴백
                    val fallback = withContext(Dispatchers.IO) { loadHighlightedSeatsFromAssets(context) }
                    if (fallback.isNotEmpty()) {
                        _seatData.value = SeatData(theaterId = -1, seats = fallback)
                    } else {
                        _error.postValue("데이터를 불러오지 못했습니다.")
                    }
                }
            } catch (e: Exception) {
                // 네트워크 예외 → assets 폴백
                val fallback = withContext(Dispatchers.IO) { loadHighlightedSeatsFromAssets(context) }
                if (fallback.isNotEmpty()) {
                    _seatData.value = SeatData(theaterId = -1, seats = fallback)
                } else {
                    _error.postValue(e.message ?: "알 수 없는 오류")
                }
            }
        }
    }

    fun loadHighlightedSeatsFromAssets(
        context: Context,
        fileName: String = "userSeatInfo.json"
    ): List<SeatHighlightInfo> {
        val highlightList = mutableListOf<SeatHighlightInfo>()
        return try {
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                highlightList += SeatHighlightInfo(
                    floor = item.getInt("floor"),
                    zone = item.getString("zone"),
                    blockNumber = item.getInt("blockNumber"),
                    rowNumber = item.getInt("rowNumber"),
                    seatIndex = item.getInt("seatIndex"),
                    numberOfSittings = item.getInt("numberOfSittings")
                )
            }
            highlightList
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}