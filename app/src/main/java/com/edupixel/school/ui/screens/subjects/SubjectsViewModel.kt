package com.edupixel.school.ui.screens.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edupixel.school.core.network.NetworkResult
import com.edupixel.school.core.state.AppContextState
import com.edupixel.school.data.remote.models.SchoolClass
import com.edupixel.school.data.remote.models.Subject
import com.edupixel.school.data.remote.models.SubjectIn
import com.edupixel.school.data.repository.SubjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SubjectsUiState {
    data object Loading : SubjectsUiState
    data class Success(
        val schoolClass: SchoolClass?,
        val subjects: List<Subject>
    ) : SubjectsUiState
    data class Error(val message: String, val isNetworkError: Boolean) : SubjectsUiState
}

class SubjectsViewModel(
    private val subjectRepository: SubjectRepository = SubjectRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<SubjectsUiState>(SubjectsUiState.Loading)
    val uiState: StateFlow<SubjectsUiState> = _uiState.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage.asStateFlow()

    init {
        loadSubjects()
    }

    fun loadSubjects() {
        viewModelScope.launch {
            _uiState.value = SubjectsUiState.Loading
            val cls = AppContextState.selectedClass.value
            if (cls == null) {
                _uiState.value = SubjectsUiState.Success(null, emptyList())
                return@launch
            }

            when (val res = subjectRepository.getSubjects(cls.id, activeOnly = false)) {
                is NetworkResult.Success -> {
                    _uiState.value = SubjectsUiState.Success(cls, res.data)
                }
                is NetworkResult.Error -> {
                    _uiState.value = SubjectsUiState.Error(res.message, res.isNetworkError)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun createSubject(subjectIn: SubjectIn, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            when (val res = subjectRepository.createSubject(subjectIn)) {
                is NetworkResult.Success -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Subject '${res.data.name}' added."
                    loadSubjects()
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

    fun updateSubject(subjectId: Int, subjectIn: SubjectIn, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            when (val res = subjectRepository.updateSubject(subjectId, subjectIn)) {
                is NetworkResult.Success -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Subject updated."
                    loadSubjects()
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

    fun deleteSubject(subjectId: Int) {
        viewModelScope.launch {
            _isSubmitting.value = true
            when (val res = subjectRepository.deleteSubject(subjectId)) {
                is NetworkResult.Success -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Subject deleted."
                    loadSubjects()
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
