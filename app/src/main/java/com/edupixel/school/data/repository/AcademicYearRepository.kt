package com.edupixel.school.data.repository

import com.edupixel.school.core.network.NetworkResult
import com.edupixel.school.core.network.RetrofitClient
import com.edupixel.school.data.remote.EduPixelApiService
import com.edupixel.school.data.remote.models.AcademicYear
import com.edupixel.school.data.remote.models.AcademicYearIn

class AcademicYearRepository(
    private val api: EduPixelApiService = RetrofitClient.apiService
) : BaseRepository() {

    suspend fun getAcademicYears(schoolId: Int): NetworkResult<List<AcademicYear>> = safeApiCall {
        api.getAcademicYears(schoolId)
    }

    suspend fun getAcademicYear(yearId: Int): NetworkResult<AcademicYear> = safeApiCall {
        api.getAcademicYear(yearId)
    }

    suspend fun createAcademicYear(yearIn: AcademicYearIn): NetworkResult<AcademicYear> = safeApiCall {
        api.createAcademicYear(yearIn)
    }

    suspend fun updateAcademicYear(yearId: Int, yearIn: AcademicYearIn): NetworkResult<AcademicYear> = safeApiCall {
        api.updateAcademicYear(yearId, yearIn)
    }

    suspend fun deleteAcademicYear(yearId: Int): NetworkResult<Unit> = safeApiCall {
        api.deleteAcademicYear(yearId)
        Unit
    }
}
