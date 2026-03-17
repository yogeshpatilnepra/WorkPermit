package com.apiscall.skeletoncode.workpermitmodule.presentation.notifications


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Notification
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.NotificationRepository
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<Resource<List<Notification>>>(Resource.Loading)
    val notifications: StateFlow<Resource<List<Notification>>> = _notifications.asStateFlow()

    private val _markAllResult = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val markAllResult: StateFlow<Resource<Boolean>> = _markAllResult.asStateFlow()

    fun loadNotifications() {
        viewModelScope.launch {
            _notifications.value = Resource.Loading
            notificationRepository.getNotifications().collect { notificationList ->
                _notifications.value = Resource.Success(notificationList)
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
            loadNotifications() // Refresh list
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            _markAllResult.value = Resource.Loading
            val result = notificationRepository.markAllAsRead()
            _markAllResult.value = if (result.isSuccess) {
                loadNotifications() // Refresh list
                Resource.Success(true)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Failed to mark all as read")
            }
        }
    }
}