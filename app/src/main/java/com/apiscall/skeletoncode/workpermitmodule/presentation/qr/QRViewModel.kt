package com.apiscall.skeletoncode.workpermitmodule.presentation.qr


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QRViewModel @Inject constructor() : ViewModel() {

    private val _scanResult = MutableStateFlow<Resource<String>>(Resource.Idle)
    val scanResult: StateFlow<Resource<String>> = _scanResult.asStateFlow()

    fun simulateScan() {
        viewModelScope.launch {
            _scanResult.value = Resource.Loading
            delay(1500) // Simulate scanning
            // Return a valid permit ID that exists in mock data (e.g., "1")
            _scanResult.value = Resource.Success("1")
        }
    }
}