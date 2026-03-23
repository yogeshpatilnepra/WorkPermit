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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _actionResult = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val actionResult: StateFlow<Resource<Boolean>> = _actionResult.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _currentUser.value = authRepository.getCurrentUser()
        }
    }

    fun loadPendingApprovals() {
        viewModelScope.launch {
            _pendingApprovals.value = Resource.Loading

            val user = _currentUser.value
            if (user == null) {
                _pendingApprovals.value = Resource.Error("User not logged in")
                return@launch
            }

            val approvalStage = when (user.role) {
                Role.ISSUER -> "issuer_review"
                Role.EHS_OFFICER -> "ehs_review"
                Role.AREA_OWNER -> "area_owner_review"
                else -> null
            }

            if (approvalStage == null) {
                _pendingApprovals.value = Resource.Success(emptyList())
                return@launch
            }

            firebaseRepository.getPermitsByApprovalStageFlow(approvalStage)
                .collect { permitModels ->
                    val permits = permitModels.map { convertToPermit(it) }
                    _pendingApprovals.value = Resource.Success(permits)
                }
        }
    }

    fun approvePermit(permitId: String, comments: String?) {
        viewModelScope.launch {
            _actionResult.value = Resource.Loading

            val user = _currentUser.value
            if (user == null) {
                _actionResult.value = Resource.Error("User not logged in")
                return@launch
            }

            val role = when (user.role) {
                Role.ISSUER -> "issuer"
                Role.EHS_OFFICER -> "ehs"
                Role.AREA_OWNER -> "area_owner"
                else -> {
                    _actionResult.value = Resource.Error("You don't have permission to approve")
                    return@launch
                }
            }

            val result = firebaseRepository.approvePermit(
                permitId = permitId,
                role = role,
                userId = user.id,
                userName = user.fullName,
                comments = comments
            )

            _actionResult.value = if (result.isSuccess) {
                Resource.Success(true)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Approval failed")
            }
        }
    }

    fun resetActionResult() {
        _actionResult.value = Resource.Idle
    }

    fun rejectPermit(permitId: String, comments: String) {
        viewModelScope.launch {
            _actionResult.value = Resource.Loading

            val user = _currentUser.value
            if (user == null) {
                _actionResult.value = Resource.Error("User not logged in")
                return@launch
            }

            val role = when (user.role) {
                Role.ISSUER -> "issuer"
                Role.EHS_OFFICER -> "ehs"
                Role.AREA_OWNER -> "area_owner"
                else -> {
                    _actionResult.value = Resource.Error("You don't have permission to reject")
                    return@launch
                }
            }

            val result = firebaseRepository.rejectPermit(
                permitId = permitId,
                role = role,
                userId = user.id,
                userName = user.fullName,
                comments = comments
            )

            _actionResult.value = if (result.isSuccess) {
                Resource.Success(true)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Rejection failed")
            }
        }
    }

    fun sendBackPermit(permitId: String, comments: String) {
        viewModelScope.launch {
            _actionResult.value = Resource.Loading

            val user = _currentUser.value
            if (user == null) {
                _actionResult.value = Resource.Error("User not logged in")
                return@launch
            }

            val role = when (user.role) {
                Role.ISSUER -> "issuer"
                Role.EHS_OFFICER -> "ehs"
                Role.AREA_OWNER -> "area_owner"
                else -> {
                    _actionResult.value = Resource.Error("You don't have permission to send back")
                    return@launch
                }
            }

            val result = firebaseRepository.sendBackPermit(
                permitId = permitId,
                role = role,
                userId = user.id,
                userName = user.fullName,
                comments = comments
            )

            _actionResult.value = if (result.isSuccess) {
                Resource.Success(true)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Action failed")
            }
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