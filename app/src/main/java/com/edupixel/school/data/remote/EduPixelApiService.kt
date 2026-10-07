package com.edupixel.school.data.remote

import com.edupixel.school.data.remote.models.*
import kotlinx.serialization.json.JsonObject
import retrofit2.Response
import retrofit2.http.*

interface EduPixelApiService {

    // ==================== Health ====================
    @GET("health")
    suspend fun getHealth(): HealthResponse

    // ==================== Schools ====================
    @POST("schools")
    suspend fun createSchool(@Body body: SchoolIn): School

    @GET("schools")
    suspend fun getSchools(): List<School>

    @GET("schools/{school_id}")
    suspend fun getSchool(@Path("school_id") schoolId: Int): School

    @PUT("schools/{school_id}")
    suspend fun updateSchool(
        @Path("school_id") schoolId: Int,
        @Body body: SchoolIn
    ): School

    @DELETE("schools/{school_id}")
    suspend fun deleteSchool(@Path("school_id") schoolId: Int): Response<Unit>

    // ==================== Academic Years ====================
    @POST("academic-years")
    suspend fun createAcademicYear(@Body body: AcademicYearIn): AcademicYear

    @GET("schools/{school_id}/academic-years")
    suspend fun getAcademicYears(@Path("school_id") schoolId: Int): List<AcademicYear>

    @GET("academic-years/{year_id}")
    suspend fun getAcademicYear(@Path("year_id") yearId: Int): AcademicYear

    @PUT("academic-years/{year_id}")
    suspend fun updateAcademicYear(
        @Path("year_id") yearId: Int,
        @Body body: AcademicYearIn
    ): AcademicYear

    @DELETE("academic-years/{year_id}")
    suspend fun deleteAcademicYear(@Path("year_id") yearId: Int): Response<Unit>

    // ==================== Classes ====================
    @POST("classes")
    suspend fun createClass(@Body body: ClassIn): SchoolClass

    @GET("schools/{school_id}/classes")
    suspend fun getClasses(
        @Path("school_id") schoolId: Int,
        @Query("year_id") yearId: Int? = null
    ): List<SchoolClass>

    @GET("classes/{class_id}")
    suspend fun getClass(@Path("class_id") classId: Int): SchoolClass

    @PUT("classes/{class_id}")
    suspend fun updateClass(
        @Path("class_id") classId: Int,
        @Body body: ClassIn
    ): SchoolClass

    @DELETE("classes/{class_id}")
    suspend fun deleteClass(@Path("class_id") classId: Int): Response<Unit>

    @GET("classes/{class_id}/policy")
    suspend fun getClassPolicy(@Path("class_id") classId: Int): JsonObject

    @PUT("classes/{class_id}/policy")
    suspend fun updateClassPolicy(
        @Path("class_id") classId: Int,
        @Body body: JsonObject
    ): JsonObject

    // ==================== Subjects ====================
    @POST("subjects")
    suspend fun createSubject(@Body body: SubjectIn): Subject

    @GET("classes/{class_id}/subjects")
    suspend fun getSubjects(
        @Path("class_id") classId: Int,
        @Query("active_only") activeOnly: Boolean? = true
    ): List<Subject>

    @GET("subjects/{subject_id}")
    suspend fun getSubject(@Path("subject_id") subjectId: Int): Subject

    @PUT("subjects/{subject_id}")
    suspend fun updateSubject(
        @Path("subject_id") subjectId: Int,
        @Body body: SubjectIn
    ): Subject

    @DELETE("subjects/{subject_id}")
    suspend fun deleteSubject(@Path("subject_id") subjectId: Int): Response<Unit>

    // ==================== Students ====================
    @POST("students")
    suspend fun createStudent(@Body body: StudentIn): Student

    @GET("classes/{class_id}/students")
    suspend fun getStudents(
        @Path("class_id") classId: Int,
        @Query("q") query: String? = null
    ): List<Student>

    @GET("students/{student_id}")
    suspend fun getStudent(@Path("student_id") studentId: Int): Student

    @PUT("students/{student_id}")
    suspend fun updateStudent(
        @Path("student_id") studentId: Int,
        @Body body: StudentIn
    ): Student

    @DELETE("students/{student_id}")
    suspend fun deleteStudent(@Path("student_id") studentId: Int): Response<Unit>

    // ==================== Scores ====================
    @POST("scores")
    suspend fun upsertScore(@Body body: ScoreIn): Score

    @POST("classes/{class_id}/scores/bulk")
    suspend fun bulkUpsertScores(
        @Path("class_id") classId: Int,
        @Body body: ScoreBulkIn
    ): List<Score>

    @GET("students/{student_id}/scores")
    suspend fun getStudentScores(@Path("student_id") studentId: Int): List<Score>

    @GET("scores/{score_id}")
    suspend fun getScore(@Path("score_id") scoreId: Int): Score

    @PUT("scores/{score_id}")
    suspend fun updateScore(
        @Path("score_id") scoreId: Int,
        @Body body: ScoreIn
    ): Score

    @DELETE("scores/{score_id}")
    suspend fun deleteScore(@Path("score_id") scoreId: Int): Response<Unit>

    @GET("scores/{score_id}/components")
    suspend fun getScoreComponents(@Path("score_id") scoreId: Int): JsonObject

    @GET("students/{student_id}/shaqah")
    suspend fun getStudentShaqah(@Path("student_id") studentId: Int): ShaqahResponse

    // ==================== Attendance ====================
    @POST("attendance")
    suspend fun upsertAttendance(
        @Query("student_id") studentId: Int,
        @Body body: AttendanceIn
    ): Attendance

    @POST("classes/{class_id}/attendance/bulk")
    suspend fun bulkUpsertAttendance(
        @Path("class_id") classId: Int,
        @Body body: AttendanceBulkIn
    ): List<Attendance>

    @GET("students/{student_id}/attendance")
    suspend fun getStudentAttendance(@Path("student_id") studentId: Int): List<Attendance>

    @PUT("attendance/{attendance_id}")
    suspend fun updateAttendance(
        @Path("attendance_id") attendanceId: Int,
        @Body body: AttendanceIn
    ): Attendance

    @DELETE("attendance/{attendance_id}")
    suspend fun deleteAttendance(@Path("attendance_id") attendanceId: Int): Response<Unit>

    // ==================== Approvals ====================
    @POST("approvals")
    suspend fun createApproval(@Body body: ApprovalIn): Approval

    @GET("classes/{class_id}/approvals")
    suspend fun getApprovals(@Path("class_id") classId: Int): List<Approval>

    @PUT("approvals/{approval_id}")
    suspend fun updateApproval(
        @Path("approval_id") approvalId: Int,
        @Body body: ApprovalIn
    ): Approval

    @DELETE("approvals/{approval_id}")
    suspend fun deleteApproval(@Path("approval_id") approvalId: Int): Response<Unit>

    // ==================== Book Distribution ====================
    @POST("book-distributions")
    suspend fun createBookDistribution(@Body body: DistributionIn): BookDistribution

    @GET("classes/{class_id}/book-distributions")
    suspend fun getBookDistributions(@Path("class_id") classId: Int): List<BookDistribution>

    @PUT("book-distributions/{distribution_id}")
    suspend fun updateBookDistribution(
        @Path("distribution_id") distributionId: Int,
        @Body body: DistributionIn
    ): BookDistribution

    @DELETE("book-distributions/{distribution_id}")
    suspend fun deleteBookDistribution(@Path("distribution_id") distributionId: Int): Response<Unit>

    // ==================== Results & Reports ====================
    @GET("students/{student_id}/result")
    suspend fun getStudentResult(@Path("student_id") studentId: Int): StudentResult

    @GET("classes/{class_id}/results")
    suspend fun getClassResults(@Path("class_id") classId: Int): ClassResultsResponse

    @GET("classes/{class_id}/compact-register")
    suspend fun getCompactRegister(@Path("class_id") classId: Int): List<CompactRegisterRow>

    @GET("classes/{class_id}/subject-register")
    suspend fun getSubjectRegister(@Path("class_id") classId: Int): SubjectRegisterResponse

    @GET("classes/{class_id}/notice-cards")
    suspend fun getNoticeCards(@Path("class_id") classId: Int): List<NoticeCard>

    @GET("students/{student_id}/report-card")
    suspend fun getStudentReportCard(@Path("student_id") studentId: Int): ReportCardResponse

    @GET("classes/{class_id}/passing-list")
    suspend fun getPassingList(@Path("class_id") classId: Int): List<StudentResult>

    @GET("classes/{class_id}/conditional-list")
    suspend fun getConditionalList(@Path("class_id") classId: Int): List<StudentResult>

    @GET("classes/{class_id}/failed-or-banned-list")
    suspend fun getFailedOrBannedList(@Path("class_id") classId: Int): List<StudentResult>

    @GET("classes/{class_id}/recent-summary")
    suspend fun getRecentSummary(@Path("class_id") classId: Int): RecentSummaryResponse

    @GET("classes/{class_id}/approval-summary")
    suspend fun getApprovalSummary(@Path("class_id") classId: Int): ApprovalSummaryResponse

    @GET("classes/{class_id}/cover")
    suspend fun getClassCover(@Path("class_id") classId: Int): ClassCoverResponse

    @GET("classes/{class_id}/book-distribution-register")
    suspend fun getBookDistributionRegister(@Path("class_id") classId: Int): BookDistributionRegisterResponse

    @GET("classes/{class_id}/dashboard")
    suspend fun getClassDashboard(@Path("class_id") classId: Int): ClassDashboardResponse
}
