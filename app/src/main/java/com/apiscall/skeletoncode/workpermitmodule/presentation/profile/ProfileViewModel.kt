package com.apiscall.skeletoncode.workpermitmodule.presentation.profile


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<Resource<User>>(Resource.Loading)
    val userProfile: StateFlow<Resource<User>> = _userProfile.asStateFlow()

    private val _logoutResult = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val logoutResult: StateFlow<Resource<Boolean>> = _logoutResult.asStateFlow()

    fun loadUserProfile() {
        viewModelScope.launch {
            _userProfile.value = Resource.Loading
            val user = authRepository.getCurrentUser()
            if (user != null) {
                _userProfile.value = Resource.Success(user)
            } else {
                _userProfile.value = Resource.Error("User not logged in")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _logoutResult.value = Resource.Loading
            try {
                authRepository.logout()
                _logoutResult.value = Resource.Success(true)
            } catch (e: Exception) {
                _logoutResult.value = Resource.Error(e.message ?: "Logout failed")
            }
        }
    }
}