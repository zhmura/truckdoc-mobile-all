package com.sanda.truckdoc.client.ui.locations

import com.sanda.truckdoc.client.data.MessagesDatabaseService
import com.sanda.truckdoc.client.data.model.DbLocation
import com.sanda.truckdoc.client.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationsViewModel @Inject constructor(
    private val databaseService: MessagesDatabaseService
) : BaseViewModel() {

    private val _locations = MutableStateFlow<List<DbLocation>>(emptyList())
    val locations: StateFlow<List<DbLocation>> = _locations

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadLocations()
    }

    private fun loadLocations() {
        launchWithLoading {
            _isLoading.value = true
            _error.value = null
            
            try {
                databaseService.getLocations()
                    .catch { e ->
                        _error.value = e.message ?: "Failed to load locations"
                    }
                    .collect { locations ->
                        _locations.value = locations
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load locations"
                _isLoading.value = false
            }
        }
    }

    fun refreshLocations() {
        loadLocations()
    }

    fun deleteLocations(locations: List<DbLocation>) {
        launchWithLoading {
            databaseService.deleteLocations(locations)
        }
    }
} 