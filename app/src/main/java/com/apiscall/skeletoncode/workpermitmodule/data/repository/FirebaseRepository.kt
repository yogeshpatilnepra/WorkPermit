package com.apiscall.skeletoncode.workpermitmodule.data.repository


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

    fun getPermitsFlow(): Flow<List<PermitModel>> {
        return permitsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(PermitModel::class.java)
            }
    }

    fun getPermitById(permitId: String): Flow<PermitModel?> {
        return permitsCollection.document(permitId).snapshots().map { snapshot ->
            snapshot.toObject(PermitModel::class.java)
        }
    }

    fun getPermitsByPlantFlow(plant: String): Flow<List<PermitModel>> {
        return if (plant == "All Plants") {
            getPermitsFlow()
        } else {
            permitsCollection
                .whereEqualTo("plant", plant)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(PermitModel::class.java)
                }
        }
    }

    fun getPermitsByStatusFlow(status: String): Flow<List<PermitModel>> {
        return if (status == "all") {
            getPermitsFlow()
        } else {
            permitsCollection
                .whereEqualTo("status", status)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(PermitModel::class.java)
                }
        }
    }

    fun getPermitsByTypeFlow(type: String): Flow<List<PermitModel>> {
        return if (type == "All Types") {
            getPermitsFlow()
        } else {
            permitsCollection
                .whereEqualTo("permitType", type)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(PermitModel::class.java)
                }
        }
    }

    fun getFilteredPermitsFlow(
        plant: String = "All Plants",
        status: String = "all",
        type: String = "All Types",
        searchQuery: String = ""
    ): Flow<List<PermitModel>> {
        var query: Query = permitsCollection

        // Apply filters
        if (plant != "All Plants") {
            query = query.whereEqualTo("plant", plant)
        }

        if (status != "all") {
            query = query.whereEqualTo("status", status)
        }

        if (type != "All Types") {
            query = query.whereEqualTo("permitType", type)
        }

        query = query.orderBy("createdAt", Query.Direction.DESCENDING)

        return query.snapshots().map { snapshot ->
            val permits = snapshot.toObjects(PermitModel::class.java)
            if (searchQuery.isNotEmpty()) {
                permits.filter {
                    it.permitNumber.contains(searchQuery, ignoreCase = true) ||
                            it.title.contains(searchQuery, ignoreCase = true)
                }
            } else {
                permits
            }
        }
    }

    suspend fun createPermit(permit: PermitModel): Result<PermitModel> {
        return try {
            val id = UUID.randomUUID().toString()
            val permitNumber = generatePermitNumber()
            val newPermit = permit.copy(
                id = id,
                permitNumber = permitNumber,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            permitsCollection.document(id).set(newPermit).await()
            Result.success(newPermit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePermit(permit: PermitModel): Result<PermitModel> {
        return try {
            val updatedPermit = permit.copy(updatedAt = Timestamp.now())
            permitsCollection.document(permit.id).set(updatedPermit).await()
            Result.success(updatedPermit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePermitStatus(
        permitId: String,
        status: String,
        comments: String? = null
    ): Result<Unit> {
        return try {
            val updates = hashMapOf<String, Any>(
                "status" to status,
                "updatedAt" to Timestamp.now()
            )
            comments?.let {
                updates["comments"] = it
            }
            permitsCollection.document(permitId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePermit(permitId: String): Result<Unit> {
        return try {
            permitsCollection.document(permitId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadAttachment(permitId: String, file: File, fileName: String): Result<String> {
        return try {
            val storageRef = storage.reference
                .child("permits")
                .child(permitId)
                .child("attachments")
                .child(fileName)

            val uploadTask = storageRef.putFile(android.net.Uri.fromFile(file)).await()
            val downloadUrl = storageRef.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addAttachmentToPermit(
        permitId: String,
        attachment: Map<String, Any>
    ): Result<Unit> {
        return try {
            permitsCollection
                .document(permitId)
                .collection("attachments")
                .document(attachment["id"] as String)
                .set(attachment)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAttachmentsFlow(permitId: String): Flow<List<Map<String, Any>>> {
        return permitsCollection
            .document(permitId)
            .collection("attachments")
            .orderBy("uploadedAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { it.data ?: emptyMap() }
            }
    }

    private fun generatePermitNumber(): String {
        val timestamp = System.currentTimeMillis().toString().takeLast(6)
        return "PTW-${timestamp}"
    }
}