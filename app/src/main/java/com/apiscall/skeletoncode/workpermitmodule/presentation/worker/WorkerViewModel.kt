package com.apiscall.skeletoncode.workpermitmodule.presentation.worker


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apiscall.skeletoncode.workpermitmodule.domain.models.User
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.PermitRepository
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.UserRepository
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkerViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val permitRepository: PermitRepository
) : ViewModel() {

    private val _workers = MutableStateFlow<Resource<List<User>>>(Resource.Loading)
    val workers: StateFlow<Resource<List<User>>> = _workers.asStateFlow()

    private val _signInResult = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val signInResult: StateFlow<Resource<Boolean>> = _signInResult.asStateFlow()

    private val _signOutResult = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val signOutResult: StateFlow<Resource<Boolean>> = _signOutResult.asStateFlow()

    fun loadAvailableWorkers() {
        viewModelScope.launch {
            _workers.value = Resource.Loading
            userRepository.getUsersByRole("WORKER").collect { users ->
                _workers.value = Resource.Success(users)
            }
        }
    }

    fun loadSignedInWorkers(permitId: String) {
        viewModelScope.launch {
            _workers.value = Resource.Loading
            permitRepository.getPermitById(permitId).collect { permit ->
                if (permit != null) {
                    _workers.value = Resource.Success(permit.workers)
                } else {
                    _workers.value = Resource.Error("Permit not found")
                }
            }
        }
    }

    fun signInWorker(permitId: String, workerId: String) {
        viewModelScope.launch {
            _signInResult.value = Resource.Loading
            // In real app, this would update the permit
            kotlinx.coroutines.delay(1000)
            _signInResult.value = Resource.Success(true)
        }
    }

    fun signOutWorker(permitId: String, workerId: String) {
        viewModelScope.launch {
            _signOutResult.value = Resource.Loading
            // In real app, this would update the permit
            kotlinx.coroutines.delay(1000)
            _signOutResult.value = Resource.Success(true)
        }
    }
}