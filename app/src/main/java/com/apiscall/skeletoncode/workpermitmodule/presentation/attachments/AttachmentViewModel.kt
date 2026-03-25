package com.apiscall.skeletoncode.workpermitmodule.presentation.attachments

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apiscall.skeletoncode.workpermitmodule.data.repository.FirebaseRepository
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Attachment
import com.apiscall.skeletoncode.workpermitmodule.domain.models.User
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.AuthRepository
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AttachmentViewModel @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _attachments = MutableStateFlow<Resource<List<Attachment>>>(Resource.Loading)
    val attachments: StateFlow<Resource<List<Attachment>>> = _attachments.asStateFlow()

    private val _uploadResult = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val uploadResult: StateFlow<Resource<Boolean>> = _uploadResult.asStateFlow()

    fun loadAttachments(permitId: String) {
        viewModelScope.launch {
            _attachments.value = Resource.Loading
            firebaseRepository.getAttachmentsFlow(permitId).collect { attachmentMaps ->
                val attachmentsList = attachmentMaps.mapNotNull { map ->
                    try {
                        Attachment(
                            id = map["id"] as? String ?: return@mapNotNull null,
                            fileName = map["fileName"] as? String ?: "",
                            filePath = map["filePath"] as? String ?: "",
                            fileType = map["fileType"] as? String ?: "",
                            fileSize = (map["fileSize"] as? Long) ?: 0,
                            uploadedBy = User(
                                id = map["uploadedById"] as? String ?: "",
                                username = "",
                                email = "",
                                fullName = map["uploadedByName"] as? String ?: "",
                                role = com.apiscall.skeletoncode.workpermitmodule.domain.models.Role.WORKER,
                                department = "",
                                employeeId = ""
                            ),
                            uploadedAt = Date((map["uploadedAt"] as? Long) ?: System.currentTimeMillis())
                        )
                    } catch (e: Exception) { null }
                }
                _attachments.value = Resource.Success(attachmentsList)
            }
        }
    }

    fun uploadAttachment(permitId: String, file: File, mimeType: String) {
        viewModelScope.launch {
            _uploadResult.value = Resource.Loading
            val currentUser = authRepository.getCurrentUser() ?: run {
                _uploadResult.value = Resource.Error("User not logged in")
                return@launch
            }

            try {
                // Store file in private app storage (Free, No Firebase Storage needed)
                val internalDir = File(context.filesDir, "attachments/$permitId").apply { mkdirs() }
                val destFile = File(internalDir, "${UUID.randomUUID()}_${file.name}")
                file.copyTo(destFile)

                val attachment = mapOf(
                    "id" to UUID.randomUUID().toString(),
                    "fileName" to file.name,
                    "filePath" to destFile.absolutePath, // Storing local path
                    "fileType" to mimeType,
                    "fileSize" to destFile.length(),
                    "uploadedById" to currentUser.id,
                    "uploadedByName" to currentUser.fullName,
                    "uploadedAt" to System.currentTimeMillis()
                )

                val result = firebaseRepository.addAttachmentToPermit(permitId, attachment)
                _uploadResult.value = if (result.isSuccess) Resource.Success(true) else Resource.Error("Failed to list item")
            } catch (e: Exception) {
                _uploadResult.value = Resource.Error("Upload failed")
            }
        }
    }

    fun uploadAttachment(permitId: String, uri: Uri, mimeType: String) {
        viewModelScope.launch {
            _uploadResult.value = Resource.Loading
            val currentUser = authRepository.getCurrentUser() ?: run {
                _uploadResult.value = Resource.Error("User not logged in")
                return@launch
            }

            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: throw Exception("Cannot open file")
                val fileName = getFileName(uri) ?: "file_${System.currentTimeMillis()}"
                val internalDir = File(context.filesDir, "attachments/$permitId").apply { mkdirs() }
                val destFile = File(internalDir, "${UUID.randomUUID()}_$fileName")
                
                FileOutputStream(destFile).use { output -> inputStream.copyTo(output) }

                val attachment = mapOf(
                    "id" to UUID.randomUUID().toString(),
                    "fileName" to fileName,
                    "filePath" to destFile.absolutePath,
                    "fileType" to (getMimeType(uri) ?: mimeType),
                    "fileSize" to destFile.length(),
                    "uploadedById" to currentUser.id,
                    "uploadedByName" to currentUser.fullName,
                    "uploadedAt" to System.currentTimeMillis()
                )

                val result = firebaseRepository.addAttachmentToPermit(permitId, attachment)
                _uploadResult.value = if (result.isSuccess) Resource.Success(true) else Resource.Error("Failed to list item")
            } catch (e: Exception) {
                _uploadResult.value = Resource.Error("Upload failed")
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        var fileName: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) fileName = cursor.getString(nameIndex)
                }
            }
        }
        return fileName ?: uri.path?.substringAfterLast('/')
    }

    private fun getMimeType(uri: Uri): String? {
        return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            context.contentResolver.getType(uri)
        } else {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString()).lowercase())
        }
    }

    fun resetState() { _uploadResult.value = Resource.Idle }
}
