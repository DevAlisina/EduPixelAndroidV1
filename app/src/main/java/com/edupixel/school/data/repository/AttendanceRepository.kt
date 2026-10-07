package com.edupixel.school.data.repository

import com.edupixel.school.core.network.NetworkResult
import com.edupixel.school.core.network.RetrofitClient
import com.edupixel.school.data.remote.EduPixelApiService
import com.edupixel.school.data.remote.models.Attendance
import com.edupixel.school.data.remote.models.AttendanceBulkIn
import com.edupixel.school.data.remote.models.AttendanceBulkItem
import com.edupixel.school.data.remote.models.AttendanceIn

class AttendanceRepository(
    private val api: EduPixelApiService = RetrofitClient.apiService
) : BaseRepository() {

    suspend fun upsertAttendance(studentId: Int, attendanceIn: AttendanceIn): NetworkResult<Attendance> = safeApiCall {
        api.upsertAttendance(studentId, attendanceIn)
    }

    suspend fun bulkUpsertAttendance(classId: Int, items: List<AttendanceBulkItem>): NetworkResult<List<Attendance>> = safeApiCall {
        api.bulkUpsertAttendance(classId, AttendanceBulkIn(items))
    }

    suspend fun getStudentAttendance(studentId: Int): NetworkResult<List<Attendance>> = safeApiCall {
        api.getStudentAttendance(studentId)
    }

    suspend fun updateAttendance(attendanceId: Int, attendanceIn: AttendanceIn): NetworkResult<Attendance> = safeApiCall {
        api.updateAttendance(attendanceId, attendanceIn)
    }

    suspend fun deleteAttendance(attendanceId: Int): NetworkResult<Unit> = safeApiCall {
        api.deleteAttendance(attendanceId)
        Unit
    }
}
