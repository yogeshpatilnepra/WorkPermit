package com.apiscall.skeletoncode.workpermitmodule.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.apiscall.skeletoncode.workpermitmodule.data.local.database.entities.PermitEntity
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PermitDao {

    @Query("SELECT * FROM permits ORDER BY updatedAt DESC")
    fun getAllPermits(): Flow<List<PermitEntity>>

    @Query("SELECT * FROM permits WHERE id = :permitId")
    fun getPermitById(permitId: String): Flow<PermitEntity?>

    @Query("SELECT * FROM permits WHERE status = :status")
    fun getPermitsByStatus(status: PermitStatus): Flow<List<PermitEntity>>

    @Query("SELECT * FROM permits WHERE requesterId = :userId")
    fun getPermitsByRequester(userId: String): Flow<List<PermitEntity>>

    @Query("SELECT * FROM permits WHERE status IN ('PENDING_ISSUER_APPROVAL', 'PENDING_AREA_OWNER_APPROVAL', 'PENDING_EHS_APPROVAL')")
    fun getPendingApprovals(): Flow<List<PermitEntity>>

    @Query("SELECT * FROM permits WHERE status = 'ACTIVE'")
    fun getActivePermits(): Flow<List<PermitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPermit(permit: PermitEntity)

    @Update
    suspend fun updatePermit(permit: PermitEntity)

    @Delete
    suspend fun deletePermit(permit: PermitEntity)

    @Query("DELETE FROM permits")
    suspend fun deleteAll()
}