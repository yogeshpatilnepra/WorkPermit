package com.apiscall.skeletoncode.workpermitmodule.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ApprovalHistoryDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("permitId")
    val permitId: String,

    @SerializedName("action")
    val action: String,

    @SerializedName("userId")
    val userId: String,

    @SerializedName("timestamp")
    val timestamp: Long,

    @SerializedName("comments")
    val comments: String?
)