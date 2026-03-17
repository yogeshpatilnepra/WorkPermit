package com.apiscall.skeletoncode.workpermitmodule.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Attachment(
    val id: String,
    val fileName: String,
    val filePath: String,
    val fileType: String,
    val fileSize: Long,
    val uploadedBy: User,
    val uploadedAt: Date,
    val permitId: String? = null
) : Parcelable