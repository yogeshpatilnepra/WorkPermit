package com.apiscall.skeletoncode.workpermitmodule.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PermitDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("permitNumber")
    val permitNumber: String,
    @SerializedName("permitType")
    val permitType: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("requesterId")
    val requesterId: String,
    @SerializedName("issuerId")
    val issuerId: String?,
    @SerializedName("areaOwnerId")
    val areaOwnerId: String?,
    @SerializedName("ehsOfficerId")
    val ehsOfficerId: String?,
    @SerializedName("location")
    val location: String,
    @SerializedName("startDate")
    val startDate: Long,
    @SerializedName("endDate")
    val endDate: Long,
    @SerializedName("createdAt")
    val createdAt: Long,
    @SerializedName("updatedAt")
    val updatedAt: Long,
    @SerializedName("formData")
    val formData: Map<String, Any>,
    @SerializedName("attachments")
    val attachments: List<AttachmentDto>,
    @SerializedName("approvalHistory")
    val approvalHistory: List<ApprovalHistoryDto>,
    @SerializedName("workers")
    val workers: List<String>,
    @SerializedName("remarks")
    val remarks: String?,
    @SerializedName("closureRemarks")
    val closureRemarks: String?,
    @SerializedName("closedAt")
    val closedAt: Long?,
    @SerializedName("isDraft")
    val isDraft: Boolean
)