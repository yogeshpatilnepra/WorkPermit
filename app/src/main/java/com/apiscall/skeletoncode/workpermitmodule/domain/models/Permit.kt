package com.apiscall.skeletoncode.workpermitmodule.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Permit(
    val id: String,
    val permitNumber: String,
    val permitType: PermitType,
    val title: String,
    val description: String,
    val status: PermitStatus,
    val requester: User,
    val issuer: User? = null,
    val areaOwner: User? = null,
    val ehsOfficer: User? = null,
    val location: String,
    val startDate: Date,
    val endDate: Date,
    val createdAt: Date,
    val updatedAt: Date,
    val formData: Map<String, Any>,
    val attachments: List<Attachment> = emptyList(),
    val approvalHistory: List<ApprovalHistory> = emptyList(),
    val workers: List<User> = emptyList(),
    val remarks: String? = null,
    val closureRemarks: String? = null,
    val closedAt: Date? = null,
    val isDraft: Boolean = false
) : Parcelable

enum class PermitStatus {
    DRAFT,
    PENDING_ISSUER_APPROVAL,
    PENDING_AREA_OWNER_APPROVAL,
    PENDING_EHS_APPROVAL,
    APPROVED,
    REJECTED,
    ACTIVE,
    CLOSED,
    EXPIRED
}

enum class PermitType {
    HOT_WORK,
    COLD_WORK,
    LOTO,
    CONFINED_SPACE,
    WORK_AT_HEIGHT,
    LIFTING,
    LIVE_EQUIPMENT
}