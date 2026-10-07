package com.edupixel.school.ui.screens.classes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edupixel.school.core.network.NetworkResult
import com.edupixel.school.core.state.AppContextState
import com.edupixel.school.data.remote.models.ClassIn
import com.edupixel.school.data.remote.models.School
import com.edupixel.school.data.remote.models.SchoolClass
import com.edupixel.school.data.repository.ClassRepository
import com.edupixel.school.data.repository.SchoolRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

sealed interface ClassesUiState {
    data object Loading : ClassesUiState
    data class Success(
        val school: School?,
        val classes: List<SchoolClass>,
        val selectedClassId: Int?
    ) : ClassesUiState
    data class Error(val message: String, val isNetworkError: Boolean) : ClassesUiState
}

class ClassesViewModel(
    private val classRepository: ClassRepository = ClassRepository(),
    private val schoolRepository: SchoolRepository = SchoolRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ClassesUiState>(ClassesUiState.Loading)
    val uiState: StateFlow<ClassesUiState> = _uiState.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage.asStateFlow()

    init {
        loadClasses()
    }

    fun loadClasses() {
        viewModelScope.launch {
            _uiState.value = ClassesUiState.Loading
            var school = AppContextState.selectedSchool.value
            if (school == null) {
                val schoolsRes = schoolRepository.getSchools()
                if (schoolsRes is NetworkResult.Success && schoolsRes.data.isNotEmpty()) {
                    school = schoolsRes.data.first()
                    AppContextState.setSelectedSchool(school)
                }
            }

            if (school == null) {
                _uiState.value = ClassesUiState.Success(null, emptyList(), null)
                return@launch
            }

            when (val res = classRepository.getClasses(school.id)) {
                is NetworkResult.Success -> {
                    val activeId = AppContextState.selectedClass.value?.id
                    _uiState.value = ClassesUiState.Success(school, res.data, activeId)
                    if (AppContextState.selectedClass.value == null && res.data.isNotEmpty()) {
                        AppContextState.setSelectedClass(res.data.first())
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.value = ClassesUiState.Error(res.message, res.isNetworkError)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun selectClass(cls: SchoolClass) {
        AppContextState.setSelectedClass(cls)
        val current = _uiState.value
        if (current is ClassesUiState.Success) {
            _uiState.value = current.copy(selectedClassId = cls.id)
        }
    }

    fun createClass(classIn: ClassIn, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            when (val res = classRepository.createClass(classIn)) {
                is NetworkResult.Success -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Class '${res.data.name}' created."
                    AppContextState.setSelectedClass(res.data)
                    loadClasses()
                    onSuccess()
                }
                is NetworkResult.Error -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Error creating class: ${res.message}"
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun updateClass(classId: Int, classIn: ClassIn, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            when (val res = classRepository.updateClass(classId, classIn)) {
                is NetworkResult.Success -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Class updated."
                    if (AppContextState.selectedClass.value?.id == classId) {
                        AppContextState.setSelectedClass(res.data)
                    }
                    loadClasses()
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

    fun deleteClass(classId: Int) {
        viewModelScope.launch {
            _isSubmitting.value = true
            when (val res = classRepository.deleteClass(classId)) {
                is NetworkResult.Success -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Class deleted."
                    if (AppContextState.selectedClass.value?.id == classId) {
                        AppContextState.setSelectedClass(null)
                    }
                    loadClasses()
                }
                is NetworkResult.Error -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Error: ${res.message}"
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun updatePolicy(classId: Int, policy: JsonObject, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            when (val res = classRepository.updateClassPolicy(classId, policy)) {
                is NetworkResult.Success -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Class policy updated."
                    loadClasses()
                    onSuccess()
                }
                is NetworkResult.Error -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Error updating policy: ${res.message}"
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun clearMessage() {
        _operationMessage.value = null
    }
}
