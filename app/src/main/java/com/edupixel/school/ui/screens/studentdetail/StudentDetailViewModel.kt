package com.edupixel.school.ui.screens.studentdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edupixel.school.core.network.NetworkResult
import com.edupixel.school.data.remote.models.*
import com.edupixel.school.data.repository.AttendanceRepository
import com.edupixel.school.data.repository.ReportRepository
import com.edupixel.school.data.repository.ScoreRepository
import com.edupixel.school.data.repository.StudentRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface StudentDetailUiState {
    data object Loading : StudentDetailUiState
    data class Success(
        val student: Student,
        val result: StudentResult?,
        val attendanceList: List<Attendance>,
        val scores: List<Score>,
        val reportCard: ReportCardResponse?
    ) : StudentDetailUiState
    data class Error(val message: String, val isNetworkError: Boolean) : StudentDetailUiState
}

class StudentDetailViewModel(
    private val studentRepository: StudentRepository = StudentRepository(),
    private val reportRepository: ReportRepository = ReportRepository(),
    private val attendanceRepository: AttendanceRepository = AttendanceRepository(),
    private val scoreRepository: ScoreRepository = ScoreRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<StudentDetailUiState>(StudentDetailUiState.Loading)
    val uiState: StateFlow<StudentDetailUiState> = _uiState.asStateFlow()

    fun loadStudentDetail(studentId: Int) {
        viewModelScope.launch {
            _uiState.value = StudentDetailUiState.Loading

            val studentDeferred = async { studentRepository.getStudent(studentId) }
            val resultDeferred = async { reportRepository.getStudentResult(studentId) }
            val attendanceDeferred = async { attendanceRepository.getStudentAttendance(studentId) }
            val scoresDeferred = async { scoreRepository.getStudentScores(studentId) }
            val reportCardDeferred = async { reportRepository.getStudentReportCard(studentId) }

            val studentRes = studentDeferred.await()
            if (studentRes is NetworkResult.Error) {
                _uiState.value = StudentDetailUiState.Error(studentRes.message, studentRes.isNetworkError)
                return@launch
            }

            val student = (studentRes as NetworkResult.Success).data
            val result = resultDeferred.await().getOrNull()
            val attendanceList = attendanceDeferred.await().getOrNull() ?: emptyList()
            val scores = scoresDeferred.await().getOrNull() ?: emptyList()
            val reportCard = reportCardDeferred.await().getOrNull()

            _uiState.value = StudentDetailUiState.Success(
                student = student,
                result = result,
                attendanceList = attendanceList,
                scores = scores,
                reportCard = reportCard
            )
        }
    }
}
