package com.apiscall.skeletoncode.workpermitmodule.domain.models

import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize

@Parcelize
data class WorkerModel(
    val id: String = "",
    val name: String = "",
    val signInAt: Timestamp? = null,
    val signOutAt: Timestamp? = null,
    val signedInById: String = "",
    val signedInByName: String = ""
) : Parcelable

@Parcelize
data class PermitModel(
    val id: String = "",
    val permitNumber: String = "",
    val title: String = "",
    val permitType: String = "",
    val plant: String = "",
    val plantCode: String = "",
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
    
    @JvmField val toolboxTalkDone: Boolean = false,
    @JvmField val toolboxTalkCompleted: Boolean = false,
    @JvmField val ppeVerified: Boolean = false,
    val status: String = "draft",

    // Approval Stage Tracking
    val approvalStage: String = "issuer_review", // issuer_review, ehs_review, area_owner_review, issued, active, closed
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
    val comments: String? = null,

    // Requestor Info
    val requestorId: String = "",
    val requestorName: String = "",
    val requestorEmail: String = "",

    // Timestamps
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,

    // Checklist fields (Hot Work)
    @JvmField val gasTesting: Boolean = false,
    @JvmField val fireWatch: Boolean = false,
    @JvmField val fireWatchAssigned: Boolean = false,
    @JvmField val fireExtinguisherAvailable: Boolean = false,
    @JvmField val sparkShields: Boolean = false,
    @JvmField val combustiblesRemoved: Boolean = false,
    @JvmField val flammablesRemoved: Boolean = false,
    @JvmField val barricading: Boolean = false,
    @JvmField val areaBarricaded: Boolean = false,
    @JvmField val postJobMonitoring: Boolean = false,
    @JvmField val riskAssessmentAttached: Boolean = false,
    
    @JvmField
    @PropertyName("stability")
    var stability: Long = 0,

    // LOTO Checklist
    @JvmField val isolationPoints: Boolean = false,
    @JvmField val locksApplied: Boolean = false,
    @JvmField val locksVerified: Boolean = false,
    @JvmField val zeroEnergyTest: Boolean = false,
    @JvmField val hiddenSources: Boolean = false,

    // Confined Space Checklist
    @JvmField val oxygenLevel: Boolean = false,
    @JvmField val lelLevel: Boolean = false,
    @JvmField val toxicGases: Boolean = false,
    @JvmField val ventilation: Boolean = false,
    @JvmField val rescueEquipment: Boolean = false,
    @JvmField val attendant: Boolean = false,
    @JvmField val rescuePlan: Boolean = false,

    // Working at Height Checklist
    @JvmField val harnessInspection: Boolean = false,
    @JvmField val anchorPoints: Boolean = false,
    @JvmField val fallProtection: Boolean = false,
    @JvmField val scaffolding: Boolean = false,
    @JvmField val rescuePlanHeight: Boolean = false,

    // Lifting Checklist
    @JvmField val loadChart: Boolean = false,
    @JvmField val riggingInspection: Boolean = false,
    @JvmField val qualifiedCrew: Boolean = false,
    @JvmField val dropZone: Boolean = false,
    @JvmField val windSpeed: Boolean = false,
    @JvmField val liftPlan: Boolean = false,

    // Live Equipment Checklist
    @JvmField val arcFlashAssessment: Boolean = false,
    @JvmField val arcRatedPpe: Boolean = false,
    @JvmField val liveWorkProcedure: Boolean = false,
    @JvmField val voltageTesting: Boolean = false,
    @JvmField val boundaries: Boolean = false,

    // Cold Work Checklist
    @JvmField val basicIsolation: Boolean = false,
    @JvmField val correctPpe: Boolean = false,
    @JvmField val barricadingCold: Boolean = false,
    @JvmField val spillPrevention: Boolean = false,
    @JvmField val housekeeping: Boolean = false
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