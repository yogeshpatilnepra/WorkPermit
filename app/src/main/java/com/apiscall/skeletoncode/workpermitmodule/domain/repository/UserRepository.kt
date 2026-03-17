package com.apiscall.skeletoncode.workpermitmodule.domain.repository

import com.apiscall.skeletoncode.workpermitmodule.domain.models.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getUsers(): Flow<List<User>>
    suspend fun getUserById(userId: String): Flow<User?>
    suspend fun getUsersByRole(role: String): Flow<List<User>>
    suspend fun updateUserProfile(user: User): Result<User>
    suspend fun searchUsers(query: String): Flow<List<User>>
}