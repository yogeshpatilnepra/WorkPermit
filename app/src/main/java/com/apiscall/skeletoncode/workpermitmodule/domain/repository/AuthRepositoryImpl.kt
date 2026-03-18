package com.apiscall.skeletoncode.workpermitmodule.domain.repository


import com.apiscall.skeletoncode.workpermitmodule.data.local.datasource.MockDataSource
import com.apiscall.skeletoncode.workpermitmodule.domain.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val mockDataSource: MockDataSource
) : AuthRepository {

    private var currentUser: User? = null
    private val _authState = MutableStateFlow(false)

    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            // Mock login validation
            val user = mockDataSource.getUserByUsername(username)
            // Accept "password123" for all demo users
            if (user != null && password == "password123") {
                currentUser = user
                _authState.value = true
                Result.success(user)
            } else {
                Result.failure(Exception("Invalid credentials"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        currentUser = null
        _authState.value = false
    }

    override suspend fun getCurrentUser(): User? = currentUser

    override fun isLoggedIn(): Boolean = currentUser != null

    override fun observeAuthState(): Flow<Boolean> = _authState.asStateFlow()
}