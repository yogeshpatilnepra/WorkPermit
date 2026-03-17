package com.apiscall.skeletoncode.workpermitmodule.utils


import androidx.room.TypeConverter
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitStatus
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitType
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Role
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromPermitStatus(status: PermitStatus): String {
        return status.name
    }

    @TypeConverter
    fun toPermitStatus(status: String): PermitStatus {
        return PermitStatus.valueOf(status)
    }

    @TypeConverter
    fun fromPermitType(type: PermitType): String {
        return type.name
    }

    @TypeConverter
    fun toPermitType(type: String): PermitType {
        return PermitType.valueOf(type)
    }

    @TypeConverter
    fun fromRole(role: Role): String {
        return role.name
    }

    @TypeConverter
    fun toRole(role: String): Role {
        return Role.valueOf(role)
    }

    @TypeConverter
    fun fromStringMap(value: Map<String, Any>?): String? {
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toStringMap(value: String?): Map<String, Any>? {
        return value?.let {
            val type = object : TypeToken<Map<String, Any>>() {}.type
            gson.fromJson(it, type)
        }
    }
}