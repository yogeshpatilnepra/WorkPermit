package com.apiscall.skeletoncode.workpermitmodule.domain.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class PermitModel(
    val id: String = "",
    val permitNumber: String = "",
    val title: String = "",
    val permitType: String = "",
    val plant: String = "",
    val department: String = "",
    val area: String = "",
    val company: String = "",
    val shift: String = "",
    val workerCount: Int = 0,
    val workStart: Timestamp? = null,
    val workEnd: Timestamp? = null,
    val riskAssessmentNo: String = "",
    val jsaNo: String = "",
    val jobDescription: String = "",
    val toolboxTalkDone: Boolean = false,
    val ppeVerified: Boolean = false,
    val status: String = "draft",

    // Approval Stage Tracking
    val approvalStage: String = "issuer_review", // issuer_review, ehs_review, area_owner_review, issued, closed
    val issuerId: String? = null,
    val issuerName: String? = null,
    val issuerReviewedAt: Timestamp? = null,
    val issuerComments: String? = null,
    val ehsId: String? = null,
    val ehsName: String? = null,
    val ehsReviewedAt: Timestamp? = null,
    val ehsComments: String? = null,
    val areaOwnerId: String? = null,
    val areaOwnerName: String? = null,
    val areaOwnerReviewedAt: Timestamp? = null,
    val areaOwnerComments: String? = null,
    val supervisorId: String? = null,
    val supervisorName: String? = null,
    val closedAt: Timestamp? = null,
    val closureComments: String? = null,

    // Requestor Info
    val requestorId: String = "",
    val requestorName: String = "",
    val requestorEmail: String = "",

    // Timestamps
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,

    // Checklist fields (Hot Work)
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
    val housekeeping: Boolean = false
) : Parcelable

// Approval Stage Constants
object ApprovalStage {
    const val ISSUER_REVIEW = "issuer_review"
    const val EHS_REVIEW = "ehs_review"
    const val AREA_OWNER_REVIEW = "area_owner_review"
    const val ISSUED = "issued"
    const val ACTIVE = "active"
    const val CLOSED = "closed"
    const val REJECTED = "rejected"
    const val SENT_BACK = "sent_back"
}