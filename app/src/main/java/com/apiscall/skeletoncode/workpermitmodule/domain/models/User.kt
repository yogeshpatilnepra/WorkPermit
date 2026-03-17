package com.apiscall.skeletoncode.workpermitmodule.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String,
    val username: String,
    val email: String,
    val fullName: String,
    val role: Role,
    val department: String,
    val employeeId: String,
    val profileImageUrl: String? = null,
    val phoneNumber: String? = null,
    val isActive: Boolean = true
) : Parcelable

enum class Role {
    CONTRACTOR,
    PERMIT_ISSUER,
    EHS_OFFICER,
    AREA_OWNER,
    SUPERVISOR,
    WORKER
}
