package com.apiscall.skeletoncode.solarproject.model

import com.google.gson.annotations.SerializedName

data class MobileInfo(
        @SerializedName("brand")
        var brand: String = "",
        @SerializedName("manufacture")
        var manufacture: String = "",
        @SerializedName("model")
        var model: String = "",
        @SerializedName("sdk_version")
        var sdkVersion: String = "",
        @SerializedName("serial_no")
        var serialNo: String = "",
        @SerializedName("version_code")
        var versionCode: String = "",
)