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

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            // Updated to use email instead of username
            val user = mockDataSource.getUserByEmail(email)
            // All users have password "123456789" as per your requirements
            if (user != null && password == "123456789") {
                currentUser = user
                _authState.value = true
                Result.success(user)
            } else {
                Result.failure(Exception("Invalid email or password"))
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