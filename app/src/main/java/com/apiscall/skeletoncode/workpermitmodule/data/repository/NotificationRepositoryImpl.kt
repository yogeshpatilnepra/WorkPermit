package com.apiscall.skeletoncode.workpermitmodule.data.repository


import com.apiscall.skeletoncode.workpermitmodule.data.local.datasource.MockDataSource
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Notification
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.NotificationRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val mockDataSource: MockDataSource
) : NotificationRepository {

    override suspend fun getNotifications(): Flow<List<Notification>> = flow {
        delay(300)
        emit(mockDataSource.getNotifications())
    }

    override suspend fun getUnreadCount(): Flow<Int> = flow {
        delay(200)
        emit(mockDataSource.getUnreadNotifications().size)
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            mockDataSource.markNotificationAsRead(notificationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAllAsRead(): Result<Unit> {
        return try {
            mockDataSource.getNotifications().forEach { notification ->
                if (!notification.isRead) {
                    mockDataSource.markNotificationAsRead(notification.id)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            // Implementation for deletion
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}