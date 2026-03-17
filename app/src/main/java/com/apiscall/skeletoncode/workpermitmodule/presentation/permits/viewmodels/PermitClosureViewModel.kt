package com.apiscall.skeletoncode.workpermitmodule.presentation.permits.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.PermitRepository
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermitClosureViewModel @Inject constructor(
    private val permitRepository: PermitRepository
) : ViewModel() {

    private val _closureResult = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val closureResult: StateFlow<Resource<Boolean>> = _closureResult.asStateFlow()

    private var attachments = mutableListOf<String>()

    fun addMockAttachment() {
        attachments.add("attachment_${System.currentTimeMillis()}")
    }

    fun closePermit(permitId: String, remarks: String) {
        viewModelScope.launch {
            _closureResult.value = Resource.Loading
            val result = permitRepository.closePermit(permitId, remarks)
            _closureResult.value = if (result.isSuccess) {
                Resource.Success(true)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Failed to close permit")
            }
        }
    }
}