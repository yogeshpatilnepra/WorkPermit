package com.apiscall.skeletoncode.workpermitmodule.presentation.permits.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Permit
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.AuthRepository
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.PermitRepository
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermitListViewModel @Inject constructor(
    private val permitRepository: PermitRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _permits = MutableStateFlow<Resource<List<Permit>>>(Resource.Idle)
    val permits: StateFlow<Resource<List<Permit>>> = _permits.asStateFlow()

    fun loadMyPermits() {
        viewModelScope.launch {
            _permits.value = Resource.Loading
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                permitRepository.getMyPermits(currentUser.id).collect { permits ->
                    _permits.value = Resource.Success(permits)
                }
            } else {
                _permits.value = Resource.Error("User not logged in")
            }
        }
    }

    fun loadPendingApprovals() {
        viewModelScope.launch {
            _permits.value = Resource.Loading
            permitRepository.getPendingApprovals().collect { permits ->
                _permits.value = Resource.Success(permits)
            }
        }
    }

    fun loadActivePermits() {
        viewModelScope.launch {
            _permits.value = Resource.Loading
            permitRepository.getActivePermits().collect { permits ->
                _permits.value = Resource.Success(permits)
            }
        }
    }
}