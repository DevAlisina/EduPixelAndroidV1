package com.edupixel.school.data.repository

import com.edupixel.school.core.network.NetworkResult
import com.edupixel.school.core.network.RetrofitClient
import com.edupixel.school.data.remote.EduPixelApiService
import com.edupixel.school.data.remote.models.*

class ReportRepository(
    private val api: EduPixelApiService = RetrofitClient.apiService
) : BaseRepository() {

    suspend fun getHealth(): NetworkResult<HealthResponse> = safeApiCall {
        api.getHealth()
    }

    suspend fun getStudentResult(studentId: Int): NetworkResult<StudentResult> = safeApiCall {
        api.getStudentResult(studentId)
    }

    suspend fun getClassResults(classId: Int): NetworkResult<ClassResultsResponse> = safeApiCall {
        api.getClassResults(classId)
    }

    suspend fun getCompactRegister(classId: Int): NetworkResult<List<CompactRegisterRow>> = safeApiCall {
        api.getCompactRegister(classId)
    }

    suspend fun getSubjectRegister(classId: Int): NetworkResult<SubjectRegisterResponse> = safeApiCall {
        api.getSubjectRegister(classId)
    }

    suspend fun getNoticeCards(classId: Int): NetworkResult<List<NoticeCard>> = safeApiCall {
        api.getNoticeCards(classId)
    }

    suspend fun getStudentReportCard(studentId: Int): NetworkResult<ReportCardResponse> = safeApiCall {
        api.getStudentReportCard(studentId)
    }

    suspend fun getPassingList(classId: Int): NetworkResult<List<StudentResult>> = safeApiCall {
        api.getPassingList(classId)
    }

    suspend fun getConditionalList(classId: Int): NetworkResult<List<StudentResult>> = safeApiCall {
        api.getConditionalList(classId)
    }

    suspend fun getFailedOrBannedList(classId: Int): NetworkResult<List<StudentResult>> = safeApiCall {
        api.getFailedOrBannedList(classId)
    }

    suspend fun getRecentSummary(classId: Int): NetworkResult<RecentSummaryResponse> = safeApiCall {
        api.getRecentSummary(classId)
    }

    suspend fun getApprovalSummary(classId: Int): NetworkResult<ApprovalSummaryResponse> = safeApiCall {
        api.getApprovalSummary(classId)
    }

    suspend fun getClassCover(classId: Int): NetworkResult<ClassCoverResponse> = safeApiCall {
        api.getClassCover(classId)
    }

    suspend fun getClassDashboard(classId: Int): NetworkResult<ClassDashboardResponse> = safeApiCall {
        api.getClassDashboard(classId)
    }
}
