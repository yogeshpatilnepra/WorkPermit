package com.apiscall.skeletoncode.workpermitmodule.presentation.permits.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Permit
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.PermitRepository
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermitDetailViewModel @Inject constructor(
    private val permitRepository: PermitRepository
) : ViewModel() {

    private val _permitDetails = MutableStateFlow<Resource<Permit>>(Resource.Loading)
    val permitDetails: StateFlow<Resource<Permit>> = _permitDetails.asStateFlow()

    private val _actionResult = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val actionResult: StateFlow<Resource<Boolean>> = _actionResult.asStateFlow()

    fun loadPermitDetails(permitId: String) {
        viewModelScope.launch {
            _permitDetails.value = Resource.Loading
            permitRepository.getPermitById(permitId).collect { permit ->
                if (permit != null) {
                    _permitDetails.value = Resource.Success(permit)
                } else {
                    _permitDetails.value = Resource.Error("Permit not found")
                }
            }
        }
    }

    fun approvePermit(permitId: String, comments: String?) {
        viewModelScope.launch {
            _actionResult.value = Resource.Loading
            val result = permitRepository.approvePermit(permitId, comments)
            _actionResult.value = if (result.isSuccess) {
                Resource.Success(true)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Approval failed")
            }
        }
    }

    fun rejectPermit(permitId: String, comments: String) {
        viewModelScope.launch {
            _actionResult.value = Resource.Loading
            val result = permitRepository.rejectPermit(permitId, comments)
            _actionResult.value = if (result.isSuccess) {
                Resource.Success(true)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Rejection failed")
            }
        }
    }

    fun sendBackPermit(permitId: String, comments: String) {
        viewModelScope.launch {
            _actionResult.value = Resource.Loading
            val result = permitRepository.sendBackPermit(permitId, comments)
            _actionResult.value = if (result.isSuccess) {
                Resource.Success(true)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Action failed")
            }
        }
    }
}