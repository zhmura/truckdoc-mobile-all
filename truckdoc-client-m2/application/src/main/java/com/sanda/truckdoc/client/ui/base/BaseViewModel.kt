package com.sanda.truckdoc.client.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {
    
    protected fun launchWithLoading(block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
        }
    }
} 