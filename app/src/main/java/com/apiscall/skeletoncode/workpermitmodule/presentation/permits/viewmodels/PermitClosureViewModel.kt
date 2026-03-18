package com.apiscall.skeletoncode.workpermitmodule.presentation.permits.viewmodels


import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Attachment
import com.apiscall.skeletoncode.workpermitmodule.domain.models.User
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
class PermitClosureViewModel @Inject constructor(
    private val permitRepository: PermitRepository,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _closureResult = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val closureResult: StateFlow<Resource<Boolean>> = _closureResult.asStateFlow()

    private val _attachments = MutableStateFlow<List<Attachment>>(emptyList())
    val attachments: StateFlow<List<Attachment>> = _attachments.asStateFlow()

    private val _uploadResult = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val uploadResult: StateFlow<Resource<Boolean>> = _uploadResult.asStateFlow()

    private var currentUser: User? = null

    init {
        viewModelScope.launch {
            currentUser = authRepository.getCurrentUser()
        }
    }

    fun addAttachment(file: File, mimeType: String) {
        viewModelScope.launch {
            _uploadResult.value = Resource.Loading

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
                    uploadedBy = currentUser!!,
                    uploadedAt = Date()
                )

                val currentList = _attachments.value.toMutableList()
                currentList.add(attachment)
                _attachments.value = currentList
                _uploadResult.value = Resource.Success(true)
            } catch (e: Exception) {
                _uploadResult.value = Resource.Error(e.message ?: "Upload failed")
            }
        }
    }

    fun addAttachment(uri: Uri, mimeType: String) {
        viewModelScope.launch {
            _uploadResult.value = Resource.Loading

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
                    uploadedBy = currentUser!!,
                    uploadedAt = Date()
                )

                val currentList = _attachments.value.toMutableList()
                currentList.add(attachment)
                _attachments.value = currentList
                _uploadResult.value = Resource.Success(true)
            } catch (e: Exception) {
                _uploadResult.value = Resource.Error(e.message ?: "Upload failed")
            }
        }
    }

    fun removeAttachment(attachmentId: String) {
        val currentList = _attachments.value.toMutableList()
        currentList.removeAll { it.id == attachmentId }
        _attachments.value = currentList
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
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
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

    fun closePermit(permitId: String, remarks: String) {
        viewModelScope.launch {
            _closureResult.value = Resource.Loading
            // Here you would also upload attachments to the permit
            // For now, just close the permit
            val result = permitRepository.closePermit(permitId, remarks)
            _closureResult.value = if (result.isSuccess) {
                Resource.Success(true)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Failed to close permit")
            }
        }
    }
}