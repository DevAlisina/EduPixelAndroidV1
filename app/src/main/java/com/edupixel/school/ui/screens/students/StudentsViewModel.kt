package com.edupixel.school.ui.screens.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edupixel.school.core.network.NetworkResult
import com.edupixel.school.core.state.AppContextState
import com.edupixel.school.data.remote.models.SchoolClass
import com.edupixel.school.data.remote.models.Student
import com.edupixel.school.data.remote.models.StudentIn
import com.edupixel.school.data.repository.StudentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface StudentsUiState {
    data object Loading : StudentsUiState
    data class Success(
        val schoolClass: SchoolClass?,
        val students: List<Student>,
        val searchQuery: String
    ) : StudentsUiState
    data class Error(val message: String, val isNetworkError: Boolean) : StudentsUiState
}

class StudentsViewModel(
    private val studentRepository: StudentRepository = StudentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<StudentsUiState>(StudentsUiState.Loading)
    val uiState: StateFlow<StudentsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadStudents()
    }

    fun loadStudents(query: String = _searchQuery.value) {
        viewModelScope.launch {
            _uiState.value = StudentsUiState.Loading
            val cls = AppContextState.selectedClass.value
            if (cls == null) {
                _uiState.value = StudentsUiState.Success(null, emptyList(), query)
                return@launch
            }

            val q = query.trim().ifEmpty { null }
            when (val res = studentRepository.getStudents(cls.id, q)) {
                is NetworkResult.Success -> {
                    _uiState.value = StudentsUiState.Success(cls, res.data, query)
                }
                is NetworkResult.Error -> {
                    _uiState.value = StudentsUiState.Error(res.message, res.isNetworkError)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // debounce
            loadStudents(query)
        }
    }

    fun createStudent(studentIn: StudentIn, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            when (val res = studentRepository.createStudent(studentIn)) {
                is NetworkResult.Success -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Student '${res.data.name}' added."
                    loadStudents()
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

    fun updateStudent(studentId: Int, studentIn: StudentIn, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            when (val res = studentRepository.updateStudent(studentId, studentIn)) {
                is NetworkResult.Success -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Student updated."
                    loadStudents()
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

    fun deleteStudent(studentId: Int) {
        viewModelScope.launch {
            _isSubmitting.value = true
            when (val res = studentRepository.deleteStudent(studentId)) {
                is NetworkResult.Success -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Student deleted."
                    loadStudents()
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
