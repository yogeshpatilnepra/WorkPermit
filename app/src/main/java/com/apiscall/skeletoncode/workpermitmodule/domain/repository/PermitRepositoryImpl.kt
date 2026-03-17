package com.apiscall.skeletoncode.workpermitmodule.domain.repository


import com.apiscall.skeletoncode.workpermitmodule.data.local.datasource.MockDataSource
import com.apiscall.skeletoncode.workpermitmodule.domain.models.ApprovalAction
import com.apiscall.skeletoncode.workpermitmodule.domain.models.ApprovalHistory
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Attachment
import com.apiscall.skeletoncode.workpermitmodule.domain.models.FormField
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Notification
import com.apiscall.skeletoncode.workpermitmodule.domain.models.NotificationPriority
import com.apiscall.skeletoncode.workpermitmodule.domain.models.NotificationType
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Permit
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitStatus
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.toMutableList

@Singleton
class PermitRepositoryImpl @Inject constructor(
    private val mockDataSource: MockDataSource
) : PermitRepository {

    override suspend fun getPermits(): Flow<List<Permit>> = flow {
        delay(500) // Simulate network delay
        emit(mockDataSource.getPermits())
    }

    override suspend fun getPermitById(id: String): Flow<Permit?> = flow {
        delay(300)
        emit(mockDataSource.getPermitById(id))
    }

    override suspend fun getPermitsByStatus(status: PermitStatus): Flow<List<Permit>> = flow {
        delay(400)
        emit(mockDataSource.getPermitsByStatus(status))
    }

    override suspend fun getMyPermits(userId: String): Flow<List<Permit>> = flow {
        delay(400)
        emit(mockDataSource.getPermitsByRequester(userId))
    }

    override suspend fun getPendingApprovals(): Flow<List<Permit>> = flow {
        delay(400)
        emit(mockDataSource.getPendingApprovals())
    }

    override suspend fun getActivePermits(): Flow<List<Permit>> = flow {
        delay(400)
        emit(mockDataSource.getActivePermits())
    }

    override suspend fun createPermit(permit: Permit): Result<Permit> {
        return try {
            val newPermit = permit.copy(
                id = UUID.randomUUID().toString(),
                permitNumber = generatePermitNumber(),
                createdAt = Date(),
                updatedAt = Date(),
                status = PermitStatus.DRAFT
            )
            mockDataSource.addPermit(newPermit)
            Result.success(newPermit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePermit(permit: Permit): Result<Permit> {
        return try {
            val updatedPermit = permit.copy(updatedAt = Date())
            mockDataSource.updatePermit(updatedPermit)
            Result.success(updatedPermit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitForApproval(permitId: String, comments: String?): Result<Permit> {
        return try {
            val permit = mockDataSource.getPermitById(permitId)
                ?: return Result.failure(Exception("Permit not found"))

            val approvalHistory = permit.approvalHistory.toMutableList()
            approvalHistory.add(
                ApprovalHistory(
                    id = UUID.randomUUID().toString(),
                    permitId = permitId,
                    action = ApprovalAction.SUBMITTED,
                    user = permit.requester,
                    timestamp = Date(),
                    comments = comments
                )
            )

            val updatedPermit = permit.copy(
                status = PermitStatus.PENDING_ISSUER_APPROVAL,
                updatedAt = Date(),
                approvalHistory = approvalHistory
            )

            mockDataSource.updatePermit(updatedPermit)

            // Create notification for issuer
            mockDataSource.addNotification(
                Notification(
                    id = UUID.randomUUID().toString(),
                    title = "New Permit Request",
                    message = "Permit ${updatedPermit.permitNumber} requires your approval",
                    type = NotificationType.NEW_PERMIT_REQUEST,
                    permitId = permitId,
                    createdAt = Date(),
                    priority = NotificationPriority.HIGH
                )
            )

            Result.success(updatedPermit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun approvePermit(permitId: String, comments: String?): Result<Permit> {
        return try {
            val permit = mockDataSource.getPermitById(permitId)
                ?: return Result.failure(Exception("Permit not found"))

            // Determine next approval step based on current status
            val nextStatus = when (permit.status) {
                PermitStatus.PENDING_ISSUER_APPROVAL -> PermitStatus.PENDING_AREA_OWNER_APPROVAL
                PermitStatus.PENDING_AREA_OWNER_APPROVAL -> PermitStatus.PENDING_EHS_APPROVAL
                PermitStatus.PENDING_EHS_APPROVAL -> PermitStatus.APPROVED
                else -> permit.status
            }

            val approvalHistory = permit.approvalHistory.toMutableList()
            approvalHistory.add(
                ApprovalHistory(
                    id = UUID.randomUUID().toString(),
                    permitId = permitId,
                    action = ApprovalAction.APPROVED,
                    user = permit.issuer ?: permit.requester,
                    timestamp = Date(),
                    comments = comments
                )
            )

            val updatedPermit = permit.copy(
                status = nextStatus,
                updatedAt = Date(),
                approvalHistory = approvalHistory
            )

            mockDataSource.updatePermit(updatedPermit)

            // Create notification
            mockDataSource.addNotification(
                Notification(
                    id = UUID.randomUUID().toString(),
                    title = "Permit Approved",
                    message = "Permit ${updatedPermit.permitNumber} has been approved",
                    type = NotificationType.PERMIT_APPROVAL,
                    permitId = permitId,
                    createdAt = Date(),
                    priority = NotificationPriority.MEDIUM
                )
            )

            Result.success(updatedPermit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun rejectPermit(permitId: String, comments: String): Result<Permit> {
        return try {
            val permit = mockDataSource.getPermitById(permitId)
                ?: return Result.failure(Exception("Permit not found"))

            val approvalHistory = permit.approvalHistory.toMutableList()
            approvalHistory.add(
                ApprovalHistory(
                    id = UUID.randomUUID().toString(),
                    permitId = permitId,
                    action = ApprovalAction.REJECTED,
                    user = permit.issuer ?: permit.requester,
                    timestamp = Date(),
                    comments = comments
                )
            )

            val updatedPermit = permit.copy(
                status = PermitStatus.REJECTED,
                updatedAt = Date(),
                approvalHistory = approvalHistory
            )

            mockDataSource.updatePermit(updatedPermit)

            // Create notification
            mockDataSource.addNotification(
                Notification(
                    id = UUID.randomUUID().toString(),
                    title = "Permit Rejected",
                    message = "Permit ${updatedPermit.permitNumber} has been rejected",
                    type = NotificationType.PERMIT_REJECTION,
                    permitId = permitId,
                    createdAt = Date(),
                    priority = NotificationPriority.HIGH
                )
            )

            Result.success(updatedPermit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendBackPermit(permitId: String, comments: String): Result<Permit> {
        return try {
            val permit = mockDataSource.getPermitById(permitId)
                ?: return Result.failure(Exception("Permit not found"))

            val approvalHistory = permit.approvalHistory.toMutableList()
            approvalHistory.add(
                ApprovalHistory(
                    id = UUID.randomUUID().toString(),
                    permitId = permitId,
                    action = ApprovalAction.SENT_BACK,
                    user = permit.issuer ?: permit.requester,
                    timestamp = Date(),
                    comments = comments
                )
            )

            val updatedPermit = permit.copy(
                status = PermitStatus.DRAFT,
                updatedAt = Date(),
                approvalHistory = approvalHistory
            )

            mockDataSource.updatePermit(updatedPermit)

            Result.success(updatedPermit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun closePermit(permitId: String, closureRemarks: String): Result<Permit> {
        return try {
            val permit = mockDataSource.getPermitById(permitId)
                ?: return Result.failure(Exception("Permit not found"))

            val updatedPermit = permit.copy(
                status = PermitStatus.CLOSED,
                closedAt = Date(),
                closureRemarks = closureRemarks,
                updatedAt = Date()
            )

            mockDataSource.updatePermit(updatedPermit)

            Result.success(updatedPermit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addAttachment(permitId: String, attachment: Attachment): Result<Attachment> {
        return try {
            val permit = mockDataSource.getPermitById(permitId)
                ?: return Result.failure(Exception("Permit not found"))

            val updatedAttachments = permit.attachments.toMutableList()
            updatedAttachments.add(attachment)

            val updatedPermit = permit.copy(
                attachments = updatedAttachments,
                updatedAt = Date()
            )

            mockDataSource.updatePermit(updatedPermit)
            Result.success(attachment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPermitForm(permitType: PermitType): Flow<List<FormField>> = flow {
        delay(200)
        emit(mockDataSource.getPermitForm(permitType))
    }

    override suspend fun searchPermits(query: String): Flow<List<Permit>> = flow {
        delay(300)
        val results = mockDataSource.getPermits().filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.permitNumber.contains(query, ignoreCase = true) ||
                    it.location.contains(query, ignoreCase = true)
        }
        emit(results)
    }

    override suspend fun filterPermits(
        status: PermitStatus?,
        type: PermitType?,
        dateFrom: Long?,
        dateTo: Long?
    ): Flow<List<Permit>> = flow {
        delay(300)
        val filtered = mockDataSource.getPermits().filter { permit ->
            var matches = true

            if (status != null) {
                matches = matches && permit.status == status
            }

            if (type != null) {
                matches = matches && permit.permitType == type
            }

            if (dateFrom != null) {
                matches = matches && permit.startDate.time >= dateFrom
            }

            if (dateTo != null) {
                matches = matches && permit.endDate.time <= dateTo
            }

            matches
        }
        emit(filtered)
    }

    private fun generatePermitNumber(): String {
        val random = (100..999).random()
        return "PTW-${Calendar.getInstance().get(Calendar.YEAR)}-$random"
    }
}