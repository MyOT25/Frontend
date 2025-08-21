package com.example.myot.ticket.model

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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

import java.io.File
import javax.inject.Inject

@HiltViewModel
class TicketViewModel@Inject constructor(): ViewModel() {

    private val service = RetrofitClient.ticketService
    val token = AuthStore.bearerOrThrow()

    private val _searchedMusical = MutableStateFlow<Musical?>(null)
    val searchedMusical: StateFlow<Musical?> = _searchedMusical

    private val _recordSaved = MutableLiveData<Boolean>()
    val recordSaved: LiveData<Boolean> get() = _recordSaved

    private val _roles = MutableLiveData<List<Role>>()
    val roles: LiveData<List<Role>> = _roles

    private val _selectedActors = MutableLiveData<MutableList<Int>>(mutableListOf())
    val selectedActors: LiveData<MutableList<Int>> = _selectedActors

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun setSearchedMusical(musical: Musical) {
        _searchedMusical.value = musical
    }

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

    fun loadCast(musicalId: Int) {
        viewModelScope.launch {
            try {
                val response = service.getCastList(token, musicalId)
                response.body()?.success?.data?.roles?.let {
                    _roles.postValue(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun selectActor(role: String, actorId: Int) {
        val current = _selectedActors.value ?: mutableListOf()

        // 같은 역할의 기존 선택 제거
        _roles.value?.find { it.role == role }?.actors?.forEach { actor ->
            current.remove(actor.actorId)
        }

        // 새 선택 추가
        current.add(actorId)
        _selectedActors.value = current
    }

    fun uploadViewingRecord(
        musicalId: String,
        watchDate: String,
        watchTime: String,
        seat: String,
        casts: String,
        content: String,
        rating: String,
        imageFiles: List<File>?
    ) {
        viewModelScope.launch {
            try {
                val response = service.postViewingRecord(
                    token,
                    musicalId.toRequestBody(),
                    watchDate.toRequestBody(),
                    watchTime.toRequestBody(),
                    seat.toRequestBody(),
                    casts.toJsonRequestBody(),
                    content.toRequestBody(),
                    rating.toRequestBody(),
                    imageFiles?.map { file ->
                        val reqFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                        MultipartBody.Part.createFormData("imageFiles", file.name, reqFile)
                    }
                )
                if (response.isSuccessful) {
                    _recordSaved.value = true
                } else {
                    _recordSaved.value = false
                    _error.value = "응답 실패: ${response.code()}"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _recordSaved.value = false
                _error.value = "오류: ${e.message}"
            }
        }
    }

    private fun String.toRequestBody(): RequestBody =
        RequestBody.create("text/plain".toMediaTypeOrNull(), this)

    private fun String.toJsonRequestBody(): RequestBody =
        RequestBody.create("application/json".toMediaTypeOrNull(), this)
}