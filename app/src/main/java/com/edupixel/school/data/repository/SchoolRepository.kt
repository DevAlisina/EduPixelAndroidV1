package com.edupixel.school.data.repository

import com.edupixel.school.core.network.NetworkResult
import com.edupixel.school.core.network.RetrofitClient
import com.edupixel.school.data.remote.EduPixelApiService
import com.edupixel.school.data.remote.models.School
import com.edupixel.school.data.remote.models.SchoolIn

class SchoolRepository(
    private val api: EduPixelApiService = RetrofitClient.apiService
) : BaseRepository() {

    suspend fun getSchools(): NetworkResult<List<School>> = safeApiCall {
        api.getSchools()
    }

    suspend fun getSchool(schoolId: Int): NetworkResult<School> = safeApiCall {
        api.getSchool(schoolId)
    }

    suspend fun createSchool(schoolIn: SchoolIn): NetworkResult<School> = safeApiCall {
        api.createSchool(schoolIn)
    }

    suspend fun updateSchool(schoolId: Int, schoolIn: SchoolIn): NetworkResult<School> = safeApiCall {
        api.updateSchool(schoolId, schoolIn)
    }

    suspend fun deleteSchool(schoolId: Int): NetworkResult<Unit> = safeApiCall {
        api.deleteSchool(schoolId)
        Unit
    }
}
