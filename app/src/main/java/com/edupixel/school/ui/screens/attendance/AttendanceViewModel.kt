package com.edupixel.school.ui.screens.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edupixel.school.core.network.NetworkResult
import com.edupixel.school.core.state.AppContextState
import com.edupixel.school.data.remote.models.*
import com.edupixel.school.data.repository.AttendanceRepository
import com.edupixel.school.data.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AttendanceUiState {
    data object Loading : AttendanceUiState
    data class Success(
        val schoolClass: SchoolClass?,
        val students: List<Student>,
        val activePeriod: String // "midterm" or "annual"
    ) : AttendanceUiState
    data class Error(val message: String, val isNetworkError: Boolean) : AttendanceUiState
}

class AttendanceViewModel(
    private val studentRepository: StudentRepository = StudentRepository(),
    private val attendanceRepository: AttendanceRepository = AttendanceRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AttendanceUiState>(AttendanceUiState.Loading)
    val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()

    private val _activePeriod = MutableStateFlow("midterm")
    val activePeriod: StateFlow<String> = _activePeriod.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage.asStateFlow()

    init {
        loadData()
    }

    fun setPeriod(period: String) {
        _activePeriod.value = period
        val current = _uiState.value
        if (current is AttendanceUiState.Success) {
            _uiState.value = current.copy(activePeriod = period)
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = AttendanceUiState.Loading
            val cls = AppContextState.selectedClass.value
            if (cls == null) {
                _uiState.value = AttendanceUiState.Success(null, emptyList(), _activePeriod.value)
                return@launch
            }

            when (val res = studentRepository.getStudents(cls.id)) {
                is NetworkResult.Success -> {
                    _uiState.value = AttendanceUiState.Success(cls, res.data, _activePeriod.value)
                }
                is NetworkResult.Error -> {
                    _uiState.value = AttendanceUiState.Error(res.message, res.isNetworkError)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun submitSingleAttendance(
        studentId: Int,
        attendanceIn: AttendanceIn,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            when (val res = attendanceRepository.upsertAttendance(studentId, attendanceIn)) {
                is NetworkResult.Success -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Attendance record updated."
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

    fun submitBulkAttendance(
        classId: Int,
        items: List<AttendanceBulkItem>,
        onSuccess: () -> Unit
    ) {
        if (items.isEmpty()) return
        viewModelScope.launch {
            _isSubmitting.value = true
            when (val res = attendanceRepository.bulkUpsertAttendance(classId, items)) {
                is NetworkResult.Success -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "${res.data.size} attendance records updated in bulk."
                    onSuccess()
                }
                is NetworkResult.Error -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Bulk error: ${res.message}"
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun clearMessage() {
        _operationMessage.value = null
    }
}
