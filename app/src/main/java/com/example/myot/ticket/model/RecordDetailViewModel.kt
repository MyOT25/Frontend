package com.example.myot.ticket.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myot.retrofit2.AuthStore
import com.example.myot.retrofit2.TicketService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordDetailViewModel @Inject constructor(
    private val api: TicketService
) : ViewModel() {

    private val _state = MutableStateFlow<RecordUiState>(RecordUiState.Loading)
    val state = _state.asStateFlow()

    val token = AuthStore.bearerOrThrow()

    fun load(postId: Int) {
        _state.value = RecordUiState.Loading
        viewModelScope.launch {
            runCatching { api.getViewingRecord(token, postId) }
                .onSuccess { env ->
                    if (env.resultType == "SUCCESS" && env.success != null) {
                        _state.value = RecordUiState.Success(env.success.data)
                    } else {
                        _state.value = RecordUiState.Error("관극 기록 조회 실패")
                    }
                }
                .onFailure { _state.value = RecordUiState.Error(it.message ?: "오류") }
        }
    }
}