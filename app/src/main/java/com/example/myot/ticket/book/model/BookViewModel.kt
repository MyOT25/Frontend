package com.example.myot.ticket.book.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myot.retrofit2.AuthStore
import com.example.myot.retrofit2.RetrofitClient
import kotlinx.coroutines.launch

class BookViewModel: ViewModel() {

    private val service = RetrofitClient.bookService
    val token = AuthStore.bearerOrThrow()

    private val _bookCovers = MutableLiveData<List<BookCover>>()
    val bookCovers: LiveData<List<BookCover>> = _bookCovers

    private val _bookIndexes = MutableLiveData<BookIndexData>()
    val bookIndexes: LiveData<BookIndexData> = _bookIndexes

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
}