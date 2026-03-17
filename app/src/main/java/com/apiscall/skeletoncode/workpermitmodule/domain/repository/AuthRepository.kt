package com.apiscall.skeletoncode.workpermitmodule.domain.repository

import com.apiscall.skeletoncode.workpermitmodule.domain.models.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<User>
    suspend fun logout()
    suspend fun getCurrentUser(): User?
    fun isLoggedIn(): Boolean
    fun observeAuthState(): Flow<Boolean>
}