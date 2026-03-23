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
    val plant: String? = null,
    val department: String? = null,
    val area: String? = null,
    val company: String? = null,
    val shift: String? = null,
    val workerCount: Int = 0,
    val startDate: Date,
    val endDate: Date,
    val createdAt: Date,
    val updatedAt: Date,
    val formData: String,
    val attachments: List<Attachment> = emptyList(),
    val approvalHistory: List<ApprovalHistory> = emptyList(),
    val workers: List<User> = emptyList(),
    val remarks: String? = null,
    val closureRemarks: String? = null,
    val closedAt: Date? = null,
    val isDraft: Boolean = false,
    val riskAssessmentNo: String? = null,
    val jsaNo: String? = null,
    val toolboxTalkDone: Boolean = false,
    val ppeVerified: Boolean = false,

    // Hot Work Checklist
    val gasTesting: Boolean = false,
    val fireWatch: Boolean = false,
    val sparkShields: Boolean = false,
    val combustiblesRemoved: Boolean = false,
    val barricading: Boolean = false,

    // LOTO Checklist
    val isolationPoints: Boolean = false,
    val locksApplied: Boolean = false,
    val locksVerified: Boolean = false,
    val zeroEnergyTest: Boolean = false,
    val hiddenSources: Boolean = false,

    // Confined Space Checklist
    val oxygenLevel: Boolean = false,
    val lelLevel: Boolean = false,
    val toxicGases: Boolean = false,
    val ventilation: Boolean = false,
    val rescueEquipment: Boolean = false,
    val attendant: Boolean = false,
    val rescuePlan: Boolean = false,

    // Working at Height Checklist
    val harnessInspection: Boolean = false,
    val anchorPoints: Boolean = false,
    val fallProtection: Boolean = false,
    val scaffolding: Boolean = false,
    val rescuePlanHeight: Boolean = false,

    // Lifting Checklist
    val loadChart: Boolean = false,
    val riggingInspection: Boolean = false,
    val qualifiedCrew: Boolean = false,
    val dropZone: Boolean = false,
    val windSpeed: Boolean = false,
    val liftPlan: Boolean = false,

    // Live Equipment Checklist
    val arcFlashAssessment: Boolean = false,
    val arcRatedPpe: Boolean = false,
    val liveWorkProcedure: Boolean = false,
    val voltageTesting: Boolean = false,
    val boundaries: Boolean = false,

    // Cold Work Checklist
    val basicIsolation: Boolean = false,
    val correctPpe: Boolean = false,
    val barricadingCold: Boolean = false,
    val spillPrevention: Boolean = false,
    val housekeeping: Boolean = false,
    val approvalStage: String = "issuer_review",
    val issuerComments: String? = null,
    val ehsComments: String? = null,
    val areaOwnerComments: String? = null
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
    EXPIRED,
    SENT_BACK
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