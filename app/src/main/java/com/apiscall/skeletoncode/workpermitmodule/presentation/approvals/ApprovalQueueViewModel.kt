package com.apiscall.skeletoncode.workpermitmodule.presentation.approvals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apiscall.skeletoncode.workpermitmodule.data.repository.FirebaseRepository
import com.apiscall.skeletoncode.workpermitmodule.domain.models.ApprovalStage
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Permit
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitModel
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitStatus
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitType
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Role
import com.apiscall.skeletoncode.workpermitmodule.domain.models.User
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.AuthRepository
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ApprovalQueueViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _pendingApprovals = MutableStateFlow<Resource<List<Permit>>>(Resource.Loading)
    val pendingApprovals: StateFlow<Resource<List<Permit>>> = _pendingApprovals.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _approvalResult = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val approvalResult: StateFlow<Resource<Boolean>> = _approvalResult.asStateFlow()

    private val _recentActions = MutableStateFlow<Resource<List<PermitAction>>>(Resource.Loading)
    val recentActions: StateFlow<Resource<List<PermitAction>>> = _recentActions.asStateFlow()

    private val _rejectionResult = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val rejectionResult: StateFlow<Resource<Boolean>> = _rejectionResult.asStateFlow()

    private val _sendBackResult = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val sendBackResult: StateFlow<Resource<Boolean>> = _sendBackResult.asStateFlow()

    private var pendingJob: Job? = null
    private var historyJob: Job? = null

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _currentUser.value = authRepository.getCurrentUser()
        }
    }

    fun loadPendingApprovals() {
        // Only show loading if we don't have data yet to avoid UI flicker
        if (_pendingApprovals.value !is Resource.Success) {
            _pendingApprovals.value = Resource.Loading
        }

        pendingJob?.cancel()
        pendingJob = viewModelScope.launch {
            try {
                var user = _currentUser.value ?: authRepository.getCurrentUser()
                if (user == null) {
                    _pendingApprovals.value = Resource.Error("User not logged in")
                    return@launch
                }

                val approvalStage = when (user.role) {
                    Role.ISSUER -> ApprovalStage.ISSUER_REVIEW
                    Role.EHS_OFFICER -> ApprovalStage.EHS_REVIEW
                    Role.AREA_OWNER -> ApprovalStage.AREA_OWNER_REVIEW
                    else -> null
                }

                if (approvalStage == null) {
                    _pendingApprovals.value = Resource.Success(emptyList())
                    return@launch
                }

                firebaseRepository.getPermitsByApprovalStageFlow(approvalStage)
                    .catch { e ->
                        _pendingApprovals.value = Resource.Error(e.message ?: "Failed to load")
                    }
                    .collect { permitModels ->
                        val permits = permitModels.map { convertToPermit(it) }
                        _pendingApprovals.value = Resource.Success(permits)
                    }
            } catch (e: Exception) {
                _pendingApprovals.value = Resource.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun loadRecentActions() {
        historyJob?.cancel()
        historyJob = viewModelScope.launch {
            _recentActions.value = Resource.Loading

            var user = _currentUser.value ?: authRepository.getCurrentUser()
            if (user == null) {
                _recentActions.value = Resource.Error("User not logged in")
                return@launch
            }

            firebaseRepository.getPermitsFlow().collect { allPermits ->
                val actions = mutableListOf<PermitAction>()

                allPermits.forEach { permit ->
                    when (user.role) {
                        Role.ISSUER -> {
                            if (permit.issuerId == user.id && permit.issuerReviewedAt != null) {
                                val actionType = when {
                                    permit.status == "rejected" -> "Rejected"
                                    permit.status == "sent_back" -> "Sent Back"
                                    permit.approvalStage != ApprovalStage.ISSUER_REVIEW && permit.approvalStage != "draft" -> "Approved"
                                    else -> null
                                }
                                actionType?.let {
                                    actions.add(
                                        PermitAction(
                                            permitId = permit.id,
                                            permitNumber = permit.permitNumber,
                                            title = permit.title,
                                            action = it,
                                            timestamp = permit.issuerReviewedAt.toDate()
                                        )
                                    )
                                }
                            }
                        }
                        Role.EHS_OFFICER -> {
                            if (permit.ehsId == user.id && permit.ehsReviewedAt != null) {
                                val actionType = when {
                                    permit.status == "rejected" -> "Rejected"
                                    permit.status == "sent_back" -> "Sent Back"
                                    else -> "Approved"
                                }
                                actions.add(
                                    PermitAction(
                                        permitId = permit.id,
                                        permitNumber = permit.permitNumber,
                                        title = permit.title,
                                        action = actionType,
                                        timestamp = permit.ehsReviewedAt.toDate()
                                    )
                                )
                            }
                        }
                        Role.AREA_OWNER -> {
                            if (permit.areaOwnerId == user.id && permit.areaOwnerReviewedAt != null) {
                                val actionType = when {
                                    permit.status == "rejected" -> "Rejected"
                                    permit.status == "sent_back" -> "Sent Back"
                                    else -> "Approved"
                                }
                                actions.add(
                                    PermitAction(
                                        permitId = permit.id,
                                        permitNumber = permit.permitNumber,
                                        title = permit.title,
                                        action = actionType,
                                        timestamp = permit.areaOwnerReviewedAt.toDate()
                                    )
                                )
                            }
                        }
                        else -> {}
                    }
                }

                _recentActions.value = Resource.Success(actions.sortedByDescending { it.timestamp })
            }
        }
    }

    fun approvePermit(permitId: String, comments: String?) {
        viewModelScope.launch {
            _approvalResult.value = Resource.Loading

            val user = _currentUser.value ?: authRepository.getCurrentUser()
            if (user == null) {
                _approvalResult.value = Resource.Error("User not logged in")
                return@launch
            }

            val roleStr = when (user.role) {
                Role.ISSUER -> "issuer"
                Role.EHS_OFFICER -> "ehs"
                Role.AREA_OWNER -> "area_owner"
                else -> {
                    _approvalResult.value = Resource.Error("You don't have permission to approve")
                    return@launch
                }
            }

            val result = firebaseRepository.approvePermit(
                permitId = permitId,
                role = roleStr,
                userId = user.id,
                userName = user.fullName,
                comments = comments
            )

            _approvalResult.value = if (result.isSuccess) {
                Resource.Success(true)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Approval failed")
            }
        }
    }

    fun rejectPermit(permitId: String, comments: String) {
        viewModelScope.launch {
            _rejectionResult.value = Resource.Loading

            val user = _currentUser.value ?: authRepository.getCurrentUser()
            if (user == null) {
                _rejectionResult.value = Resource.Error("User not logged in")
                return@launch
            }

            val roleStr = when (user.role) {
                Role.ISSUER -> "issuer"
                Role.EHS_OFFICER -> "ehs"
                Role.AREA_OWNER -> "area_owner"
                else -> {
                    _rejectionResult.value = Resource.Error("You don't have permission to reject")
                    return@launch
                }
            }

            val result = firebaseRepository.rejectPermit(
                permitId = permitId,
                role = roleStr,
                userId = user.id,
                userName = user.fullName,
                comments = comments
            )

            _rejectionResult.value = if (result.isSuccess) {
                Resource.Success(true)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Rejection failed")
            }
        }
    }

    fun sendBackPermit(permitId: String, comments: String) {
        viewModelScope.launch {
            _sendBackResult.value = Resource.Loading

            val user = _currentUser.value ?: authRepository.getCurrentUser()
            if (user == null) {
                _sendBackResult.value = Resource.Error("User not logged in")
                return@launch
            }

            val roleStr = when (user.role) {
                Role.ISSUER -> "issuer"
                Role.EHS_OFFICER -> "ehs"
                Role.AREA_OWNER -> "area_owner"
                else -> {
                    _sendBackResult.value = Resource.Error("You don't have permission to send back")
                    return@launch
                }
            }

            val result = firebaseRepository.sendBackPermit(
                permitId = permitId,
                role = roleStr,
                userId = user.id,
                userName = user.fullName,
                comments = comments
            )

            _sendBackResult.value = if (result.isSuccess) {
                Resource.Success(true)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Action failed")
            }
        }
    }

    fun resetApprovalResult() {
        _approvalResult.value = Resource.Idle
    }

    fun resetRejectionResult() {
        _rejectionResult.value = Resource.Idle
    }

    fun resetSendBackResult() {
        _sendBackResult.value = Resource.Idle
    }

    data class PermitAction(
        val permitId: String,
        val permitNumber: String,
        val title: String,
        val action: String,
        val timestamp: Date
    )

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

        val ehsOfficer = if (model.ehsId != null) {
            User(
                id = model.ehsId,
                username = "",
                email = "",
                fullName = model.ehsName ?: "",
                role = Role.EHS_OFFICER,
                department = "",
                employeeId = ""
            )
        } else null

        val areaOwner = if (model.areaOwnerId != null) {
            User(
                id = model.areaOwnerId,
                username = "",
                email = "",
                fullName = model.areaOwnerName ?: "",
                role = Role.AREA_OWNER,
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
            ehsOfficer = ehsOfficer,
            areaOwner = areaOwner,
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
            areaOwnerComments = model.areaOwnerComments,
            // Checklists
            gasTesting = model.gasTesting,
            fireWatch = model.fireWatch,
            sparkShields = model.sparkShields,
            combustiblesRemoved = model.combustiblesRemoved,
            barricading = model.barricading,
            isolationPoints = model.isolationPoints,
            locksApplied = model.locksApplied,
            locksVerified = model.locksVerified,
            zeroEnergyTest = model.zeroEnergyTest,
            hiddenSources = model.hiddenSources,
            oxygenLevel = model.oxygenLevel,
            lelLevel = model.lelLevel,
            toxicGases = model.toxicGases,
            ventilation = model.ventilation,
            rescueEquipment = model.rescueEquipment,
            attendant = model.attendant,
            rescuePlan = model.rescuePlan,
            harnessInspection = model.harnessInspection,
            anchorPoints = model.anchorPoints,
            fallProtection = model.fallProtection,
            scaffolding = model.scaffolding,
            rescuePlanHeight = model.rescuePlanHeight,
            loadChart = model.loadChart,
            riggingInspection = model.riggingInspection,
            qualifiedCrew = model.qualifiedCrew,
            dropZone = model.dropZone,
            windSpeed = model.windSpeed,
            liftPlan = model.liftPlan,
            arcFlashAssessment = model.arcFlashAssessment,
            arcRatedPpe = model.arcRatedPpe,
            liveWorkProcedure = model.liveWorkProcedure,
            voltageTesting = model.voltageTesting,
            boundaries = model.boundaries,
            basicIsolation = model.basicIsolation,
            correctPpe = model.correctPpe,
            barricadingCold = model.barricadingCold,
            spillPrevention = model.spillPrevention,
            housekeeping = model.housekeeping
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
