package com.apiscall.skeletoncode.workpermitmodule.data.remote.dto

import com.google.gson.annotations.SerializedName

data class NotificationDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("permitId")
    val permitId: String?,

    @SerializedName("isRead")
    val isRead: Boolean,

    @SerializedName("createdAt")
    val createdAt: Long,

    @SerializedName("priority")
    val priority: String
)