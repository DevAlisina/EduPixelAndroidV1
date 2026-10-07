package com.edupixel.school.ui.screens.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edupixel.school.core.network.NetworkResult
import com.edupixel.school.core.state.AppContextState
import com.edupixel.school.data.remote.models.ClassResultsResponse
import com.edupixel.school.data.remote.models.SchoolClass
import com.edupixel.school.data.remote.models.StudentResult
import com.edupixel.school.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MidtermResultsUiState {
    data object Loading : MidtermResultsUiState
    data class Success(
        val schoolClass: SchoolClass?,
        val resultsResponse: ClassResultsResponse,
        val filteredStudents: List<StudentResult>,
        val searchQuery: String,
        val statusFilter: String?
    ) : MidtermResultsUiState
    data class Error(val message: String, val isNetworkError: Boolean) : MidtermResultsUiState
}

class MidtermResultsViewModel(
    private val reportRepository: ReportRepository = ReportRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<MidtermResultsUiState>(MidtermResultsUiState.Loading)
    val uiState: StateFlow<MidtermResultsUiState> = _uiState.asStateFlow()

    private var currentResponse: ClassResultsResponse? = null
    private var searchQuery = ""
    private var statusFilter: String? = null

    init {
        loadMidtermResults()
    }

    fun loadMidtermResults() {
        viewModelScope.launch {
            _uiState.value = MidtermResultsUiState.Loading
            val cls = AppContextState.selectedClass.value
            if (cls == null) {
                _uiState.value = MidtermResultsUiState.Success(
                    schoolClass = null,
                    resultsResponse = ClassResultsResponse(classId = 0),
                    filteredStudents = emptyList(),
                    searchQuery = "",
                    statusFilter = null
                )
                return@launch
            }

            when (val res = reportRepository.getClassResults(cls.id)) {
                is NetworkResult.Success -> {
                    currentResponse = res.data
                    applyFilters()
                }
                is NetworkResult.Error -> {
                    _uiState.value = MidtermResultsUiState.Error(res.message, res.isNetworkError)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun onSearchChanged(query: String) {
        searchQuery = query
        applyFilters()
    }

    fun onStatusFilterChanged(filter: String?) {
        statusFilter = filter
        applyFilters()
    }

    private fun applyFilters() {
        val resp = currentResponse ?: return
        val cls = AppContextState.selectedClass.value
        val list = resp.students.filter { student ->
            val matchesQuery = searchQuery.isBlank() ||
                    student.studentName.contains(searchQuery, ignoreCase = true) ||
                    student.attendanceNo?.toString() == searchQuery.trim()

            val matchesStatus = statusFilter == null || student.midResult == statusFilter

            matchesQuery && matchesStatus
        }

        _uiState.value = MidtermResultsUiState.Success(
            schoolClass = cls,
            resultsResponse = resp,
            filteredStudents = list,
            searchQuery = searchQuery,
            statusFilter = statusFilter
        )
    }
}
