package com.edupixel.school.ui.screens.books

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edupixel.school.core.network.NetworkResult
import com.edupixel.school.core.state.AppContextState
import com.edupixel.school.data.remote.models.*
import com.edupixel.school.data.repository.BookRepository
import com.edupixel.school.data.repository.StudentRepository
import com.edupixel.school.data.repository.SubjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface BooksUiState {
    data object Loading : BooksUiState
    data class Success(
        val schoolClass: SchoolClass?,
        val distributions: List<BookDistribution>,
        val students: List<Student>,
        val subjects: List<Subject>
    ) : BooksUiState
    data class Error(val message: String, val isNetworkError: Boolean) : BooksUiState
}

class BooksViewModel(
    private val bookRepository: BookRepository = BookRepository(),
    private val studentRepository: StudentRepository = StudentRepository(),
    private val subjectRepository: SubjectRepository = SubjectRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<BooksUiState>(BooksUiState.Loading)
    val uiState: StateFlow<BooksUiState> = _uiState.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage.asStateFlow()

    init {
        loadBooks()
    }

    fun loadBooks() {
        viewModelScope.launch {
            _uiState.value = BooksUiState.Loading
            val cls = AppContextState.selectedClass.value
            if (cls == null) {
                _uiState.value = BooksUiState.Success(null, emptyList(), emptyList(), emptyList())
                return@launch
            }

            val distRes = bookRepository.getBookDistributions(cls.id)
            val stRes = studentRepository.getStudents(cls.id)
            val subRes = subjectRepository.getSubjects(cls.id, activeOnly = true)

            if (distRes is NetworkResult.Error) {
                _uiState.value = BooksUiState.Error(distRes.message, distRes.isNetworkError)
                return@launch
            }

            val distributions = (distRes as NetworkResult.Success).data
            val students = stRes.getOrNull() ?: emptyList()
            val subjects = subRes.getOrNull() ?: emptyList()

            _uiState.value = BooksUiState.Success(cls, distributions, students, subjects)
        }
    }

    fun createDistribution(distributionIn: DistributionIn, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            when (val res = bookRepository.createBookDistribution(distributionIn)) {
                is NetworkResult.Success -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Textbook distribution recorded."
                    loadBooks()
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

    fun updateDistribution(
        distributionId: Int,
        distributionIn: DistributionIn,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isSubmitting.value = true
            when (val res = bookRepository.updateBookDistribution(distributionId, distributionIn)) {
                is NetworkResult.Success -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Distribution record updated."
                    loadBooks()
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

    fun deleteDistribution(distributionId: Int) {
        viewModelScope.launch {
            _isSubmitting.value = true
            when (val res = bookRepository.deleteBookDistribution(distributionId)) {
                is NetworkResult.Success -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Distribution deleted."
                    loadBooks()
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
