package com.apiscall.skeletoncode.workpermitmodule.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "permits")
data class PermitEntity(
    @PrimaryKey
    val id: String,
    val permitNumber: String,
    val permitType: String,
    val title: String,
    val description: String,
    val status: String,
    val requesterId: String,
    val issuerId: String?,
    val areaOwnerId: String?,
    val ehsOfficerId: String?,
    val location: String,
    val startDate: Long,
    val endDate: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val formData: String, // JSON string
    val attachmentsJson: String, // JSON string
    val approvalHistoryJson: String, // JSON string
    val workersJson: String, // JSON string
    val remarks: String?,
    val closureRemarks: String?,
    val closedAt: Long?,
    val isDraft: Boolean
)