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
    val requestorId: String = "",
    val requestorName: String = "",
    val requestorEmail: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val plantCode: String = "",

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
    val housekeeping: Boolean = false
) : Parcelable