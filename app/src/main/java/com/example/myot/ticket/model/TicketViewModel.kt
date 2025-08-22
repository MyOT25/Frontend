package com.example.myot.ticket.model

import android.util.Log
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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

import java.io.File
import javax.inject.Inject

@HiltViewModel
class TicketViewModel@Inject constructor(): ViewModel() {

    private val service = RetrofitClient.ticketService
    val token = AuthStore.bearerOrThrow()

    private val _searchedMusical = MutableStateFlow<Musical?>(null)
    val searchedMusical: StateFlow<Musical?> = _searchedMusical

    private val _ticketToday = MutableLiveData<List<TicketToday>>()
    val ticketToday: LiveData<List<TicketToday>> get() = _ticketToday

    private val _recordSaved = MutableLiveData<Boolean>()
    val recordSaved: LiveData<Boolean> get() = _recordSaved

    private val _roles = MutableLiveData<List<Role>>()
    val roles: LiveData<List<Role>> = _roles

    private val _selectedActors = MutableLiveData<MutableList<Int>>(mutableListOf())
    val selectedActors: LiveData<MutableList<Int>> = _selectedActors

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _mandatoryActors = MutableLiveData<Set<Int>>(emptySet())
    val mandatoryActors: LiveData<Set<Int>> = _mandatoryActors

    fun setMandatoryActors(ids: Set<Int>) {
        _mandatoryActors.value = ids
        val merged = (_selectedActors.value?.toMutableSet() ?: mutableSetOf())
        merged.addAll(ids) // ✅ 항상 포함
        _selectedActors.value = merged.toMutableList()
    }


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
        if (_mandatoryActors.value?.contains(actorId) == true) return
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
        Log.d("Record Input", "${musicalId}, ${watchDate}, ${watchTime}, ${seat}, $casts, $content, $rating")
        viewModelScope.launch {
            try {
                val imageParts = imageFiles?.map { file ->
                    val reqFile = file.asImageRequestBody()
                    MultipartBody.Part.createFormData("imageFiles", file.name, reqFile)
                }

                val response = service.postViewingRecord(
                    token,
                    musicalId = musicalId.toPlain(),         // ✅ 모두 text/plain
                    watchDate  = watchDate.toPlain(),
                    watchTime  = watchTime.toPlain(),
                    seat       = seat.toPlain(),             // JSON 문자열이지만 text/plain로
                    casts      = casts.toPlain(),            // ✅ toJsonRequestBody() 금지!
                    content    = content.toPlain(),
                    rating     = rating.toPlain(),
                    imageFiles = imageParts
                )

                if (response.body()?.resultType == "SUCCESS") {
                    _recordSaved.value = true
                    Log.d("Record Response", response.body()?.success?.data.toString())
                } else {
                    _recordSaved.value = false
                    _error.value = "응답 실패: ${response.code()} / ${response.errorBody()?.string()}"
                    Log.d("Record Response", _error.value.toString())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _recordSaved.value = false
                _error.value = "오류: ${e.message}"
                Log.d("Record Response", _error.value.toString())
            }
        }
    }

    fun fetchLatestViewing() {
        viewModelScope.launch {
            try {
                val response = service.getLatestViewingRecord(token)
                if (response.isSuccessful) {
                    response.body()?.success?.data?.let { data ->
                        val fullActors = data.actors
                        val take3 = fullActors.take(1)
                        val castList = buildString {
                            append(take3.joinToString(", ") { it.name })
                            val extra = (fullActors.size - take3.size)
                            if (extra > 0) append(" 외 ${extra}명")
                        }

                        val ticket = TicketToday(
                            title = data.title,
                            theater = "${data.place} (${data.region.name})",
                            period = data.period,
                            cast = castList,          // ← 상위 3명(+외 N명)만 표시
                            avgRating = data.averageRating,
                            myRating = data.myRating,
                            posterUrl = data.poster
                        )

                        _ticketToday.postValue(listOf(ticket))
                    }
                } else {
                    _error.value = "응답 실패: ${response.code()}"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "오류: ${e.message}"
            }
        }
    }

    private fun String.toPlain(): RequestBody =
        this.toRequestBody("text/plain; charset=utf-8".toMediaType())

    private fun File.asImageRequestBody(): RequestBody {
        val mime = when (extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> "application/octet-stream"
        }.toMediaType()
        return asRequestBody(mime)
    }
}