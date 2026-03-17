package com.apiscall.skeletoncode.workpermitmodule.presentation.search


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apiscall.skeletoncode.workpermitmodule.domain.models.Permit
import com.apiscall.skeletoncode.workpermitmodule.domain.models.PermitStatus
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
class SearchViewModel @Inject constructor(
    private val permitRepository: PermitRepository
) : ViewModel() {

    private val _searchResults = MutableStateFlow<Resource<List<Permit>>>(Resource.Idle)
    val searchResults: StateFlow<Resource<List<Permit>>> = _searchResults.asStateFlow()

    private var currentStatus: PermitStatus? = null
    private var currentType: PermitType? = null
    private var dateFrom: Long? = null
    private var dateTo: Long? = null

    fun search(query: String) {
        viewModelScope.launch {
            _searchResults.value = Resource.Loading
            permitRepository.searchPermits(query).collect { permits ->
                _searchResults.value = Resource.Success(permits)
            }
        }
    }

    fun filterPermits(status: PermitStatus?, type: PermitType?) {
        this.currentStatus = status
        this.currentType = type

        viewModelScope.launch {
            _searchResults.value = Resource.Loading
            permitRepository.filterPermits(status, type, dateFrom, dateTo).collect { permits ->
                _searchResults.value = Resource.Success(permits)
            }
        }
    }

    fun setDateFrom(timestamp: Long) {
        this.dateFrom = timestamp
    }

    fun setDateTo(timestamp: Long) {
        this.dateTo = timestamp
    }

    fun clearFilters() {
        currentStatus = null
        currentType = null
        dateFrom = null
        dateTo = null
        _searchResults.value = Resource.Idle
    }
}