package com.apiscall.skeletoncode.workpermitmodule.data.mappers


import com.apiscall.skeletoncode.workpermitmodule.data.local.database.entities.PermitEntity
import com.apiscall.skeletoncode.workpermitmodule.data.remote.dto.ApprovalHistoryDto
import com.apiscall.skeletoncode.workpermitmodule.data.remote.dto.AttachmentDto
import com.apiscall.skeletoncode.workpermitmodule.data.remote.dto.PermitDto
import com.apiscall.skeletoncode.workpermitmodule.domain.models.ApprovalAction
import com.apiscall.skeletoncode.workpermitmodule.domain.models.ApprovalHistory
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Attachment
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Permit
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitStatus
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitType
import com.apiscall.skeletoncode.workpermitmodule.domain.models.User
import com.google.gson.Gson
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermitMapper @Inject constructor(
    private val userMapper: UserMapper,
    private val gson: Gson
) {

    fun mapToDomain(dto: PermitDto, users: Map<String, User>): Permit {
        return Permit(
            id = dto.id,
            permitNumber = dto.permitNumber,
            permitType = PermitType.valueOf(dto.permitType),
            title = dto.title,
            description = dto.description,
            status = PermitStatus.valueOf(dto.status),
            requester = users[dto.requesterId]
                ?: throw IllegalStateException("Requester not found"),
            issuer = dto.issuerId?.let { users[it] },
            areaOwner = dto.areaOwnerId?.let { users[it] },
            ehsOfficer = dto.ehsOfficerId?.let { users[it] },
            location = dto.location,
            startDate = Date(dto.startDate),
            endDate = Date(dto.endDate),
            createdAt = Date(dto.createdAt),
            updatedAt = Date(dto.updatedAt),
            formData = dto.formData,
            attachments = dto.attachments.map { mapAttachmentToDomain(it, users) },
            approvalHistory = dto.approvalHistory.map { mapApprovalHistoryToDomain(it, users) },
            workers = dto.workers.mapNotNull { users[it] },
            remarks = dto.remarks,
            closureRemarks = dto.closureRemarks,
            closedAt = dto.closedAt?.let { Date(it) },
            isDraft = dto.isDraft
        )
    }

    fun mapToEntity(permit: Permit): PermitEntity {
        return PermitEntity(
            id = permit.id,
            permitNumber = permit.permitNumber,
            permitType = permit.permitType.name,
            title = permit.title,
            description = permit.description,
            status = permit.status.name,
            requesterId = permit.requester.id,
            issuerId = permit.issuer?.id,
            areaOwnerId = permit.areaOwner?.id,
            ehsOfficerId = permit.ehsOfficer?.id,
            location = permit.location,
            startDate = permit.startDate.time,
            endDate = permit.endDate.time,
            createdAt = permit.createdAt.time,
            updatedAt = permit.updatedAt.time,
            formData = gson.toJson(permit.formData),
            attachmentsJson = gson.toJson(permit.attachments.map { mapAttachmentToDto(it) }),
            approvalHistoryJson = gson.toJson(permit.approvalHistory.map {
                mapApprovalHistoryToDto(
                    it
                )
            }),
            workersJson = gson.toJson(permit.workers.map { it.id }),
            remarks = permit.remarks,
            closureRemarks = permit.closureRemarks,
            closedAt = permit.closedAt?.time,
            isDraft = permit.isDraft
        )
    }

    fun mapToDto(permit: Permit): PermitDto {
        return PermitDto(
            id = permit.id,
            permitNumber = permit.permitNumber,
            permitType = permit.permitType.name,
            title = permit.title,
            description = permit.description,
            status = permit.status.name,
            requesterId = permit.requester.id,
            issuerId = permit.issuer?.id,
            areaOwnerId = permit.areaOwner?.id,
            ehsOfficerId = permit.ehsOfficer?.id,
            location = permit.location,
            startDate = permit.startDate.time,
            endDate = permit.endDate.time,
            createdAt = permit.createdAt.time,
            updatedAt = permit.updatedAt.time,
            formData = permit.formData,
            attachments = permit.attachments.map { mapAttachmentToDto(it) },
            approvalHistory = permit.approvalHistory.map { mapApprovalHistoryToDto(it) },
            workers = permit.workers.map { it.id },
            remarks = permit.remarks,
            closureRemarks = permit.closureRemarks,
            closedAt = permit.closedAt?.time,
            isDraft = permit.isDraft
        )
    }

    private fun mapAttachmentToDomain(dto: AttachmentDto, users: Map<String, User>): Attachment {
        return Attachment(
            id = dto.id,
            fileName = dto.fileName,
            filePath = dto.filePath,
            fileType = dto.fileType,
            fileSize = dto.fileSize,
            uploadedBy = users[dto.uploadedById]
                ?: throw IllegalStateException("Uploader not found"),
            uploadedAt = Date(dto.uploadedAt)
        )
    }

    private fun mapAttachmentToDto(attachment: Attachment): AttachmentDto {
        return AttachmentDto(
            id = attachment.id,
            fileName = attachment.fileName,
            filePath = attachment.filePath,
            fileType = attachment.fileType,
            fileSize = attachment.fileSize,
            uploadedById = attachment.uploadedBy.id,
            uploadedAt = attachment.uploadedAt.time,
        )
    }

    private fun mapApprovalHistoryToDomain(
        dto: ApprovalHistoryDto,
        users: Map<String, User>
    ): ApprovalHistory {
        return ApprovalHistory(
            id = dto.id,
            permitId = dto.permitId,
            action = ApprovalAction.valueOf(dto.action),
            user = users[dto.userId] ?: throw IllegalStateException("User not found"),
            timestamp = Date(dto.timestamp),
            comments = dto.comments
        )
    }

    private fun mapApprovalHistoryToDto(history: ApprovalHistory): ApprovalHistoryDto {
        return ApprovalHistoryDto(
            id = history.id,
            permitId = history.permitId,
            action = history.action.name,
            userId = history.user.id,
            timestamp = history.timestamp.time,
            comments = history.comments
        )
    }
}