package com.edupixel.school.ui.screens.schools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edupixel.school.core.network.NetworkResult
import com.edupixel.school.core.state.AppContextState
import com.edupixel.school.data.remote.models.School
import com.edupixel.school.data.remote.models.SchoolIn
import com.edupixel.school.data.repository.SchoolRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SchoolsUiState {
    data object Loading : SchoolsUiState
    data class Success(val schools: List<School>, val selectedSchoolId: Int?) : SchoolsUiState
    data class Error(val message: String, val isNetworkError: Boolean) : SchoolsUiState
}

class SchoolsViewModel(
    private val schoolRepository: SchoolRepository = SchoolRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<SchoolsUiState>(SchoolsUiState.Loading)
    val uiState: StateFlow<SchoolsUiState> = _uiState.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage.asStateFlow()

    init {
        loadSchools()
    }

    fun loadSchools() {
        viewModelScope.launch {
            _uiState.value = SchoolsUiState.Loading
            when (val res = schoolRepository.getSchools()) {
                is NetworkResult.Success -> {
                    val activeId = AppContextState.selectedSchool.value?.id
                    _uiState.value = SchoolsUiState.Success(res.data, activeId)
                    if (AppContextState.selectedSchool.value == null && res.data.isNotEmpty()) {
                        AppContextState.setSelectedSchool(res.data.first())
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.value = SchoolsUiState.Error(res.message, res.isNetworkError)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun selectSchool(school: School) {
        AppContextState.setSelectedSchool(school)
        val current = _uiState.value
        if (current is SchoolsUiState.Success) {
            _uiState.value = current.copy(selectedSchoolId = school.id)
        }
    }

    fun createSchool(schoolIn: SchoolIn, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            when (val res = schoolRepository.createSchool(schoolIn)) {
                is NetworkResult.Success -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "School '${res.data.name}' created successfully."
                    AppContextState.setSelectedSchool(res.data)
                    loadSchools()
                    onSuccess()
                }
                is NetworkResult.Error -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Error creating school: ${res.message}"
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun updateSchool(schoolId: Int, schoolIn: SchoolIn, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            when (val res = schoolRepository.updateSchool(schoolId, schoolIn)) {
                is NetworkResult.Success -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "School updated successfully."
                    if (AppContextState.selectedSchool.value?.id == schoolId) {
                        AppContextState.setSelectedSchool(res.data)
                    }
                    loadSchools()
                    onSuccess()
                }
                is NetworkResult.Error -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Error updating school: ${res.message}"
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun deleteSchool(schoolId: Int) {
        viewModelScope.launch {
            _isSubmitting.value = true
            when (val res = schoolRepository.deleteSchool(schoolId)) {
                is NetworkResult.Success -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "School deleted."
                    if (AppContextState.selectedSchool.value?.id == schoolId) {
                        AppContextState.setSelectedSchool(null)
                    }
                    loadSchools()
                }
                is NetworkResult.Error -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Error deleting school: ${res.message}"
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun clearMessage() {
        _operationMessage.value = null
    }
}
