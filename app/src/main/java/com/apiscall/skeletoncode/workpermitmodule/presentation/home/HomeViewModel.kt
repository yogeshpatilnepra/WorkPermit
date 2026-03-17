package com.apiscall.skeletoncode.workpermitmodule.presentation.home


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitStatus
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitType
import com.apiscall.skeletoncode.workpermitmodule.domain.models.User
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.AuthRepository
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.PermitRepository
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val permitRepository: PermitRepository
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _dashboardStats = MutableStateFlow<Resource<DashboardStats>>(Resource.Loading)
    val dashboardStats: StateFlow<Resource<DashboardStats>> = _dashboardStats.asStateFlow()

    private val _recentPermits = MutableStateFlow<Resource<List<DashboardPermit>>>(Resource.Loading)
    val recentPermits: StateFlow<Resource<List<DashboardPermit>>> = _recentPermits.asStateFlow()

    init {
        loadUserData()
        loadDashboardData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _currentUser.value = authRepository.getCurrentUser()
        }
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            // Load stats
            permitRepository.getPermits().collect { permits ->
                val stats = DashboardStats(
                    totalPermits = permits.size,
                    pendingApprovals = permits.count {
                        it.status == PermitStatus.PENDING_ISSUER_APPROVAL ||
                                it.status == PermitStatus.PENDING_AREA_OWNER_APPROVAL ||
                                it.status == PermitStatus.PENDING_EHS_APPROVAL
                    },
                    activePermits = permits.count { it.status == PermitStatus.ACTIVE },
                    expiringToday = permits.count {
                        it.status == PermitStatus.ACTIVE &&
                                it.endDate.time - System.currentTimeMillis() < 86400000
                    }
                )
                _dashboardStats.value = Resource.Success(stats)
            }

            // Load recent permits
            permitRepository.getPermits().collect { permits ->
                val recent = permits
                    .sortedByDescending { it.updatedAt }
                    .take(5)
                    .map { permit ->
                        DashboardPermit(
                            id = permit.id,
                            permitNumber = permit.permitNumber,
                            title = permit.title,
                            status = permit.status,
                            type = permit.permitType,
                            location = permit.location
                        )
                    }
                _recentPermits.value = Resource.Success(recent)
            }
        }
    }

    fun refreshDashboard() {
        _dashboardStats.value = Resource.Loading
        _recentPermits.value = Resource.Loading
        loadDashboardData()
    }

    data class DashboardStats(
        val totalPermits: Int,
        val pendingApprovals: Int,
        val activePermits: Int,
        val expiringToday: Int
    )

    data class DashboardPermit(
        val id: String,
        val permitNumber: String,
        val title: String,
        val status: PermitStatus,
        val type: PermitType,
        val location: String
    )
}