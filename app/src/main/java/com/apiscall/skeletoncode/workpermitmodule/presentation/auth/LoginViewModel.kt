package com.apiscall.skeletoncode.workpermitmodule.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.AuthRepository
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val loginState: StateFlow<Resource<Boolean>> = _loginState.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isPasswordVisible = MutableStateFlow(false)
    val isPasswordVisible: StateFlow<Boolean> = _isPasswordVisible.asStateFlow()

    fun onUsernameChanged(username: String) {
        _username.value = username
    }

    fun onPasswordChanged(password: String) {
        _password.value = password
    }

    fun togglePasswordVisibility() {
        _isPasswordVisible.value = !_isPasswordVisible.value
    }

    fun login() {
        if (!validateInputs()) {
            _loginState.value = Resource.Error("Please fill in all fields")
            return
        }

        viewModelScope.launch {
            _loginState.value = Resource.Loading
            val result = authRepository.login(_username.value, _password.value)

            _loginState.value = if (result.isSuccess) {
                Resource.Success(true)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    private fun validateInputs(): Boolean {
        return _username.value.isNotBlank() && _password.value.isNotBlank()
    }

    fun resetState() {
        _loginState.value = Resource.Idle
    }
}