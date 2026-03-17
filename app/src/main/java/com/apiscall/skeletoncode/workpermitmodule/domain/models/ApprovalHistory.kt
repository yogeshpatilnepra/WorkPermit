package com.apiscall.skeletoncode.workpermitmodule.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class ApprovalHistory(
    val id: String,
    val permitId: String,
    val action: ApprovalAction,
    val user: User,
    val timestamp: Date,
    val comments: String? = null
) : Parcelable

enum class ApprovalAction {
    SUBMITTED,
    APPROVED,
    REJECTED,
    SENT_BACK,
    SIGNED_IN,
    SIGNED_OUT,
    CLOSED
}