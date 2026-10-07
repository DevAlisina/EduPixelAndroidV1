package com.edupixel.school.data.repository

import com.edupixel.school.core.network.NetworkResult
import com.edupixel.school.core.network.RetrofitClient
import com.edupixel.school.data.remote.EduPixelApiService
import com.edupixel.school.data.remote.models.Student
import com.edupixel.school.data.remote.models.StudentIn

class StudentRepository(
    private val api: EduPixelApiService = RetrofitClient.apiService
) : BaseRepository() {

    suspend fun getStudents(classId: Int, query: String? = null): NetworkResult<List<Student>> = safeApiCall {
        api.getStudents(classId, query)
    }

    suspend fun getStudent(studentId: Int): NetworkResult<Student> = safeApiCall {
        api.getStudent(studentId)
    }

    suspend fun createStudent(studentIn: StudentIn): NetworkResult<Student> = safeApiCall {
        api.createStudent(studentIn)
    }

    suspend fun updateStudent(studentId: Int, studentIn: StudentIn): NetworkResult<Student> = safeApiCall {
        api.updateStudent(studentId, studentIn)
    }

    suspend fun deleteStudent(studentId: Int): NetworkResult<Unit> = safeApiCall {
        api.deleteStudent(studentId)
        Unit
    }
}
