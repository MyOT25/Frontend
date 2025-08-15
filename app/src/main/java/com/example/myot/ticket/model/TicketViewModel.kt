package com.example.myot.ticket.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myot.retrofit2.RetrofitClient
import com.example.myot.retrofit2.TicketService
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

import java.io.File

class TicketViewModel(private val service: TicketService): ViewModel() {
    private val _searchedMusical = MutableLiveData<Musical?>()
    val searchedMusical: LiveData<Musical?> get() = _searchedMusical

    private val _recordSaved = MutableLiveData<Boolean>()
    val recordSaved: LiveData<Boolean> get() = _recordSaved

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun searchMusical(name: String) {
        viewModelScope.launch {
            try {
                val res = service.searchMusical(name)
                if (res.isSuccessful) {
                    val data = res.body()?.success?.data?.firstOrNull()
                    _searchedMusical.value = data
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun postRecord(
        musicalId: Int,
        watchDate: String,
        watchTime: String,
        seatJson: String,
        castsJson: String,
        content: String,
        rating: Float,
        imagePaths: List<String>
    ) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.ticketService.postRecord(
                    musicalId.toRequestBody(),
                    watchDate.toRequestBody(),
                    watchTime.toRequestBody(),
                    seatJson.toRequestBody(),
                    castsJson.toRequestBody(),
                    content.toRequestBody(),
                    rating.toString().toRequestBody(),
                    imagePaths.toMultipartBodyParts()
                )

                if (response.isSuccessful && response.body()?.resultType == "SUCCESS") {
                        _recordSaved.value = true
                }  else {
                    _recordSaved.value = false
                    _error.value = "응답 실패: ${response.body()?.error}"
                }
            } catch (e: Exception) {
                _recordSaved.value = false
                _error.value = "오류: ${e.message}"
            }
        }
    }

    private fun String.toRequestBody(): RequestBody =
        this.toRequestBody("text/plain".toMediaType())

    private fun Int.toRequestBody(): RequestBody =
        this.toString().toRequestBody("text/plain".toMediaType())

    private fun List<String>.toMultipartBodyParts(): List<MultipartBody.Part> {
        return this.map { path ->
            val file = File(path)
            val requestFile = file.asRequestBody("image/*".toMediaType())
            MultipartBody.Part.createFormData("imageFiles", file.name, requestFile)
        }
    }
}