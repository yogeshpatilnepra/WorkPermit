package com.apiscall.skeletoncode.workpermitmodule.data.remote.api

import com.apiscall.skeletoncode.workpermitmodule.data.remote.dto.AuthResponse
import com.apiscall.skeletoncode.workpermitmodule.data.remote.dto.LoginRequest
import com.apiscall.skeletoncode.workpermitmodule.data.remote.dto.NotificationDto
import com.apiscall.skeletoncode.workpermitmodule.data.remote.dto.PermitDto
import com.apiscall.skeletoncode.workpermitmodule.data.remote.dto.UserDto
import retrofit2.http.*

interface ApiService {

    @GET("permits")
    suspend fun getPermits(): List<PermitDto>

    @GET("permits/{id}")
    suspend fun getPermit(@Path("id") id: String): PermitDto

    @POST("permits")
    suspend fun createPermit(@Body permit: PermitDto): PermitDto

    @PUT("permits/{id}")
    suspend fun updatePermit(@Path("id") id: String, @Body permit: PermitDto): PermitDto

    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): AuthResponse

    @POST("auth/logout")
    suspend fun logout()

    @GET("notifications")
    suspend fun getNotifications(): List<NotificationDto>

    @PUT("notifications/{id}/read")
    suspend fun markNotificationAsRead(@Path("id") id: String): NotificationDto

    @GET("users")
    suspend fun getUsers(): List<UserDto>

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): UserDto
}