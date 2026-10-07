package com.edupixel.school.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edupixel.school.core.network.NetworkResult
import com.edupixel.school.core.state.AppContextState
import com.edupixel.school.data.remote.models.*
import com.edupixel.school.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ReportData {
    data class Compact(val rows: List<CompactRegisterRow>) : ReportData
    data class SubjectRegister(val data: SubjectRegisterResponse) : ReportData
    data class Notice(val cards: List<NoticeCard>) : ReportData
    data class StudentList(val title: String, val list: List<StudentResult>) : ReportData
    data class Cover(val cover: ClassCoverResponse) : ReportData
    data class Approvals(val summary: ApprovalSummaryResponse) : ReportData
}

sealed interface ReportsUiState {
    data object Loading : ReportsUiState
    data class Success(
        val schoolClass: SchoolClass?,
        val selectedReportIndex: Int,
        val reportData: ReportData?
    ) : ReportsUiState
    data class Error(val message: String, val isNetworkError: Boolean) : ReportsUiState
}

class ReportsViewModel(
    private val reportRepository: ReportRepository = ReportRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReportsUiState>(ReportsUiState.Loading)
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    private val _selectedReportIndex = MutableStateFlow(0)
    val selectedReportIndex: StateFlow<Int> = _selectedReportIndex.asStateFlow()

    init {
        loadCurrentReport(0)
    }

    fun selectReport(index: Int) {
        _selectedReportIndex.value = index
        loadCurrentReport(index)
    }

    fun loadCurrentReport(index: Int = _selectedReportIndex.value) {
        viewModelScope.launch {
            _uiState.value = ReportsUiState.Loading
            val cls = AppContextState.selectedClass.value
            if (cls == null) {
                _uiState.value = ReportsUiState.Success(null, index, null)
                return@launch
            }

            val data: ReportData? = when (index) {
                0 -> { // Compact Register
                    when (val res = reportRepository.getCompactRegister(cls.id)) {
                        is NetworkResult.Success -> ReportData.Compact(res.data)
                        is NetworkResult.Error -> {
                            _uiState.value = ReportsUiState.Error(res.message, res.isNetworkError)
                            return@launch
                        }
                        is NetworkResult.Loading -> null
                    }
                }
                1 -> { // Subject Register
                    when (val res = reportRepository.getSubjectRegister(cls.id)) {
                        is NetworkResult.Success -> ReportData.SubjectRegister(res.data)
                        is NetworkResult.Error -> {
                            _uiState.value = ReportsUiState.Error(res.message, res.isNetworkError)
                            return@launch
                        }
                        is NetworkResult.Loading -> null
                    }
                }
                2 -> { // Notice Cards
                    when (val res = reportRepository.getNoticeCards(cls.id)) {
                        is NetworkResult.Success -> ReportData.Notice(res.data)
                        is NetworkResult.Error -> {
                            _uiState.value = ReportsUiState.Error(res.message, res.isNetworkError)
                            return@launch
                        }
                        is NetworkResult.Loading -> null
                    }
                }
                3 -> { // Passing List
                    when (val res = reportRepository.getPassingList(cls.id)) {
                        is NetworkResult.Success -> ReportData.StudentList("Passing / Promoted Students (ارتقا صنف)", res.data)
                        is NetworkResult.Error -> {
                            _uiState.value = ReportsUiState.Error(res.message, res.isNetworkError)
                            return@launch
                        }
                        is NetworkResult.Loading -> null
                    }
                }
                4 -> { // Conditional List
                    when (val res = reportRepository.getConditionalList(cls.id)) {
                        is NetworkResult.Success -> ReportData.StudentList("Conditional Students (مشروط / معذرتی)", res.data)
                        is NetworkResult.Error -> {
                            _uiState.value = ReportsUiState.Error(res.message, res.isNetworkError)
                            return@launch
                        }
                        is NetworkResult.Loading -> null
                    }
                }
                5 -> { // Failed / Banned List
                    when (val res = reportRepository.getFailedOrBannedList(cls.id)) {
                        is NetworkResult.Success -> ReportData.StudentList("Failed & Banned Students (تکرار صنف / محروم)", res.data)
                        is NetworkResult.Error -> {
                            _uiState.value = ReportsUiState.Error(res.message, res.isNetworkError)
                            return@launch
                        }
                        is NetworkResult.Loading -> null
                    }
                }
                6 -> { // Cover Sheet
                    when (val res = reportRepository.getClassCover(cls.id)) {
                        is NetworkResult.Success -> ReportData.Cover(res.data)
                        is NetworkResult.Error -> {
                            _uiState.value = ReportsUiState.Error(res.message, res.isNetworkError)
                            return@launch
                        }
                        is NetworkResult.Loading -> null
                    }
                }
                7 -> { // Approval Summary
                    when (val res = reportRepository.getApprovalSummary(cls.id)) {
                        is NetworkResult.Success -> ReportData.Approvals(res.data)
                        is NetworkResult.Error -> {
                            _uiState.value = ReportsUiState.Error(res.message, res.isNetworkError)
                            return@launch
                        }
                        is NetworkResult.Loading -> null
                    }
                }
                else -> null
            }

            _uiState.value = ReportsUiState.Success(cls, index, data)
        }
    }
}
