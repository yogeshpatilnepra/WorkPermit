package com.apiscall.skeletoncode.solarproject.model

import com.google.gson.annotations.SerializedName

data class ServiceResponse(
    @SerializedName("code")
    val code: String = "",
    @SerializedName("msg")
    val msg: String = "",
    @SerializedName("data")
    val result: Any? = null
)