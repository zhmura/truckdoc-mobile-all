package com.sanda.truckdoc.client.ui.routes

import com.sanda.truckdoc.client.data.MessagesDatabaseService
import com.sanda.truckdoc.client.data.model.route.DbRouteAssignment
import com.sanda.truckdoc.client.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutesViewModel @Inject constructor(
    private val databaseService: MessagesDatabaseService
) : BaseViewModel() {

    private val _routes = MutableStateFlow<List<DbRouteAssignment>>(emptyList())
    val routes: StateFlow<List<DbRouteAssignment>> = _routes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadRoutes()
    }

    private fun loadRoutes() {
        launchWithLoading {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Note: This would need to be implemented in MessagesDatabaseService
                // For now, we'll use an empty list
                _routes.value = emptyList()
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load routes"
                _isLoading.value = false
            }
        }
    }

    fun refreshRoutes() {
        loadRoutes()
    }

    fun deleteAssignments() {
        launchWithLoading {
            databaseService.deleteAssignments()
        }
    }

    fun getRouteAssignmentById(id: Long): Flow<DbRouteAssignment?> {
        return databaseService.findRouteAssignmentById(id)
    }
} 