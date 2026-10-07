package com.edupixel.school.ui.screens.years

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edupixel.school.core.network.NetworkResult
import com.edupixel.school.core.state.AppContextState
import com.edupixel.school.data.remote.models.AcademicYear
import com.edupixel.school.data.remote.models.AcademicYearIn
import com.edupixel.school.data.remote.models.School
import com.edupixel.school.data.repository.AcademicYearRepository
import com.edupixel.school.data.repository.SchoolRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface YearsUiState {
    data object Loading : YearsUiState
    data class Success(
        val school: School?,
        val years: List<AcademicYear>,
        val activeYearId: Int?
    ) : YearsUiState
    data class Error(val message: String, val isNetworkError: Boolean) : YearsUiState
}

class AcademicYearsViewModel(
    private val yearRepository: AcademicYearRepository = AcademicYearRepository(),
    private val schoolRepository: SchoolRepository = SchoolRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<YearsUiState>(YearsUiState.Loading)
    val uiState: StateFlow<YearsUiState> = _uiState.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage.asStateFlow()

    init {
        loadYears()
    }

    fun loadYears() {
        viewModelScope.launch {
            _uiState.value = YearsUiState.Loading
            var school = AppContextState.selectedSchool.value
            if (school == null) {
                val schoolsRes = schoolRepository.getSchools()
                if (schoolsRes is NetworkResult.Success && schoolsRes.data.isNotEmpty()) {
                    school = schoolsRes.data.first()
                    AppContextState.setSelectedSchool(school)
                }
            }

            if (school == null) {
                _uiState.value = YearsUiState.Success(null, emptyList(), null)
                return@launch
            }

            when (val res = yearRepository.getAcademicYears(school.id)) {
                is NetworkResult.Success -> {
                    val activeId = res.data.firstOrNull { it.isActive }?.id
                    _uiState.value = YearsUiState.Success(school, res.data, activeId)
                }
                is NetworkResult.Error -> {
                    _uiState.value = YearsUiState.Error(res.message, res.isNetworkError)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun createYear(solarYear: Int, qamariYear: Int?, label: String?, isActive: Boolean, onSuccess: () -> Unit) {
        val school = AppContextState.selectedSchool.value ?: return
        viewModelScope.launch {
            _isSubmitting.value = true
            val yearIn = AcademicYearIn(
                schoolId = school.id,
                solarYear = solarYear,
                qamariYear = qamariYear,
                label = label,
                isActive = isActive
            )
            when (val res = yearRepository.createAcademicYear(yearIn)) {
                is NetworkResult.Success -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Academic year created."
                    if (res.data.isActive) {
                        AppContextState.setSelectedYear(res.data)
                    }
                    loadYears()
                    onSuccess()
                }
                is NetworkResult.Error -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Error: ${res.message}"
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun updateYear(yearId: Int, solarYear: Int, qamariYear: Int?, label: String?, isActive: Boolean, onSuccess: () -> Unit) {
        val school = AppContextState.selectedSchool.value ?: return
        viewModelScope.launch {
            _isSubmitting.value = true
            val yearIn = AcademicYearIn(
                schoolId = school.id,
                solarYear = solarYear,
                qamariYear = qamariYear,
                label = label,
                isActive = isActive
            )
            when (val res = yearRepository.updateAcademicYear(yearId, yearIn)) {
                is NetworkResult.Success -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Academic year updated."
                    loadYears()
                    onSuccess()
                }
                is NetworkResult.Error -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Error: ${res.message}"
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun deleteYear(yearId: Int) {
        viewModelScope.launch {
            _isSubmitting.value = true
            when (val res = yearRepository.deleteAcademicYear(yearId)) {
                is NetworkResult.Success -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Academic year deleted."
                    loadYears()
                }
                is NetworkResult.Error -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Error: ${res.message}"
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun clearMessage() {
        _operationMessage.value = null
    }
}
