package com.edupixel.school.data.repository

import com.edupixel.school.core.network.NetworkResult
import com.edupixel.school.core.network.RetrofitClient
import com.edupixel.school.data.remote.EduPixelApiService
import com.edupixel.school.data.remote.models.Approval
import com.edupixel.school.data.remote.models.ApprovalIn

class ApprovalRepository(
    private val api: EduPixelApiService = RetrofitClient.apiService
) : BaseRepository() {

    suspend fun getApprovals(classId: Int): NetworkResult<List<Approval>> = safeApiCall {
        api.getApprovals(classId)
    }

    suspend fun createApproval(approvalIn: ApprovalIn): NetworkResult<Approval> = safeApiCall {
        api.createApproval(approvalIn)
    }

    suspend fun updateApproval(approvalId: Int, approvalIn: ApprovalIn): NetworkResult<Approval> = safeApiCall {
        api.updateApproval(approvalId, approvalIn)
    }

    suspend fun deleteApproval(approvalId: Int): NetworkResult<Unit> = safeApiCall {
        api.deleteApproval(approvalId)
        Unit
    }
}
