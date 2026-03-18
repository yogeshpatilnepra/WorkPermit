package com.apiscall.skeletoncode.workpermitmodule.utils

import android.os.Parcel
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.android.parcel.Parceler

class MapParceler : Parceler<Map<String, Any>> {

    override fun create(parcel: Parcel): Map<String, Any> {
        val json = parcel.readString() ?: return emptyMap()
        val type = object : TypeToken<Map<String, Any>>() {}.type
        return Gson().fromJson(json, type)
    }

    override fun Map<String, Any>.write(parcel: Parcel, flags: Int) {
        parcel.writeString(Gson().toJson(this))
    }
}

// Helper annotation
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class MapParcelerAnnotation