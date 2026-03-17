package com.apiscall.skeletoncode.workpermitmodule.data.repository


import com.apiscall.skeletoncode.workpermitmodule.data.local.datasource.MockDataSource
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Role
import com.apiscall.skeletoncode.workpermitmodule.domain.models.User
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val mockDataSource: MockDataSource
) : UserRepository {

    override suspend fun getUsers(): Flow<List<User>> = flow {
        delay(300)
        emit(mockDataSource.getUsers())
    }

    override suspend fun getUserById(userId: String): Flow<User?> = flow {
        delay(200)
        emit(mockDataSource.getUserById(userId))
    }

    override suspend fun getUsersByRole(role: String): Flow<List<User>> = flow {
        delay(300)
        val roleEnum = Role.valueOf(role)
        emit(mockDataSource.getUsers().filter { it.role == roleEnum })
    }

    override suspend fun updateUserProfile(user: User): Result<User> {
        return try {
            // In a real implementation, this would update the user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchUsers(query: String): Flow<List<User>> = flow {
        delay(300)
        emit(mockDataSource.getUsers().filter {
            it.fullName.contains(query, ignoreCase = true) ||
                    it.username.contains(query, ignoreCase = true) ||
                    it.email.contains(query, ignoreCase = true)
        })
    }
}