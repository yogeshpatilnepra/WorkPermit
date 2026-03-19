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

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isPasswordVisible = MutableStateFlow(false)
    val isPasswordVisible: StateFlow<Boolean> = _isPasswordVisible.asStateFlow()

    fun onEmailChanged(email: String) {
        _email.value = email
    }

    fun onPasswordChanged(password: String) {
        _password.value = password
    }

    fun togglePasswordVisibility() {
        _isPasswordVisible.value = !_isPasswordVisible.value
    }

    fun login() {
        if (!validateInputs()) {
            _loginState.value = Resource.Error("Please enter email and password")
            return
        }

        viewModelScope.launch {
            _loginState.value = Resource.Loading
            val result = authRepository.login(_email.value, _password.value)

            _loginState.value = if (result.isSuccess) {
                Resource.Success(true)
            } else {
                Resource.Error(
                    result.exceptionOrNull()?.message ?: "Login failed. Invalid credentials."
                )
            }
        }
    }

    private fun validateInputs(): Boolean {
        return _email.value.isNotBlank() && _password.value.isNotBlank()
    }

    fun resetState() {
        _loginState.value = Resource.Idle
    }
}