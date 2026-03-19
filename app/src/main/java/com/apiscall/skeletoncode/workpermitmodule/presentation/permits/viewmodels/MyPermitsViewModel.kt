package com.apiscall.skeletoncode.workpermitmodule.presentation.permits.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apiscall.skeletoncode.workpermitmodule.data.repository.FirebaseRepository
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Permit
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitModel
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Role
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
class MyPermitsViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _permits = MutableStateFlow<Resource<List<Permit>>>(Resource.Loading)
    val permits: StateFlow<Resource<List<Permit>>> = _permits.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _currentUser.value = authRepository.getCurrentUser()
        }
    }

    fun loadMyPermits() {
        viewModelScope.launch {
            _permits.value = Resource.Loading

            val user = _currentUser.value
            if (user == null) {
                _permits.value = Resource.Error("User not logged in")
                return@launch
            }

            // For EHS, Issuer, Area Owner - show all permits
            // For Requestor, Supervisor, Admin - show only their permits
            firebaseRepository.getPermitsFlow().collect { permitModels ->
                val filteredPermits = when (user.role) {
                    Role.EHS_OFFICER, Role.ISSUER, Role.AREA_OWNER -> {
                        // These roles can see all permits
                        permitModels
                    }

                    else -> {
                        // Requestor, Supervisor, Admin - see only their permits
                        permitModels.filter { it.requestorId == user.id }
                    }
                }

                val permits = filteredPermits.map { convertToPermit(it) }
                _permits.value = Resource.Success(permits)
            }
        }
    }

    fun canCreatePermit(): Boolean {
        val user = _currentUser.value ?: return false
        return when (user.role) {
            Role.REQUESTOR, Role.SUPERVISOR, Role.ADMIN -> true
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
            ppeVerified = model.ppeVerified,

            // Hot Work Checklist
            gasTesting = model.gasTesting,
            fireWatch = model.fireWatch,
            sparkShields = model.sparkShields,
            combustiblesRemoved = model.combustiblesRemoved,
            barricading = model.barricading,

            // LOTO Checklist
            isolationPoints = model.isolationPoints,
            locksApplied = model.locksApplied,
            locksVerified = model.locksVerified ?: false,
            zeroEnergyTest = model.zeroEnergyTest,
            hiddenSources = model.hiddenSources ?: false,

            // Confined Space Checklist
            oxygenLevel = model.oxygenLevel,
            lelLevel = model.lelLevel ?: false,
            toxicGases = model.toxicGases ?: false,
            ventilation = model.ventilation,
            rescueEquipment = model.rescueEquipment,
            attendant = model.attendant,
            rescuePlan = model.rescuePlan ?: false,

            // Working at Height Checklist
            harnessInspection = model.harnessInspection,
            anchorPoints = model.anchorPoints,
            fallProtection = model.fallProtection,
            scaffolding = model.scaffolding ?: false,
            rescuePlanHeight = model.rescuePlanHeight ?: false,

            // Lifting Checklist
            loadChart = model.loadChart,
            riggingInspection = model.riggingInspection,
            qualifiedCrew = model.qualifiedCrew,
            dropZone = model.dropZone,
            windSpeed = model.windSpeed,
            liftPlan = model.liftPlan ?: false,

            // Live Equipment Checklist
            arcFlashAssessment = model.arcFlashAssessment,
            arcRatedPpe = model.arcRatedPpe,
            liveWorkProcedure = model.liveWorkProcedure,
            voltageTesting = model.voltageTesting,
            boundaries = model.boundaries,

            // Cold Work Checklist
            basicIsolation = model.basicIsolation ?: false,
            correctPpe = model.correctPpe ?: false,
            barricadingCold = model.barricadingCold ?: false,
            spillPrevention = model.spillPrevention ?: false,
            housekeeping = model.housekeeping ?: false
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