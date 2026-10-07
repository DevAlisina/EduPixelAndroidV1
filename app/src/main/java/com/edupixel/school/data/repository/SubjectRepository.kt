package com.edupixel.school.data.repository

import com.edupixel.school.core.network.NetworkResult
import com.edupixel.school.core.network.RetrofitClient
import com.edupixel.school.data.remote.EduPixelApiService
import com.edupixel.school.data.remote.models.Subject
import com.edupixel.school.data.remote.models.SubjectIn

class SubjectRepository(
    private val api: EduPixelApiService = RetrofitClient.apiService
) : BaseRepository() {

    suspend fun getSubjects(classId: Int, activeOnly: Boolean? = true): NetworkResult<List<Subject>> = safeApiCall {
        api.getSubjects(classId, activeOnly)
    }

    suspend fun getSubject(subjectId: Int): NetworkResult<Subject> = safeApiCall {
        api.getSubject(subjectId)
    }

    suspend fun createSubject(subjectIn: SubjectIn): NetworkResult<Subject> = safeApiCall {
        api.createSubject(subjectIn)
    }

    suspend fun updateSubject(subjectId: Int, subjectIn: SubjectIn): NetworkResult<Subject> = safeApiCall {
        api.updateSubject(subjectId, subjectIn)
    }

    suspend fun deleteSubject(subjectId: Int): NetworkResult<Unit> = safeApiCall {
        api.deleteSubject(subjectId)
        Unit
    }
}
