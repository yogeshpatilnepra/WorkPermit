package com.apiscall.skeletoncode.workpermitmodule.data.repository

import android.util.Log
import com.apiscall.skeletoncode.workpermitmodule.domain.models.ApprovalStage
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitModel
import com.apiscall.skeletoncode.workpermitmodule.domain.models.WorkerModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRepository @Inject constructor() {

    private val db = FirebaseFirestore.getInstance()
    private val permitsCollection = db.collection("permits")
    private val TAG = "FirebaseRepository"

    fun getPermitsFlow(): Flow<List<PermitModel>> {
        return permitsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(PermitModel::class.java) }
    }

    fun getPermitById(permitId: String): Flow<PermitModel?> {
        return permitsCollection.document(permitId).snapshots()
            .map { it.toObject(PermitModel::class.java) }
    }

    fun getPermitsByApprovalStageFlow(stage: String): Flow<List<PermitModel>> {
        return permitsCollection
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(PermitModel::class.java).filter { permit ->
                    val permitStage = permit.approvalStage ?: if (permit.status == "submitted" || permit.status == "pending") "issuer_review" else ""
                    permitStage == stage &&
                    permit.status != "rejected" && permit.status != "sent_back" && permit.status != "closed" && permit.status != "draft"
                }.sortedByDescending { it.createdAt }
            }
    }

    fun getAllPendingApprovalsFlow(): Flow<List<PermitModel>> {
        return permitsCollection
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(PermitModel::class.java).filter { permit ->
                    val permitStage = permit.approvalStage ?: if (permit.status == "submitted" || permit.status == "pending") "issuer_review" else ""
                    // Include review stages AND issued/active for Supervisor closure flow
                    (permitStage == "issuer_review" || permitStage == "ehs_review" || permitStage == "area_owner_review" || permitStage == "issued" || permitStage == "active") &&
                    permit.status != "rejected" && permit.status != "sent_back" && permit.status != "closed" && permit.status != "draft"
                }.sortedByDescending { it.createdAt }
            }
    }

    suspend fun createPermit(permit: PermitModel): Result<PermitModel> {
        return try {
            val id = if (permit.id.isEmpty()) UUID.randomUUID().toString() else permit.id
            val permitNumber = if (permit.permitNumber.isEmpty()) generatePermitNumber() else permit.permitNumber
            val newPermit = permit.copy(
                id = id,
                permitNumber = permitNumber,
                createdAt = permit.createdAt ?: Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            permitsCollection.document(id).set(newPermit).await()
            Result.success(newPermit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun approvePermit(permitId: String, role: String, userId: String, userName: String, comments: String?): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "updatedAt" to Timestamp.now()
            )

            when (role) {
                "issuer" -> {
                    updates["issuerId"] = userId
                    updates["issuerName"] = userName
                    updates["issuerReviewedAt"] = Timestamp.now()
                    updates["issuerComments"] = comments ?: ""
                    updates["approvalStage"] = "ehs_review"
                }
                "ehs" -> {
                    updates["ehsId"] = userId
                    updates["ehsName"] = userName
                    updates["ehsReviewedAt"] = Timestamp.now()
                    updates["ehsComments"] = comments ?: ""
                    updates["approvalStage"] = "area_owner_review"
                }
                "area_owner" -> {
                    updates["areaOwnerId"] = userId
                    updates["areaOwnerName"] = userName
                    updates["areaOwnerReviewedAt"] = Timestamp.now()
                    updates["areaOwnerComments"] = comments ?: ""
                    updates["approvalStage"] = "issued"
                    updates["status"] = "issued"
                }
            }
            permitsCollection.document(permitId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun rejectPermit(permitId: String, role: String, userId: String, userName: String, comments: String): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "status" to "rejected",
                "approvalStage" to "rejected",
                "updatedAt" to Timestamp.now()
            )
            when (role) {
                "issuer" -> {
                    updates["issuerId"] = userId
                    updates["issuerName"] = userName
                    updates["issuerReviewedAt"] = Timestamp.now()
                    updates["issuerComments"] = comments
                }
                "ehs" -> {
                    updates["ehsId"] = userId
                    updates["ehsName"] = userName
                    updates["ehsReviewedAt"] = Timestamp.now()
                    updates["ehsComments"] = comments
                }
                "area_owner" -> {
                    updates["areaOwnerId"] = userId
                    updates["areaOwnerName"] = userName
                    updates["areaOwnerReviewedAt"] = Timestamp.now()
                    updates["areaOwnerComments"] = comments
                }
            }
            permitsCollection.document(permitId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun sendBackPermit(permitId: String, role: String, userId: String, userName: String, comments: String): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "status" to "sent_back",
                "approvalStage" to "sent_back",
                "updatedAt" to Timestamp.now()
            )
            when (role) {
                "issuer" -> {
                    updates["issuerId"] = userId
                    updates["issuerName"] = userName
                    updates["issuerReviewedAt"] = Timestamp.now()
                    updates["issuerComments"] = comments
                }
                "ehs" -> {
                    updates["ehsId"] = userId
                    updates["ehsName"] = userName
                    updates["ehsReviewedAt"] = Timestamp.now()
                    updates["ehsComments"] = comments
                }
                "area_owner" -> {
                    updates["areaOwnerId"] = userId
                    updates["areaOwnerName"] = userName
                    updates["areaOwnerReviewedAt"] = Timestamp.now()
                    updates["areaOwnerComments"] = comments
                }
            }
            permitsCollection.document(permitId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun submitDraftPermit(permitId: String): Result<Unit> {
        return try {
            val updates = mapOf(
                "status" to "submitted",
                "approvalStage" to "issuer_review",
                "updatedAt" to Timestamp.now()
            )
            permitsCollection.document(permitId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun resubmitPermit(permitId: String): Result<Unit> {
        return submitDraftPermit(permitId)
    }

    suspend fun deletePermit(permitId: String): Result<Unit> {
        return try {
            permitsCollection.document(permitId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun closePermit(permitId: String, userId: String, userName: String, comments: String): Result<Unit> {
        return try {
            val updates = mapOf(
                "status" to "closed",
                "approvalStage" to "closed",
                "supervisorId" to userId,
                "supervisorName" to userName,
                "closedAt" to Timestamp.now(),
                "closureComments" to comments,
                "updatedAt" to Timestamp.now()
            )
            permitsCollection.document(permitId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun workerSignIn(permitId: String, userId: String, workerName: String, signedInByName: String): Result<Unit> {
        return try {
            val workerId = UUID.randomUUID().toString()
            val workerData = mapOf(
                "id" to workerId,
                "name" to workerName,
                "signInAt" to Timestamp.now(),
                "signedInById" to userId,
                "signedInByName" to signedInByName
            )
            
            // 1. Add to sub-collection
            permitsCollection.document(permitId).collection("worker_log")
                .document(workerId).set(workerData).await()
            
            // 2. Ensure status is active
            permitsCollection.document(permitId).update(
                mapOf(
                    "status" to "active",
                    "approvalStage" to "active",
                    "updatedAt" to Timestamp.now()
                )
            ).await()
            
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun workerSignOut(permitId: String, logEntryId: String): Result<Unit> {
        return try {
            permitsCollection.document(permitId).collection("worker_log")
                .document(logEntryId).update("signOutAt", Timestamp.now()).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    fun getWorkerLogFlow(permitId: String): Flow<List<WorkerModel>> {
        return permitsCollection.document(permitId).collection("worker_log")
            .orderBy("signInAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { it.toObjects(WorkerModel::class.java) }
    }

    suspend fun updatePermitStatus(permitId: String, status: String, comments: String): Result<Unit> {
        return try {
            val updates = mapOf("status" to status, "updatedAt" to Timestamp.now(), "statusComment" to comments)
            permitsCollection.document(permitId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun addAttachmentToPermit(permitId: String, attachment: Map<String, Any>): Result<Unit> {
        return try {
            permitsCollection.document(permitId).collection("attachments")
                .document(attachment["id"] as String).set(attachment).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    fun getAttachmentsFlow(permitId: String): Flow<List<Map<String, Any>>> {
        return permitsCollection.document(permitId).collection("attachments")
            .orderBy("uploadedAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot -> snapshot.documents.map { it.data ?: emptyMap() } }
    }

    private fun generatePermitNumber(): String {
        val timestamp = System.currentTimeMillis().toString().takeLast(6)
        return "PTW-${timestamp}"
    }
}
