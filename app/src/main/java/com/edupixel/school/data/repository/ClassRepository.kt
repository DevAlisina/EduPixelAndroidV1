package com.edupixel.school.data.repository

import com.edupixel.school.core.network.NetworkResult
import com.edupixel.school.core.network.RetrofitClient
import com.edupixel.school.data.remote.EduPixelApiService
import com.edupixel.school.data.remote.models.ClassIn
import com.edupixel.school.data.remote.models.SchoolClass
import kotlinx.serialization.json.JsonObject

class ClassRepository(
    private val api: EduPixelApiService = RetrofitClient.apiService
) : BaseRepository() {

    suspend fun getClasses(schoolId: Int, yearId: Int? = null): NetworkResult<List<SchoolClass>> = safeApiCall {
        api.getClasses(schoolId, yearId)
    }

    suspend fun getClass(classId: Int): NetworkResult<SchoolClass> = safeApiCall {
        api.getClass(classId)
    }

    suspend fun createClass(classIn: ClassIn): NetworkResult<SchoolClass> = safeApiCall {
        api.createClass(classIn)
    }

    suspend fun updateClass(classId: Int, classIn: ClassIn): NetworkResult<SchoolClass> = safeApiCall {
        api.updateClass(classId, classIn)
    }

    suspend fun deleteClass(classId: Int): NetworkResult<Unit> = safeApiCall {
        api.deleteClass(classId)
        Unit
    }

    suspend fun getClassPolicy(classId: Int): NetworkResult<JsonObject> = safeApiCall {
        api.getClassPolicy(classId)
    }

    suspend fun updateClassPolicy(classId: Int, policy: JsonObject): NetworkResult<JsonObject> = safeApiCall {
        api.updateClassPolicy(classId, policy)
    }
}
