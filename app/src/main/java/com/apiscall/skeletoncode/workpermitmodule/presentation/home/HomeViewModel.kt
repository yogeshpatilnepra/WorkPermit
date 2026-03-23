package com.apiscall.skeletoncode.workpermitmodule.presentation.home


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apiscall.skeletoncode.workpermitmodule.data.repository.FirebaseRepository
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitStatus
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitType
import com.apiscall.skeletoncode.workpermitmodule.domain.models.User
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.AuthRepository
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firebaseRepository: FirebaseRepository
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
            firebaseRepository.getPermitsFlow().collect { permits ->
                val now = System.currentTimeMillis()

                val stats = DashboardStats(
                    totalPermits = permits.size,
                    pendingApprovals = permits.count {
                        it.approvalStage == "issuer_review" ||
                                it.approvalStage == "ehs_review" ||
                                it.approvalStage == "area_owner_review"
                    },
                    activePermits = permits.count { it.status == "active" || it.approvalStage == "issued" },
                    expiringToday = permits.count {
                        (it.status == "active" || it.approvalStage == "issued") &&
                                (it.workEnd?.toDate()?.time?.minus(now) ?: 0) < 86400000 &&
                                (it.workEnd?.toDate()?.time ?: 0) > now
                    }
                )
                _dashboardStats.value = Resource.Success(stats)

                // Recent permits (last 5 by createdAt)
                val recent = permits
                    .sortedByDescending { it.createdAt?.toDate() ?: Date() }
                    .take(5)
                    .map { permit ->
                        DashboardPermit(
                            id = permit.id,
                            permitNumber = permit.permitNumber,
                            title = permit.title,
                            status = getPermitStatusFromString(permit.status),
                            type = getPermitTypeFromString(permit.permitType),
                            location = permit.area
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

    private fun getPermitTypeFromString(type: String): PermitType {
        return when (type.lowercase()) {
            "hot work" -> PermitType.HOT_WORK
            "cold work" -> PermitType.COLD_WORK
            "loto" -> PermitType.LOTO
            "confined space" -> PermitType.CONFINED_SPACE
            "working at height" -> PermitType.WORK_AT_HEIGHT
            "lifting" -> PermitType.LIFTING
            "live equipment" -> PermitType.LIVE_EQUIPMENT
            else -> PermitType.HOT_WORK
        }
    }

    private fun getPermitStatusFromString(status: String): PermitStatus {
        return when (status.lowercase()) {
            "draft" -> PermitStatus.DRAFT
            "submitted", "issuer_review" -> PermitStatus.PENDING_ISSUER_APPROVAL
            "ehs_review" -> PermitStatus.PENDING_EHS_APPROVAL
            "area_owner_review" -> PermitStatus.PENDING_AREA_OWNER_APPROVAL
            "issued" -> PermitStatus.APPROVED
            "active" -> PermitStatus.ACTIVE
            "closed" -> PermitStatus.CLOSED
            "rejected" -> PermitStatus.REJECTED
            "sent_back" -> PermitStatus.SENT_BACK
            else -> PermitStatus.DRAFT
        }
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