package com.apiscall.skeletoncode.workpermitmodule.presentation.permits.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apiscall.skeletoncode.workpermitmodule.data.repository.FirebaseRepository
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Permit
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

                firebaseRepository.getFilteredPermitsFlow(
                    plant = plant,
                    status = status,
                    type = type,
                    searchQuery = query
                ).collect { permitModels ->
                    // Convert PermitModel to Permit (your domain model)
                    val permits = permitModels.map { model ->
                        convertToPermit(model)
                    }
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

    private fun convertToPermit(model: com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitModel): Permit {
        // Create a mock user for the requester (you should fetch actual user from your user repository)
        val requester = User(
            id = model.requestorId,
            username = "",
            email = model.requestorEmail,
            fullName = model.requestorName,
            role = Role.REQUESTOR,
            department = "",
            employeeId = ""
        )

        return Permit(
            id = model.id,
            permitNumber = model.permitNumber,
            permitType = getPermitTypeFromString(model.permitType),
            title = model.title,
            description = model.jobDescription,
            status = getPermitStatusFromString(model.status),
            requester = requester,
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
            ppeVerified = model.ppeVerified
        )
    }

    private fun getPermitTypeFromString(type: String): com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitType {
        return when (type.lowercase()) {
            "hot work" -> com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitType.HOT_WORK
            "cold work" -> com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitType.COLD_WORK
            "loto" -> com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitType.LOTO
            "confined space" -> com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitType.CONFINED_SPACE
            "working at height" -> com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitType.WORK_AT_HEIGHT
            "lifting" -> com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitType.LIFTING
            "live equipment" -> com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitType.LIVE_EQUIPMENT
            else -> com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitType.HOT_WORK
        }
    }

    private fun getPermitStatusFromString(status: String): com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitStatus {
        return when (status.lowercase()) {
            "draft" -> com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitStatus.DRAFT
            "issuer review" -> com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitStatus.PENDING_ISSUER_APPROVAL
            "ehs review" -> com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitStatus.PENDING_EHS_APPROVAL
            "area owner review" -> com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitStatus.PENDING_AREA_OWNER_APPROVAL
            "issued" -> com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitStatus.APPROVED
            "in progress" -> com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitStatus.ACTIVE
            "closed" -> com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitStatus.CLOSED
            "rejected" -> com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitStatus.REJECTED
            "expired" -> com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitStatus.EXPIRED
            else -> com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitStatus.DRAFT
        }
    }
}