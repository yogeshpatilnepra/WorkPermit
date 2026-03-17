package com.apiscall.skeletoncode.workpermitmodule.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AttachmentDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("fileName")
    val fileName: String,

    @SerializedName("filePath")
    val filePath: String,

    @SerializedName("fileType")
    val fileType: String,

    @SerializedName("fileSize")
    val fileSize: Long,

    @SerializedName("uploadedById")
    val uploadedById: String,

    @SerializedName("uploadedAt")
    val uploadedAt: Long,

//    @SerializedName("description")
//    val description: String?
)