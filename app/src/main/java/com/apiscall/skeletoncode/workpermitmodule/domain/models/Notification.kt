package com.apiscall.skeletoncode.workpermitmodule.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val permitId: String? = null,
    val isRead: Boolean = false,
    val createdAt: Date,
    val priority: NotificationPriority
) : Parcelable

enum class NotificationType {
    PERMIT_APPROVAL,
    PERMIT_REJECTION,
    PERMIT_EXPIRING,
    NEW_PERMIT_REQUEST,
    WORKER_SIGN_IN,
    WORKER_SIGN_OUT,
    SYSTEM_ALERT
}

enum class NotificationPriority {
    HIGH,
    MEDIUM,
    LOW
}