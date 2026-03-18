package com.apiscall.skeletoncode.workpermitmodule.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


object FormDataConverter {
    private val gson = Gson()

    fun fromMap(map: Map<String, Any>): String {
        return gson.toJson(map)
    }

    fun toMap(json: String): Map<String, Any> {
        if (json.isEmpty() || json == "{}") {
            return emptyMap()
        }
        val type = object : TypeToken<Map<String, Any>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun emptyFormData(): String {
        return "{}"
    }
}