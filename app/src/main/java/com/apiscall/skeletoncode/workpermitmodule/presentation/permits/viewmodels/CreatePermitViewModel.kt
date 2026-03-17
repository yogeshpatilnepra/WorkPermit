package com.apiscall.skeletoncode.workpermitmodule.presentation.permits.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Permit
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitStatus
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitType
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.AuthRepository
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.PermitRepository
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreatePermitViewModel @Inject constructor(
    private val permitRepository: PermitRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private var permitType: PermitType? = null
    private var location: String? = null
    private var startDate: Long? = null
    private var endDate: Long? = null
    private var title: String? = null
    private var description: String? = null

    private val _createResult = MutableStateFlow<Resource<Permit>>(Resource.Idle)
    val createResult: StateFlow<Resource<Permit>> = _createResult.asStateFlow()

    fun setPermitType(type: PermitType) {
        this.permitType = type
    }

    fun setLocation(location: String) {
        this.location = location
    }

    fun setStartDate(timestamp: Long) {
        this.startDate = timestamp
    }

    fun setEndDate(timestamp: Long) {
        this.endDate = timestamp
    }

    fun createPermit(title: String, description: String, isDraft: Boolean) {
        this.title = title
        this.description = description

        viewModelScope.launch {
            _createResult.value = Resource.Loading

            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                _createResult.value = Resource.Error("User not logged in")
                return@launch
            }

            if (!validateInputs()) {
                _createResult.value = Resource.Error("Please fill all required fields")
                return@launch
            }

            val permit = Permit(
                id = UUID.randomUUID().toString(),
                permitNumber = "",
                permitType = permitType ?: PermitType.HOT_WORK,
                title = title,
                description = description,
                status = if (isDraft) PermitStatus.DRAFT else PermitStatus.PENDING_ISSUER_APPROVAL,
                requester = currentUser,
                location = location ?: "",
                startDate = Date(startDate ?: System.currentTimeMillis()),
                endDate = Date(endDate ?: System.currentTimeMillis() + 86400000),
                createdAt = Date(),
                updatedAt = Date(),
                formData = emptyMap(),
                isDraft = isDraft
            )

            val result = permitRepository.createPermit(permit)
            _createResult.value = if (result.isSuccess) {
                Resource.Success(result.getOrNull()!!)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Failed to create permit")
            }
        }
    }

    private fun validateInputs(): Boolean {
        return permitType != null && !location.isNullOrBlank() &&
                startDate != null && endDate != null &&
                !title.isNullOrBlank() && !description.isNullOrBlank()
    }
}