package com.apiscall.skeletoncode.workpermitmodule.domain.repository

import com.apiscall.skeletoncode.workpermitmodule.domain.models.Attachment
import com.apiscall.skeletoncode.workpermitmodule.domain.models.FormField
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Permit
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitStatus
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitType
import kotlinx.coroutines.flow.Flow

interface PermitRepository {
    suspend fun getPermits(): Flow<List<Permit>>
    suspend fun getPermitById(id: String): Flow<Permit?>
    suspend fun getPermitsByStatus(status: PermitStatus): Flow<List<Permit>>
    suspend fun getMyPermits(userId: String): Flow<List<Permit>>
    suspend fun getPendingApprovals(): Flow<List<Permit>>
    suspend fun getActivePermits(): Flow<List<Permit>>
    suspend fun createPermit(permit: Permit): Result<Permit>
    suspend fun updatePermit(permit: Permit): Result<Permit>
    suspend fun submitForApproval(permitId: String, comments: String?): Result<Permit>
    suspend fun approvePermit(permitId: String, comments: String?): Result<Permit>
    suspend fun rejectPermit(permitId: String, comments: String): Result<Permit>
    suspend fun sendBackPermit(permitId: String, comments: String): Result<Permit>
    suspend fun closePermit(permitId: String, closureRemarks: String): Result<Permit>
    suspend fun addAttachment(permitId: String, attachment: Attachment): Result<Attachment>
    suspend fun getPermitForm(permitType: PermitType): Flow<List<FormField>>
    suspend fun searchPermits(query: String): Flow<List<Permit>>
    suspend fun filterPermits(
        status: PermitStatus?,
        type: PermitType?,
        dateFrom: Long?,
        dateTo: Long?
    ): Flow<List<Permit>>
}