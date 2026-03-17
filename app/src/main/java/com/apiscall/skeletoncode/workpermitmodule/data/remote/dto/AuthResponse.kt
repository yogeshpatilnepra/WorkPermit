package com.apiscall.skeletoncode.workpermitmodule.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("token")
    val token: String,
    @SerializedName("user")
    val user: UserDto,
    @SerializedName("expiresIn")
    val expiresIn: Long
)