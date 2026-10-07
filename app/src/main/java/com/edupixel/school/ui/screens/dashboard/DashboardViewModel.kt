package com.edupixel.school.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edupixel.school.core.network.NetworkResult
import com.edupixel.school.core.state.AppContextState
import com.edupixel.school.data.remote.models.ClassDashboardResponse
import com.edupixel.school.data.remote.models.School
import com.edupixel.school.data.remote.models.SchoolClass
import com.edupixel.school.data.repository.ClassRepository
import com.edupixel.school.data.repository.ReportRepository
import com.edupixel.school.data.repository.SchoolRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(
        val schools: List<School>,
        val classes: List<SchoolClass>,
        val selectedSchool: School?,
        val selectedClass: SchoolClass?,
        val classDashboard: ClassDashboardResponse?
    ) : DashboardUiState
    data class Error(val message: String, val isNetworkError: Boolean) : DashboardUiState
}

class DashboardViewModel(
    private val schoolRepository: SchoolRepository = SchoolRepository(),
    private val classRepository: ClassRepository = ClassRepository(),
    private val reportRepository: ReportRepository = ReportRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading

            val schoolsRes = schoolRepository.getSchools()
            if (schoolsRes is NetworkResult.Error) {
                _uiState.value = DashboardUiState.Error(schoolsRes.message, schoolsRes.isNetworkError)
                return@launch
            }

            val schools = (schoolsRes as NetworkResult.Success).data
            val currentSchool = AppContextState.selectedSchool.value ?: schools.firstOrNull()
            if (AppContextState.selectedSchool.value == null && currentSchool != null) {
                AppContextState.setSelectedSchool(currentSchool)
            }

            var classes = emptyList<SchoolClass>()
            if (currentSchool != null) {
                val classesRes = classRepository.getClasses(currentSchool.id)
                if (classesRes is NetworkResult.Success) {
                    classes = classesRes.data
                }
            }

            val currentClass = AppContextState.selectedClass.value ?: classes.firstOrNull()
            if (AppContextState.selectedClass.value == null && currentClass != null) {
                AppContextState.setSelectedClass(currentClass)
            }

            var classDashboard: ClassDashboardResponse? = null
            if (currentClass != null) {
                val dashRes = reportRepository.getClassDashboard(currentClass.id)
                if (dashRes is NetworkResult.Success) {
                    classDashboard = dashRes.data
                }
            }

            _uiState.value = DashboardUiState.Success(
                schools = schools,
                classes = classes,
                selectedSchool = currentSchool,
                selectedClass = currentClass,
                classDashboard = classDashboard
            )
        }
    }

    fun selectSchool(school: School) {
        AppContextState.setSelectedSchool(school)
        loadData()
    }

    fun selectClass(schoolClass: SchoolClass) {
        AppContextState.setSelectedClass(schoolClass)
        loadData()
    }
}
