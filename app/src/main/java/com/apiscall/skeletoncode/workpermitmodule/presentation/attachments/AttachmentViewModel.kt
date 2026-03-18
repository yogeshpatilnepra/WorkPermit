package com.apiscall.skeletoncode.workpermitmodule.presentation.attachments


import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Attachment
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.AuthRepository
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.PermitRepository
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
    private val permitRepository: PermitRepository,
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
            permitRepository.getPermitById(permitId).collect { permit ->
                if (permit != null) {
                    _attachments.value = Resource.Success(permit.attachments)
                } else {
                    _attachments.value = Resource.Error("Permit not found")
                }
            }
        }
    }

    fun uploadAttachment(permitId: String, file: File, mimeType: String) {
        viewModelScope.launch {
            _uploadResult.value = Resource.Loading

            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                _uploadResult.value = Resource.Error("User not logged in")
                return@launch
            }

            try {
                val attachment = Attachment(
                    id = UUID.randomUUID().toString(),
                    fileName = file.name,
                    filePath = file.absolutePath,
                    fileType = mimeType,
                    fileSize = file.length(),
                    uploadedBy = currentUser,
                    uploadedAt = Date()
                )

                val result = permitRepository.addAttachment(permitId, attachment)
                _uploadResult.value = if (result.isSuccess) {
                    Resource.Success(true)
                } else {
                    Resource.Error(result.exceptionOrNull()?.message ?: "Upload failed")
                }
            } catch (e: Exception) {
                _uploadResult.value = Resource.Error(e.message ?: "Upload failed")
            }
        }
    }

    fun uploadAttachment(permitId: String, uri: Uri, mimeType: String) {
        viewModelScope.launch {
            _uploadResult.value = Resource.Loading

            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                _uploadResult.value = Resource.Error("User not logged in")
                return@launch
            }

            try {
                val file = copyUriToFile(uri)
                val actualMimeType = getMimeType(uri) ?: mimeType

                val attachment = Attachment(
                    id = UUID.randomUUID().toString(),
                    fileName = getFileName(uri) ?: "file_${System.currentTimeMillis()}",
                    filePath = file.absolutePath,
                    fileType = actualMimeType,
                    fileSize = file.length(),
                    uploadedBy = currentUser,
                    uploadedAt = Date()
                )

                val result = permitRepository.addAttachment(permitId, attachment)
                _uploadResult.value = if (result.isSuccess) {
                    Resource.Success(true)
                } else {
                    Resource.Error(result.exceptionOrNull()?.message ?: "Upload failed")
                }
            } catch (e: Exception) {
                _uploadResult.value = Resource.Error(e.message ?: "Upload failed")
            }
        }
    }

    private fun copyUriToFile(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Could not open input stream")

        val fileName = getFileName(uri) ?: "file_${System.currentTimeMillis()}"
        val outputFile = File(context.getExternalFilesDir(null), fileName)

        FileOutputStream(outputFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }

        return outputFile
    }

    private fun getFileName(uri: Uri): String? {
        var fileName: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        fileName = it.getString(nameIndex)
                    }
                }
            }
        }
        if (fileName == null) {
            fileName = uri.path
            val cut = fileName?.lastIndexOf('/')
            if (cut != -1) {
                fileName = fileName?.substring(cut!! + 1)
            }
        }
        return fileName
    }

    private fun getMimeType(uri: Uri): String? {
        return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            context.contentResolver.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.lowercase())
        }
    }

    fun resetState() {
        _uploadResult.value = Resource.Idle
    }
}