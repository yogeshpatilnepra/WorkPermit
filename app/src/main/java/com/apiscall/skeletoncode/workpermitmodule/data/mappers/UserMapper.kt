package com.apiscall.skeletoncode.workpermitmodule.data.mappers

import com.apiscall.skeletoncode.workpermitmodule.data.local.database.entities.UserEntity
import com.apiscall.skeletoncode.workpermitmodule.data.remote.dto.UserDto
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Role
import com.apiscall.skeletoncode.workpermitmodule.domain.models.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserMapper @Inject constructor() {

    fun mapToDomain(dto: UserDto): User {
        return User(
            id = dto.id,
            username = dto.username,
            email = dto.email,
            fullName = dto.fullName,
            role = Role.valueOf(dto.role),
            department = dto.department,
            employeeId = dto.employeeId,
            profileImageUrl = dto.profileImageUrl,
            phoneNumber = dto.phoneNumber,
            isActive = dto.isActive
        )
    }

    fun mapToEntity(user: User): UserEntity {
        return UserEntity(
            id = user.id,
            username = user.username,
            email = user.email,
            fullName = user.fullName,
            role = user.role.name,
            department = user.department,
            employeeId = user.employeeId,
            profileImageUrl = user.profileImageUrl,
            phoneNumber = user.phoneNumber,
            isActive = user.isActive
        )
    }

    fun mapToDto(user: User): UserDto {
        return UserDto(
            id = user.id,
            username = user.username,
            email = user.email,
            fullName = user.fullName,
            role = user.role.name,
            department = user.department,
            employeeId = user.employeeId,
            profileImageUrl = user.profileImageUrl,
            phoneNumber = user.phoneNumber,
            isActive = user.isActive
        )
    }
}