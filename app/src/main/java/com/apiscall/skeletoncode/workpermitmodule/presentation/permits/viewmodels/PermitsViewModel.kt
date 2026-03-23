package com.apiscall.skeletoncode.workpermitmodule.presentation.permits.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apiscall.skeletoncode.workpermitmodule.data.repository.FirebaseRepository
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Permit
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitModel
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitStatus
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitType
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Role
import com.apiscall.skeletoncode.workpermitmodule.domain.models.User
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.AuthRepository
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class PermitsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _permits = MutableStateFlow<Resource<List<Permit>>>(Resource.Loading)
    val permits: StateFlow<Resource<List<Permit>>> = _permits.asStateFlow()

    // Filter states
    private val _selectedPlant = MutableStateFlow("All Plants")
    private val _selectedStatus = MutableStateFlow("all")
    private val _selectedType = MutableStateFlow("All Types")
    private val _searchQuery = MutableStateFlow("")

    init {
        loadCurrentUser()
        setupPermitsListener()
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            _currentUser.value = authRepository.getCurrentUser()
        }
    }

    private fun setupPermitsListener() {
        viewModelScope.launch {
            combine(
                _selectedPlant,
                _selectedStatus,
                _selectedType,
                _searchQuery
            ) { plant, status, type, query ->
                Triple(plant, status, type) to query
            }.collect { (filters, query) ->
                val (plant, status, type) = filters

                _permits.value = Resource.Loading

                // Get all permits first, then apply filters locally
                firebaseRepository.getPermitsFlow().collect { permitModels ->
                    var filtered = permitModels

                    // Apply plant filter
                    if (plant != "All Plants") {
                        filtered = filtered.filter { it.plant == plant }
                    }

                    // Apply status filter
                    if (status != "all") {
                        filtered = filtered.filter { it.status == status }
                    }

                    // Apply type filter
                    if (type != "All Types") {
                        filtered = filtered.filter { it.permitType == type }
                    }

                    // Apply search query
                    if (query.isNotEmpty()) {
                        filtered = filtered.filter {
                            it.permitNumber.contains(query, ignoreCase = true) ||
                                    it.title.contains(query, ignoreCase = true)
                        }
                    }

                    val permits = filtered.map { convertToPermit(it) }
                    _permits.value = Resource.Success(permits)
                }
            }
        }
    }

    fun loadPermits() {
        // Trigger reload by updating filters
        _selectedPlant.value = _selectedPlant.value
    }

    fun updatePlantFilter(plant: String) {
        _selectedPlant.value = plant
    }

    fun updateStatusFilter(status: String) {
        _selectedStatus.value = status
    }

    fun updateTypeFilter(type: String) {
        _selectedType.value = type
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearFilters() {
        _selectedPlant.value = "All Plants"
        _selectedStatus.value = "all"
        _selectedType.value = "All Types"
        _searchQuery.value = ""
    }

    fun canCreatePermit(): Boolean {
        val user = _currentUser.value ?: return false
        return when (user.role) {
            Role.ADMIN, Role.SUPERVISOR, Role.REQUESTOR -> true
            else -> false
        }
    }

    private fun convertToPermit(model: PermitModel): Permit {
        val requester = User(
            id = model.requestorId,
            username = "",
            email = model.requestorEmail,
            fullName = model.requestorName,
            role = Role.REQUESTOR,
            department = model.department,
            employeeId = ""
        )

        val issuer = if (model.issuerId != null) {
            User(
                id = model.issuerId,
                username = "",
                email = "",
                fullName = model.issuerName ?: "",
                role = Role.ISSUER,
                department = "",
                employeeId = ""
            )
        } else null

        return Permit(
            id = model.id,
            permitNumber = model.permitNumber,
            permitType = getPermitTypeFromString(model.permitType),
            title = model.title,
            description = model.jobDescription,
            status = getPermitStatusFromString(model.status),
            requester = requester,
            issuer = issuer,
            location = model.area,
            plant = model.plant,
            department = model.department,
            area = model.area,
            company = model.company,
            shift = model.shift,
            workerCount = model.workerCount,
            startDate = model.workStart?.toDate() ?: Date(),
            endDate = model.workEnd?.toDate() ?: Date(),
            createdAt = model.createdAt?.toDate() ?: Date(),
            updatedAt = model.updatedAt?.toDate() ?: Date(),
            formData = "{}",
            riskAssessmentNo = model.riskAssessmentNo,
            jsaNo = model.jsaNo,
            toolboxTalkDone = model.toolboxTalkDone,
            ppeVerified = model.ppeVerified,
            approvalStage = model.approvalStage,
            issuerComments = model.issuerComments,
            ehsComments = model.ehsComments,
            areaOwnerComments = model.areaOwnerComments
        )
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
}