package com.apiscall.skeletoncode.workpermitmodule.presentation.qr


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import com.apiscall.skeletoncode.workpermitmodule.utils.Constants
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QRViewModel @Inject constructor() : ViewModel() {

    private val _scanResult = MutableStateFlow<Resource<String>>(Resource.Idle)
    val scanResult: StateFlow<Resource<String>> = _scanResult.asStateFlow()

    private var scannedPermitId: String? = null

    fun processScanResult(qrData: String) {
        viewModelScope.launch {
            _scanResult.value = Resource.Loading

            // Simulate processing
            kotlinx.coroutines.delay(500)

            if (qrData.startsWith(Constants.QR_CODE_PREFIX)) {
                val permitId = qrData.substringAfter(Constants.QR_CODE_PREFIX)
                scannedPermitId = permitId
                _scanResult.value = Resource.Success(permitId)
            } else {
                _scanResult.value = Resource.Error("Invalid QR code")
            }
        }
    }

    fun navigateToPermit(): NavDirections? {
        return scannedPermitId?.let {
            // Create navigation action to permit details
            null // This would be implemented with actual navigation
        }
    }
}