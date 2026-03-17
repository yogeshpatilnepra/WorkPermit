package com.apiscall.skeletoncode.workpermitmodule.domain.models


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FormField(
    val id: String,
    val label: String,
    val type: FieldType,
    val isRequired: Boolean,
    val options: List<String> = emptyList(),
    val placeholder: String? = null,
    val validationRegex: String? = null,
    val defaultValue: String? = null,
    val minValue: Int? = null,
    val maxValue: Int? = null,
    val dependsOn: String? = null,
    val order: Int
) : Parcelable

enum class FieldType {
    TEXT,
    NUMBER,
    DATE,
    TIME,
    DROPDOWN,
    CHECKBOX,
    RADIO,
    TEXTAREA,
    SIGNATURE,
    FILE_UPLOAD
}