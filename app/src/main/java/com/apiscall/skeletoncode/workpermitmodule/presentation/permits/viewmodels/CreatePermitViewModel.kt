package com.apiscall.skeletoncode.workpermitmodule.presentation.permits.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apiscall.skeletoncode.workpermitmodule.data.repository.FirebaseRepository
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitModel
import com.apiscall.skeletoncode.workpermitmodule.domain.models.User
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.AuthRepository
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreatePermitViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository, private val authRepository: AuthRepository
) : ViewModel() {

    private val _createResult = MutableStateFlow<Resource<PermitModel>>(Resource.Idle)
    val createResult: StateFlow<Resource<PermitModel>> = _createResult.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _currentUser.value = authRepository.getCurrentUser()
        }
    }

    fun createPermit(permit: PermitModel) {
        viewModelScope.launch {
            _createResult.value = Resource.Loading

            // Set the approval stage to "issuer_review" so it appears in Issuer's pending approvals
            val permitWithStage = permit.copy(
                approvalStage = "issuer_review", status = "submitted"
            )

            val result = firebaseRepository.createPermit(permitWithStage)

            _createResult.value = if (result.isSuccess) {
                Resource.Success(result.getOrNull()!!)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Failed to create permit")
            }
        }
    }

    fun resetState() {
        _createResult.value = Resource.Idle
    }
}