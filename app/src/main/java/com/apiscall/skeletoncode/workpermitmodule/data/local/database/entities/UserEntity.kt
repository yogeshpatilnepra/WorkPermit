package com.apiscall.skeletoncode.workpermitmodule.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Role

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val username: String,
    val email: String,
    val fullName: String,
    val role: Role,
    val department: String,
    val employeeId: String,
    val profileImageUrl: String?,
    val phoneNumber: String?,
    val isActive: Boolean
)