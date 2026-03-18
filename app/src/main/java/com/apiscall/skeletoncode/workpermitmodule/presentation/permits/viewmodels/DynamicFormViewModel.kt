package com.apiscall.skeletoncode.workpermitmodule.presentation.permits.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apiscall.skeletoncode.workpermitmodule.domain.models.FormField
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitType
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.PermitRepository
import com.apiscall.skeletoncode.workpermitmodule.utils.FormDataConverter
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DynamicFormViewModel @Inject constructor(
    private val permitRepository: PermitRepository
) : ViewModel() {

    private val _formFields = MutableStateFlow<Resource<List<FormField>>>(Resource.Loading)
    val formFields: StateFlow<Resource<List<FormField>>> = _formFields.asStateFlow()

    private val _submitResult = MutableStateFlow<Resource<Boolean>>(Resource.Idle)
    val submitResult: StateFlow<Resource<Boolean>> = _submitResult.asStateFlow()

    private val fieldValues = mutableMapOf<String, String>()
    private val requiredFields = mutableListOf<String>()

    fun loadFormFields(permitType: PermitType) {
        viewModelScope.launch {
            _formFields.value = Resource.Loading
            try {
                permitRepository.getPermitForm(permitType)
                    .catch { e ->
                        _formFields.value = Resource.Error(e.message ?: "Error loading form")
                    }
                    .collect { fields ->
                        _formFields.value = Resource.Success(fields)
                        requiredFields.clear()
                        requiredFields.addAll(fields.filter { it.isRequired }.map { it.id })
                    }
            } catch (e: Exception) {
                _formFields.value = Resource.Error(e.message ?: "Error loading form")
            }
        }
    }

    fun updateFieldValue(fieldId: String, value: String) {
        fieldValues[fieldId] = value
    }

    fun getFormDataAsJson(): String = FormDataConverter.fromMap(fieldValues)

    fun validateForm(): Boolean = requiredFields.all { !fieldValues[it].isNullOrBlank() }

    fun saveDraft() {
        _submitResult.value = Resource.Success(true)
    }

    fun submitForm() {
        viewModelScope.launch {
            _submitResult.value = Resource.Loading
            try {
                kotlinx.coroutines.delay(1000) // Simulate API call
                _submitResult.value = Resource.Success(true)
            } catch (e: Exception) {
                _submitResult.value = Resource.Error(e.message ?: "Submission failed")
            }
        }
    }
}