package com.apiscall.skeletoncode.workpermitmodule.presentation.approvals

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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ApprovalQueueViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Reactive flow for pending approvals
    val pendingApprovals: StateFlow<Resource<List<Permit>>> = _currentUser
        .filterNotNull()
        .distinctUntilChanged { old, new -> old.id == new.id && old.role == new.role }
        .flatMapLatest { user ->
            val canViewApprovals = when (user.role) {
                Role.ISSUER, Role.EHS_OFFICER, Role.AREA_OWNER, Role.ADMIN, Role.SUPERVISOR, Role.WORKER -> true
                else -> false
            }

            if (!canViewApprovals) {
                flowOf(Resource.Success(emptyList<Permit>()) as Resource<List<Permit>>)
            } else {
                // Fetch ALL pending permits across the system for Approvers
                firebaseRepository.getAllPendingApprovalsFlow()
                    .map { models ->
                        val permits = models.map { convertToPermit(it) }
                        Resource.Success(permits) as Resource<List<Permit>>
                    }
                    .catch { e -> emit(Resource.Error(e.message ?: "Error loading approvals")) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Resource.Loading
        )

    // Reactive flow for recent actions
    val recentActions: StateFlow<Resource<List<PermitAction>>> = _currentUser
        .filterNotNull()
        .distinctUntilChanged { old, new -> old.id == new.id }
        .flatMapLatest { user ->
            firebaseRepository.getPermitsFlow()
                .map { allPermits ->
                    val actions = mutableListOf<PermitAction>()
                    allPermits.forEach { permit ->
                        if (user.role == Role.ADMIN || user.role == Role.SUPERVISOR) {
                            // Admins and Supervisors see ALL actions
                            addAllUserActions(permit, actions)
                        } else {
                            // Other users see only THEIR own actions
                            val action = extractUserAction(permit, user)
                            if (action != null) actions.add(action)
                        }
                    }
                    Resource.Success(actions.distinctBy { "${it.permitId}_${it.action}_${it.timestamp.time}" }.sortedByDescending { it.timestamp }) as Resource<List<PermitAction>>
                }
                .catch { e -> emit(Resource.Error(e.message ?: "Error loading history")) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Resource.Loading
        )

    private val _approvalResult = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val approvalResult: StateFlow<Resource<Boolean>> = _approvalResult.asStateFlow()

    private val _rejectionResult = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val rejectionResult: StateFlow<Resource<Boolean>> = _rejectionResult.asStateFlow()

    private val _sendBackResult = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val sendBackResult: StateFlow<Resource<Boolean>> = _sendBackResult.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _currentUser.value = authRepository.getCurrentUser()
        }
    }

    private fun addAllUserActions(permit: PermitModel, list: MutableList<PermitAction>) {
        if (permit.issuerReviewedAt != null) {
            list.add(createAction(permit, permit.issuerReviewedAt.toDate(), 
                if (permit.status == "rejected" && permit.approvalStage == "rejected") "Rejected by Issuer"
                else if (permit.status == "sent_back" && permit.approvalStage == "sent_back") "Sent Back by Issuer"
                else "Approved by Issuer"))
        }
        if (permit.ehsReviewedAt != null) {
            list.add(createAction(permit, permit.ehsReviewedAt.toDate(), 
                if (permit.status == "rejected" && permit.approvalStage == "rejected") "Rejected by EHS"
                else if (permit.status == "sent_back" && permit.approvalStage == "sent_back") "Sent Back by EHS"
                else "Approved by EHS"))
        }
        if (permit.areaOwnerReviewedAt != null) {
            list.add(createAction(permit, permit.areaOwnerReviewedAt.toDate(), 
                if (permit.status == "rejected" && permit.approvalStage == "rejected") "Rejected by Area Owner"
                else if (permit.status == "sent_back" && permit.approvalStage == "sent_back") "Sent Back by Area Owner"
                else "Approved by Area Owner"))
        }
        if (permit.closedAt != null) {
            list.add(createAction(permit, permit.closedAt.toDate(), "Permit Closed by Supervisor"))
        }
    }

    private fun extractUserAction(permit: PermitModel, user: User): PermitAction? {
        return when (user.role) {
            Role.ISSUER -> {
                if (permit.issuerId == user.id && permit.issuerReviewedAt != null) {
                    createAction(permit, permit.issuerReviewedAt.toDate(), determineActionType(permit, "issuer"))
                } else null
            }
            Role.EHS_OFFICER -> {
                if (permit.ehsId == user.id && permit.ehsReviewedAt != null) {
                    createAction(permit, permit.ehsReviewedAt.toDate(), determineActionType(permit, "ehs"))
                } else null
            }
            Role.AREA_OWNER -> {
                if (permit.areaOwnerId == user.id && permit.areaOwnerReviewedAt != null) {
                    createAction(permit, permit.areaOwnerReviewedAt.toDate(), determineActionType(permit, "area_owner"))
                } else null
            }
            Role.WORKER -> null
            Role.SUPERVISOR -> {
                if (permit.supervisorId == user.id && permit.closedAt != null) {
                    createAction(permit, permit.closedAt.toDate(), "Closed Permit")
                } else null
            }
            else -> null
        }
    }

    private fun determineActionType(permit: PermitModel, role: String): String {
        return when (permit.status) {
            "rejected" -> "Rejected"
            "sent_back" -> "Sent Back"
            else -> "Approved"
        }
    }

    private fun createAction(permit: PermitModel, date: Date, action: String) = PermitAction(
        permitId = permit.id,
        permitNumber = permit.permitNumber,
        title = permit.title,
        action = action,
        timestamp = date
    )

    fun approvePermit(permitId: String, comments: String?) {
        viewModelScope.launch {
            _approvalResult.value = Resource.Loading
            val user = _currentUser.value ?: return@launch
            val role = getRoleKeyForPermit(user.role, permitId) ?: return@launch

            val result = firebaseRepository.approvePermit(permitId, role, user.id, user.fullName, comments)
            _approvalResult.value = if (result.isSuccess) Resource.Success(true) 
                                   else Resource.Error(result.exceptionOrNull()?.message ?: "Approval failed")
        }
    }

    private fun getRoleKeyDirect(role: Role): String? = when (role) {
        Role.ISSUER -> "issuer"
        Role.EHS_OFFICER -> "ehs"
        Role.AREA_OWNER -> "area_owner"
        else -> null
    }

    private suspend fun getRoleKeyForPermit(role: Role, permitId: String): String? {
        // Direct mapping for specific approver roles
        val directKey = getRoleKeyDirect(role)
        if (directKey != null) return directKey

        // For Admin/Supervisor, determine role key from the permit's current approval stage
        if (role == Role.ADMIN || role == Role.SUPERVISOR) {
            val permits = (pendingApprovals.value as? Resource.Success)?.data
            val permit = permits?.find { it.id == permitId }
            return when (permit?.approvalStage?.lowercase()?.trim()) {
                "issuer_review" -> "issuer"
                "ehs_review" -> "ehs"
                "area_owner_review" -> "area_owner"
                else -> "issuer"
            }
        }
        return null
    }

    fun rejectPermit(permitId: String, comments: String) {
        viewModelScope.launch {
            _rejectionResult.value = Resource.Loading
            val user = _currentUser.value ?: return@launch
            val role = getRoleKeyForPermit(user.role, permitId) ?: return@launch

            val result = firebaseRepository.rejectPermit(permitId, role, user.id, user.fullName, comments)
            _rejectionResult.value = if (result.isSuccess) Resource.Success(true)
                                    else Resource.Error(result.exceptionOrNull()?.message ?: "Rejection failed")
        }
    }

    fun sendBackPermit(permitId: String, comments: String) {
        viewModelScope.launch {
            _sendBackResult.value = Resource.Loading
            val user = _currentUser.value ?: return@launch
            val role = getRoleKeyForPermit(user.role, permitId) ?: return@launch

            val result = firebaseRepository.sendBackPermit(permitId, role, user.id, user.fullName, comments)
            _sendBackResult.value = if (result.isSuccess) Resource.Success(true)
                                   else Resource.Error(result.exceptionOrNull()?.message ?: "Action failed")
        }
    }

    fun resetApprovalResult() { _approvalResult.value = Resource.Idle }
    fun resetRejectionResult() { _rejectionResult.value = Resource.Idle }
    fun resetSendBackResult() { _sendBackResult.value = Resource.Idle }

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
            approvalStage = model.approvalStage,
            issuerComments = model.issuerComments,
            ehsComments = model.ehsComments,
            areaOwnerComments = model.areaOwnerComments
        )
    }

    private fun getPermitTypeFromString(type: String): PermitType {
        return when (type.lowercase().trim()) {
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
        return when (status.lowercase().trim()) {
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
