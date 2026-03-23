package com.apiscall.skeletoncode.workpermitmodule.data.repository

import com.apiscall.skeletoncode.workpermitmodule.domain.models.ApprovalStage
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRepository @Inject constructor() {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val permitsCollection = db.collection("permits")

    // Get all permits
    fun getPermitsFlow(): Flow<List<PermitModel>> {
        return permitsCollection.orderBy("createdAt", Query.Direction.DESCENDING).snapshots()
            .map { snapshot ->
                snapshot.toObjects(PermitModel::class.java)
            }
    }

    // Get permits by requestor (my permits)
    fun getPermitsByRequestorFlow(requestorId: String): Flow<List<PermitModel>> {
        return permitsCollection.whereEqualTo("requestorId", requestorId)
            .orderBy("createdAt", Query.Direction.DESCENDING).snapshots().map { snapshot ->
                snapshot.toObjects(PermitModel::class.java)
            }
    }

    // Get permits by status
    fun getPermitsByStatusFlow(status: String): Flow<List<PermitModel>> {
        return permitsCollection.whereEqualTo("status", status)
            .orderBy("createdAt", Query.Direction.DESCENDING).snapshots().map { snapshot ->
                snapshot.toObjects(PermitModel::class.java)
            }
    }

    // Get single permit by ID
    fun getPermitById(permitId: String): Flow<PermitModel?> {
        return permitsCollection.document(permitId).snapshots().map { snapshot ->
            snapshot.toObject(PermitModel::class.java)
        }
    }

    // Create new permit
    suspend fun createPermit(permit: PermitModel): Result<PermitModel> {
        return try {
            val id = UUID.randomUUID().toString()
            val permitNumber = generatePermitNumber()
            val newPermit = permit.copy(
                id = id,
                permitNumber = permitNumber,
                status = "draft",
                approvalStage = ApprovalStage.ISSUER_REVIEW,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            permitsCollection.document(id).set(newPermit).await()
            Result.success(newPermit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Submit permit for approval
    suspend fun submitPermit(permitId: String): Result<Unit> {
        return try {
            val updates = hashMapOf<String, Any>(
                "status" to "submitted",
                "approvalStage" to ApprovalStage.ISSUER_REVIEW,
                "updatedAt" to Timestamp.now()
            )
            permitsCollection.document(permitId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Approve permit at current stage
    suspend fun approvePermit(
        permitId: String, role: String, userId: String, userName: String, comments: String?
    ): Result<PermitModel> {
        return try {
            val permitDoc = permitsCollection.document(permitId).get().await()
            val currentPermit = permitDoc.toObject(PermitModel::class.java)

            if (currentPermit == null) {
                return Result.failure(Exception("Permit not found"))
            }

            val updates = mutableMapOf<String, Any>()

            when (role) {
                "issuer" -> {
                    updates["issuerId"] = userId
                    updates["issuerName"] = userName
                    updates["issuerReviewedAt"] = Timestamp.now()
                    updates["issuerComments"] = comments ?: ""
                    updates["approvalStage"] = ApprovalStage.EHS_REVIEW
                }

                "ehs" -> {
                    updates["ehsId"] = userId
                    updates["ehsName"] = userName
                    updates["ehsReviewedAt"] = Timestamp.now()
                    updates["ehsComments"] = comments ?: ""
                    updates["approvalStage"] = ApprovalStage.AREA_OWNER_REVIEW
                }

                "area_owner" -> {
                    updates["areaOwnerId"] = userId
                    updates["areaOwnerName"] = userName
                    updates["areaOwnerReviewedAt"] = Timestamp.now()
                    updates["areaOwnerComments"] = comments ?: ""
                    updates["approvalStage"] = ApprovalStage.ISSUED
                    updates["status"] = "issued"
                }

                else -> return Result.failure(Exception("Invalid role for approval"))
            }

            updates["updatedAt"] = Timestamp.now()
            permitsCollection.document(permitId).update(updates).await()

            val updatedPermit = permitsCollection.document(permitId).get().await()
            Result.success(updatedPermit.toObject(PermitModel::class.java)!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Reject permit
    suspend fun rejectPermit(
        permitId: String,
        role: String,
        userId: String,
        userName: String,
        comments: String
    ): Result<PermitModel> {
        return try {
            val updates = hashMapOf<String, Any>(
                "status" to "rejected",
                "approvalStage" to "rejected",
                "updatedAt" to Timestamp.now(),
                "${role}Id" to userId,
                "${role}Name" to userName,
                "${role}ReviewedAt" to Timestamp.now(),
                "${role}Comments" to comments
            )

            permitsCollection.document(permitId).update(updates).await()

            val updatedPermit = permitsCollection.document(permitId).get().await()
            Result.success(updatedPermit.toObject(PermitModel::class.java)!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Send back permit for revision
    suspend fun sendBackPermit(
        permitId: String, role: String, userId: String, userName: String, comments: String
    ): Result<PermitModel> {
        return try {
            val updates = hashMapOf<String, Any>(
                "status" to "sent_back",
                "approvalStage" to ApprovalStage.SENT_BACK,
                "updatedAt" to Timestamp.now()
            )

            when (role) {
                "issuer" -> {
                    updates["issuerComments"] = comments
                    updates["issuerReviewedAt"] = Timestamp.now()
                }

                "ehs" -> {
                    updates["ehsComments"] = comments
                    updates["ehsReviewedAt"] = Timestamp.now()
                }

                "area_owner" -> {
                    updates["areaOwnerComments"] = comments
                    updates["areaOwnerReviewedAt"] = Timestamp.now()
                }
            }

            permitsCollection.document(permitId).update(updates).await()

            val updatedPermit = permitsCollection.document(permitId).get().await()
            Result.success(updatedPermit.toObject(PermitModel::class.java)!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Close permit (Supervisor)
    suspend fun closePermit(
        permitId: String, supervisorId: String, supervisorName: String, comments: String
    ): Result<PermitModel> {
        return try {
            val updates = hashMapOf<String, Any>(
                "status" to "closed",
                "approvalStage" to ApprovalStage.CLOSED,
                "supervisorId" to supervisorId,
                "supervisorName" to supervisorName,
                "closureComments" to comments,
                "closedAt" to Timestamp.now(),
                "updatedAt" to Timestamp.now()
            )

            permitsCollection.document(permitId).update(updates).await()

            val updatedPermit = permitsCollection.document(permitId).get().await()
            Result.success(updatedPermit.toObject(PermitModel::class.java)!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Upload attachment
    suspend fun uploadAttachment(permitId: String, file: File, fileName: String): Result<String> {
        return try {
            val storageRef = storage.reference.child("permits").child(permitId).child("attachments")
                .child(fileName)

            storageRef.putFile(android.net.Uri.fromFile(file)).await()
            val downloadUrl = storageRef.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Add attachment reference to permit
    suspend fun addAttachmentToPermit(
        permitId: String, attachment: Map<String, Any>
    ): Result<Unit> {
        return try {
            permitsCollection.document(permitId).collection("attachments")
                .document(attachment["id"] as String).set(attachment).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get attachments for permit
    fun getAttachmentsFlow(permitId: String): Flow<List<Map<String, Any>>> {
        return permitsCollection.document(permitId).collection("attachments")
            .orderBy("uploadedAt", Query.Direction.DESCENDING).snapshots().map { snapshot ->
                snapshot.documents.map { it.data ?: emptyMap() }
            }
    }

    private fun generatePermitNumber(): String {
        val timestamp = System.currentTimeMillis().toString().takeLast(6)
        return "PTW-${timestamp}"
    }

    suspend fun updatePermitStatus(
        permitId: String, status: String, comments: String
    ): Result<Unit> {
        return try {
            val updates = hashMapOf<String, Any>(
                "status" to status, "updatedAt" to Timestamp.now(), "statusComment" to comments
            )
            permitsCollection.document(permitId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getPermitsByApprovalStageFlow(stage: String): Flow<List<PermitModel>> {
        val excludedStatuses = listOf("rejected", "sent_back", "closed")
        
        // Failsafe query to avoid all composite index requirements
        // We fetch all documents (ordered by creation date) and filter in memory.
        // This is safe for moderate datasets and guarantees no "missing index" errors.
        return permitsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(PermitModel::class.java).filter { model ->
                    model.approvalStage == stage && model.status !in excludedStatuses
                }
            }
    }
}
