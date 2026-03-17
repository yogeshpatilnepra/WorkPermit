package com.apiscall.skeletoncode.workpermitmodule.domain.repository

import com.apiscall.skeletoncode.workpermitmodule.domain.models.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun getNotifications(): Flow<List<Notification>>
    suspend fun getUnreadCount(): Flow<Int>
    suspend fun markAsRead(notificationId: String): Result<Unit>
    suspend fun markAllAsRead(): Result<Unit>
    suspend fun deleteNotification(notificationId: String): Result<Unit>
}