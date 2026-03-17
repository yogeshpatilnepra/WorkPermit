package com.apiscall.skeletoncode.workpermitmodule.presentation.permits.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apiscall.skeletoncode.workpermitmodule.domain.models.FormField
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitType
import com.apiscall.skeletoncode.workpermitmodule.domain.repository.PermitRepository
import com.apiscall.skeletoncode.workpermitmodule.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
            permitRepository.getPermitForm(permitType).collect { fields ->
                _formFields.value = Resource.Success(fields)
                requiredFields.clear()
                requiredFields.addAll(fields.filter { it.isRequired }.map { it.id })
            }
        }
    }

    fun updateFieldValue(fieldId: String, value: String) {
        fieldValues[fieldId] = value
    }

    fun validateForm(): Boolean {
        return requiredFields.all { fieldId ->
            !fieldValues[fieldId].isNullOrBlank()
        }
    }

    fun saveDraft() {
        // In a real app, save to local database
        _submitResult.value = Resource.Success(true)
    }

    fun submitForm() {
        viewModelScope.launch {
            _submitResult.value = Resource.Loading
            // Simulate API call
            kotlinx.coroutines.delay(1000)
            _submitResult.value = Resource.Success(true)
        }
    }
}