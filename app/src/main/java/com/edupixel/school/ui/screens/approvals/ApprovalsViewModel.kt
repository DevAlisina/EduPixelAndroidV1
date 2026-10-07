package com.edupixel.school.ui.screens.approvals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edupixel.school.core.network.NetworkResult
import com.edupixel.school.core.state.AppContextState
import com.edupixel.school.data.remote.models.Approval
import com.edupixel.school.data.remote.models.ApprovalIn
import com.edupixel.school.data.remote.models.SchoolClass
import com.edupixel.school.data.repository.ApprovalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ApprovalsUiState {
    data object Loading : ApprovalsUiState
    data class Success(
        val schoolClass: SchoolClass?,
        val approvals: List<Approval>
    ) : ApprovalsUiState
    data class Error(val message: String, val isNetworkError: Boolean) : ApprovalsUiState
}

class ApprovalsViewModel(
    private val approvalRepository: ApprovalRepository = ApprovalRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ApprovalsUiState>(ApprovalsUiState.Loading)
    val uiState: StateFlow<ApprovalsUiState> = _uiState.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage.asStateFlow()

    init {
        loadApprovals()
    }

    fun loadApprovals() {
        viewModelScope.launch {
            _uiState.value = ApprovalsUiState.Loading
            val cls = AppContextState.selectedClass.value
            if (cls == null) {
                _uiState.value = ApprovalsUiState.Success(null, emptyList())
                return@launch
            }

            when (val res = approvalRepository.getApprovals(cls.id)) {
                is NetworkResult.Success -> {
                    _uiState.value = ApprovalsUiState.Success(cls, res.data)
                }
                is NetworkResult.Error -> {
                    _uiState.value = ApprovalsUiState.Error(res.message, res.isNetworkError)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun createApproval(approvalIn: ApprovalIn, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            when (val res = approvalRepository.createApproval(approvalIn)) {
                is NetworkResult.Success -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Approval entry recorded."
                    loadApprovals()
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

    fun updateApproval(approvalId: Int, approvalIn: ApprovalIn, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            when (val res = approvalRepository.updateApproval(approvalId, approvalIn)) {
                is NetworkResult.Success -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Approval record updated."
                    loadApprovals()
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

    fun toggleApprovalStatus(approval: Approval) {
        viewModelScope.launch {
            val updated = ApprovalIn(
                classId = approval.classId,
                role = approval.role,
                personName = approval.personName,
                note = approval.note,
                approved = !approval.approved,
                classType = approval.classType
            )
            approvalRepository.updateApproval(approval.id, updated)
            loadApprovals()
        }
    }

    fun deleteApproval(approvalId: Int) {
        viewModelScope.launch {
            _isSubmitting.value = true
            when (val res = approvalRepository.deleteApproval(approvalId)) {
                is NetworkResult.Success -> {
                    _isSubmitting.value = false
                    _operationMessage.value = "Approval record removed."
                    loadApprovals()
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
