package com.apiscall.skeletoncode.workpermitmodule.presentation.permits.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apiscall.skeletoncode.workpermitmodule.data.repository.FirebaseRepository
import com.apiscall.skeletoncode.workpermitmodule.domain.models.*
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
class PermitDetailViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _permitDetails = MutableStateFlow<Resource<Permit>>(Resource.Loading)
    val permitDetails: StateFlow<Resource<Permit>> = _permitDetails.asStateFlow()

    private val _actionResult = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val actionResult: StateFlow<Resource<Boolean>> = _actionResult.asStateFlow()

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

    fun loadPermitDetails(permitId: String) {
        viewModelScope.launch {
            _permitDetails.value = Resource.Loading

            combine(
                firebaseRepository.getPermitById(permitId),
                firebaseRepository.getAttachmentsFlow(permitId)
            ) { permitModel, attachmentMaps ->
                if (permitModel != null) {
                    val attachments = attachmentMaps.mapNotNull { map ->
                        try {
                            Attachment(
                                id = map["id"] as? String ?: return@mapNotNull null,
                                fileName = map["fileName"] as? String ?: "",
                                filePath = map["filePath"] as? String ?: "",
                                fileType = map["fileType"] as? String ?: "",
                                fileSize = (map["fileSize"] as? Long) ?: 0,
                                uploadedBy = User(
                                    id = map["uploadedById"] as? String ?: "",
                                    username = "",
                                    email = "",
                                    fullName = map["uploadedByName"] as? String ?: "",
                                    role = Role.WORKER,
                                    department = "",
                                    employeeId = ""
                                ),
                                uploadedAt = Date((map["uploadedAt"] as? Long) ?: System.currentTimeMillis())
                            )
                        } catch (e: Exception) { null }
                    }
                    val permit = convertToPermit(permitModel, attachments)
                    Resource.Success(permit)
                } else {
                    Resource.Error("Permit not found")
                }
            }.collect {
                _permitDetails.value = it
            }
        }
    }

    fun approvePermit(permitId: String, role: String, comments: String?) {
        viewModelScope.launch {
            _actionResult.value = Resource.Loading
            val user = _currentUser.value ?: return@launch
            val result = firebaseRepository.approvePermit(permitId, role, user.id, user.fullName, comments)
            _actionResult.value = if (result.isSuccess) Resource.Success(true) else Resource.Error(result.exceptionOrNull()?.message ?: "Approval failed")
        }
    }

    fun rejectPermit(permitId: String, role: String, comments: String) {
        viewModelScope.launch {
            _actionResult.value = Resource.Loading
            val user = _currentUser.value ?: return@launch
            val result = firebaseRepository.rejectPermit(permitId, role, user.id, user.fullName, comments)
            _actionResult.value = if (result.isSuccess) Resource.Success(true) else Resource.Error(result.exceptionOrNull()?.message ?: "Rejection failed")
        }
    }

    fun sendBackPermit(permitId: String, role: String, comments: String) {
        viewModelScope.launch {
            _actionResult.value = Resource.Loading
            val user = _currentUser.value ?: return@launch
            val result = firebaseRepository.sendBackPermit(permitId, role, user.id, user.fullName, comments)
            _actionResult.value = if (result.isSuccess) Resource.Success(true) else Resource.Error(result.exceptionOrNull()?.message ?: "Action failed")
        }
    }

    fun closePermit(permitId: String, comments: String) {
        viewModelScope.launch {
            _actionResult.value = Resource.Loading
            val user = _currentUser.value ?: return@launch
            val result = firebaseRepository.closePermit(permitId, user.id, user.fullName, comments)
            _actionResult.value = if (result.isSuccess) Resource.Success(true) else Resource.Error(result.exceptionOrNull()?.message ?: "Failed to close permit")
        }
    }

    fun submitDraftPermit(permitId: String) {
        viewModelScope.launch {
            _actionResult.value = Resource.Loading
            val result = firebaseRepository.submitDraftPermit(permitId)
            _actionResult.value = if (result.isSuccess) Resource.Success(true) else Resource.Error("Submit failed")
        }
    }

    fun resubmitPermit(permitId: String) {
        viewModelScope.launch {
            _actionResult.value = Resource.Loading
            val result = firebaseRepository.resubmitPermit(permitId)
            _actionResult.value = if (result.isSuccess) Resource.Success(true) else Resource.Error("Resubmit failed")
        }
    }

    fun deletePermit(permitId: String) {
        viewModelScope.launch {
            _actionResult.value = Resource.Loading
            val result = firebaseRepository.deletePermit(permitId)
            _actionResult.value = if (result.isSuccess) Resource.Success(true) else Resource.Error("Delete failed")
        }
    }

    fun workerSignIn(permitId: String, workerName: String) {
        viewModelScope.launch {
            _actionResult.value = Resource.Loading
            val user = _currentUser.value ?: return@launch
            val result = firebaseRepository.workerSignIn(permitId, user.id, workerName, user.fullName)
            _actionResult.value = if (result.isSuccess) Resource.Success(true) else Resource.Error(result.exceptionOrNull()?.message ?: "Sign in failed")
        }
    }

    fun workerSignOut(permitId: String, logEntryId: String) {
        viewModelScope.launch {
            _actionResult.value = Resource.Loading
            val result = firebaseRepository.workerSignOut(permitId, logEntryId)
            _actionResult.value = if (result.isSuccess) Resource.Success(true) else Resource.Error(result.exceptionOrNull()?.message ?: "Sign out failed")
        }
    }

    private fun convertToPermit(model: PermitModel, attachments: List<Attachment> = emptyList(), workerLogModels: List<WorkerModel> = emptyList()): Permit {
        val requester = User(
            id = model.requestorId,
            username = "",
            email = model.requestorEmail,
            fullName = model.requestorName,
            role = Role.REQUESTOR,
            department = model.department,
            employeeId = ""
        )

        val issuer = if (model.issuerId != null) User(model.issuerId, "", "", model.issuerName ?: "", Role.ISSUER, "", "") else null
        val ehsOfficer = if (model.ehsId != null) User(model.ehsId, "", "", model.ehsName ?: "", Role.EHS_OFFICER, "", "") else null
        val areaOwner = if (model.areaOwnerId != null) User(model.areaOwnerId, "", "", model.areaOwnerName ?: "", Role.AREA_OWNER, "", "") else null

        val workerLog = workerLogModels.map { wm ->
            WorkerSignIn(
                id = wm.id,
                name = wm.name,
                signInAt = wm.signInAt?.toDate() ?: Date(),
                signOutAt = wm.signOutAt?.toDate(),
                signedInBy = User(wm.signedInById, "", "", wm.signedInByName, Role.WORKER, "", "")
            )
        }

        // Build approval history from model fields
        val history = mutableListOf<ApprovalHistory>()
        if (model.issuerReviewedAt != null) {
            val action = if (model.status == "rejected" && model.approvalStage == "rejected") ApprovalAction.REJECTED 
                        else if (model.status == "sent_back" && model.approvalStage == "sent_back") ApprovalAction.SENT_BACK
                        else ApprovalAction.APPROVED
            history.add(ApprovalHistory("h1", model.id, action, issuer ?: requester, model.issuerReviewedAt.toDate(), model.issuerComments))
        }
        if (model.ehsReviewedAt != null) {
            val action = if (model.status == "rejected" && model.approvalStage == "rejected") ApprovalAction.REJECTED 
                        else if (model.status == "sent_back" && model.approvalStage == "sent_back") ApprovalAction.SENT_BACK
                        else ApprovalAction.APPROVED
            history.add(ApprovalHistory("h2", model.id, action, ehsOfficer ?: requester, model.ehsReviewedAt.toDate(), model.ehsComments))
        }
        if (model.areaOwnerReviewedAt != null) {
            val action = if (model.status == "rejected" && model.approvalStage == "rejected") ApprovalAction.REJECTED 
                        else if (model.status == "sent_back" && model.approvalStage == "sent_back") ApprovalAction.SENT_BACK
                        else ApprovalAction.APPROVED
            history.add(ApprovalHistory("h3", model.id, action, areaOwner ?: requester, model.areaOwnerReviewedAt.toDate(), model.areaOwnerComments))
        }
        
        // Add worker sign-ins to history
        workerLog.forEachIndexed { index, ws ->
            history.add(ApprovalHistory("w_$index", model.id, ApprovalAction.APPROVED, ws.signedInBy, ws.signInAt, "Worker Signed In: ${ws.name}"))
            ws.signOutAt?.let { soot ->
                history.add(ApprovalHistory("wo_$index", model.id, ApprovalAction.APPROVED, ws.signedInBy, soot, "Worker Signed Out: ${ws.name}"))
            }
        }

        if (model.closedAt != null) {
            history.add(ApprovalHistory("h6", model.id, ApprovalAction.APPROVED, User(model.supervisorId ?: "", "", "", model.supervisorName ?: "Supervisor", Role.SUPERVISOR, "", ""), model.closedAt.toDate(), model.closureComments))
        }

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
            attachments = attachments,
            approvalHistory = history,
            closedAt = model.closedAt?.toDate(),
            closureRemarks = model.closureComments,
            workerLog = workerLog,
            riskAssessmentNo = model.riskAssessmentNo,
            jsaNo = model.jsaNo,
            toolboxTalkDone = model.toolboxTalkDone,
            ppeVerified = model.ppeVerified,
            gasTesting = model.gasTesting,
            fireWatch = model.fireWatch,
            sparkShields = model.sparkShields,
            combustiblesRemoved = model.combustiblesRemoved,
            barricading = model.barricading,
            isolationPoints = model.isolationPoints,
            locksApplied = model.locksApplied,
            zeroEnergyTest = model.zeroEnergyTest,
            oxygenLevel = model.oxygenLevel,
            ventilation = model.ventilation,
            rescueEquipment = model.rescueEquipment,
            attendant = model.attendant,
            harnessInspection = model.harnessInspection,
            anchorPoints = model.anchorPoints,
            fallProtection = model.fallProtection,
            loadChart = model.loadChart,
            riggingInspection = model.riggingInspection,
            qualifiedCrew = model.qualifiedCrew,
            dropZone = model.dropZone,
            arcFlashAssessment = model.arcFlashAssessment,
            arcRatedPpe = model.arcRatedPpe,
            voltageTesting = model.voltageTesting,
            basicIsolation = model.basicIsolation,
            correctPpe = model.correctPpe,
            housekeeping = model.housekeeping,
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
