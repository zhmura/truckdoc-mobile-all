package com.sanda.truckdoc.client.ui.contacts

import com.sanda.truckdoc.client.data.MessagesDatabaseService
import com.sanda.truckdoc.client.data.model.DbContactRecord
import com.sanda.truckdoc.client.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val databaseService: MessagesDatabaseService
) : BaseViewModel() {

    private val _contacts = MutableStateFlow<List<DbContactRecord>>(emptyList())
    val contacts: StateFlow<List<DbContactRecord>> = _contacts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadContacts()
    }

    private fun loadContacts() {
        launchWithLoading {
            _isLoading.value = true
            _error.value = null
            
            try {
                databaseService.getContactRecords()
                    .catch { e ->
                        _error.value = e.message ?: "Failed to load contacts"
                    }
                    .collect { contacts ->
                        _contacts.value = contacts
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load contacts"
                _isLoading.value = false
            }
        }
    }

    fun refreshContacts() {
        loadContacts()
    }

    fun updateContact(contact: DbContactRecord) {
        launchWithLoading {
            databaseService.updateContactRecord(contact)
        }
    }

    fun replaceContacts(contacts: List<DbContactRecord>) {
        launchWithLoading {
            databaseService.replaceContactRecords(contacts)
        }
    }
} 