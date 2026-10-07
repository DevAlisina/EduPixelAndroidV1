package com.edupixel.school.ui.screens.scores

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edupixel.school.core.network.NetworkResult
import com.edupixel.school.core.state.AppContextState
import com.edupixel.school.data.remote.models.*
import com.edupixel.school.data.repository.ScoreRepository
import com.edupixel.school.data.repository.StudentRepository
import com.edupixel.school.data.repository.SubjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ScoreEntryUiState {
    data object Loading : ScoreEntryUiState
    data class Success(
        val schoolClass: SchoolClass?,
        val students: List<Student>,
        val subjects: List<Subject>
    ) : ScoreEntryUiState
    data class Error(val message: String, val isNetworkError: Boolean) : ScoreEntryUiState
}

class ScoreEntryViewModel(
    private val studentRepository: StudentRepository = StudentRepository(),
    private val subjectRepository: SubjectRepository = SubjectRepository(),
    private val scoreRepository: ScoreRepository = ScoreRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScoreEntryUiState>(ScoreEntryUiState.Loading)
    val uiState: StateFlow<ScoreEntryUiState> = _uiState.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = ScoreEntryUiState.Loading
            val cls = AppContextState.selectedClass.value
            if (cls == null) {
                _uiState.value = ScoreEntryUiState.Success(null, emptyList(), emptyList())
                return@launch
            }

            val studentsRes = studentRepository.getStudents(cls.id)
            val subjectsRes = subjectRepository.getSubjects(cls.id, activeOnly = true)

            if (studentsRes is NetworkResult.Error) {
                _uiState.value = ScoreEntryUiState.Error(studentsRes.message, studentsRes.isNetworkError)
                return@launch
            }
            if (subjectsRes is NetworkResult.Error) {
                _uiState.value = ScoreEntryUiState.Error(subjectsRes.message, subjectsRes.isNetworkError)
                return@launch
            }

            val students = (studentsRes as NetworkResult.Success).data
            val subjects = (subjectsRes as NetworkResult.Success).data

            _uiState.value = ScoreEntryUiState.Success(cls, students, subjects)
        }
    }

    fun submitSingleScore(
        studentId: Int,
        subjectId: Int,
        mid: Double?,
        annual: Double?,
        note: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val scoreIn = ScoreIn(
                studentId = studentId,
                subjectId = subjectId,
                mid = mid,
                annual = annual,
                note = note?.trim()?.ifEmpty { null }
            )
            when (val res = scoreRepository.upsertScore(scoreIn)) {
                is NetworkResult.Success -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Score saved successfully."
                    onSuccess()
                }
                is NetworkResult.Error -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Error saving score: ${res.message}"
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun submitBulkScores(
        classId: Int,
        items: List<ScoreIn>,
        onSuccess: () -> Unit
    ) {
        if (items.isEmpty()) return
        viewModelScope.launch {
            _isSubmitting.value = true
            when (val res = scoreRepository.bulkUpsertScores(classId, items)) {
                is NetworkResult.Success -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "${res.data.size} scores saved successfully in bulk."
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
