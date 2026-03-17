package com.apiscall.skeletoncode.workpermitmodule.presentation.attachments


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Attachment
import com.apiscall.skeletoncode.workpermitmodule.domain.models.User
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.AuthRepository
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.PermitRepository
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AttachmentViewModel @Inject constructor(
    private val permitRepository: PermitRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uploadResult = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val uploadResult: StateFlow<Resource<Boolean>> = _uploadResult.asStateFlow()

    private var selectedFileType: String? = null
    private var currentUser: User? = null

    init {
        viewModelScope.launch {
            currentUser = authRepository.getCurrentUser()
        }
    }

    fun setSelectedFile(fileType: String) {
        selectedFileType = fileType
    }

    fun uploadAttachment(permitId: String, description: String) {
        viewModelScope.launch {
            if (selectedFileType == null) {
                _uploadResult.value = Resource.Error("Please select a file")
                return@launch
            }

            if (currentUser == null) {
                _uploadResult.value = Resource.Error("User not logged in")
                return@launch
            }

            _uploadResult.value = Resource.Loading

            // Create mock attachment
            val attachment = Attachment(
                id = UUID.randomUUID().toString(),
                fileName = "sample_${selectedFileType!!.lowercase()}.${selectedFileType!!.lowercase()}",
                filePath = "/mock/path/file.${selectedFileType!!.lowercase()}",
                fileType = selectedFileType!!,
                fileSize = 1024 * 1024, // 1MB
                uploadedBy = currentUser!!,
                uploadedAt = Date(),
            )

            // Simulate upload delay
            kotlinx.coroutines.delay(1500)

            val result = permitRepository.addAttachment(permitId, attachment)
            _uploadResult.value = if (result.isSuccess) {
                Resource.Success(true)
            } else {
                Resource.Error(result.exceptionOrNull()?.message ?: "Upload failed")
            }
        }
    }
}