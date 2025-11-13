package com.sanda.truckdoc.client.ui.messages

import androidx.lifecycle.viewModelScope
import com.sanda.truckdoc.client.data.MessagesDatabaseService
import com.sanda.truckdoc.client.data.model.ServerMessage
import com.sanda.truckdoc.client.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val databaseService: MessagesDatabaseService
) : BaseViewModel() {

    private val _messages = MutableStateFlow<List<ServerMessage>>(emptyList())
    val messages: StateFlow<List<ServerMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadMessages()
    }

    private fun loadMessages() {
        launchWithLoading {
            _isLoading.value = true
            _error.value = null
            
            try {
                databaseService.getMessages(showHidden = false)
                    .catch { e ->
                        _error.value = e.message ?: "Failed to load messages"
                    }
                    .collect { messages ->
                        _messages.value = messages
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load messages"
                _isLoading.value = false
            }
        }
    }

    fun refreshMessages() {
        loadMessages()
    }

    private val _showHidden = MutableStateFlow(false)
    val showHidden: StateFlow<Boolean> = _showHidden.asStateFlow()

    val pendingOutMessages: StateFlow<List<ServerMessage>> = databaseService
        .getPendingOutMessages()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleShowHidden() {
        _showHidden.value = !_showHidden.value
    }

    fun markMessageAsHidden(message: ServerMessage) {
        launchWithLoading {
            databaseService.markHidden(message)
        }
    }

    fun deleteOldMessages() {
        launchWithLoading {
            databaseService.deleteOldMessages()
        }
    }

    fun deleteAllMessages() {
        launchWithLoading {
            databaseService.deleteAllMessages()
        }
    }
} 