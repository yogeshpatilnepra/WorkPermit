package com.apiscall.skeletoncode.workpermitmodule.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("fullName")
    val fullName: String,
    @SerializedName("role")
    val role: String,
    @SerializedName("department")
    val department: String,
    @SerializedName("employeeId")
    val employeeId: String,
    @SerializedName("profileImageUrl")
    val profileImageUrl: String?,
    @SerializedName("phoneNumber")
    val phoneNumber: String?,
    @SerializedName("isActive")
    val isActive: Boolean
)