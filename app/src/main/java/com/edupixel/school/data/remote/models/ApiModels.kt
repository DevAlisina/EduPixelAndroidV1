package com.edupixel.school.data.remote.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class HealthResponse(
    val status: String,
    val service: String,
    @SerialName("excel_runtime_dependency")
    val excelRuntimeDependency: String? = null
)

// ==================== School ====================

@Serializable
data class School(
    val id: Int,
    val name: String,
    val province: String? = null,
    val district: String? = null,
    @SerialName("manager_name")
    val managerName: String? = null,
    @SerialName("principal_name")
    val principalName: String? = null,
    @SerialName("academic_year_solar")
    val academicYearSolar: Int? = null,
    @SerialName("academic_year_qamari")
    val academicYearQamari: Int? = null
)

@Serializable
data class SchoolIn(
    val name: String,
    val province: String? = null,
    val district: String? = null,
    @SerialName("manager_name")
    val managerName: String? = null,
    @SerialName("principal_name")
    val principalName: String? = null,
    @SerialName("academic_year_solar")
    val academicYearSolar: Int? = null,
    @SerialName("academic_year_qamari")
    val academicYearQamari: Int? = null
)

// ==================== Academic Year ====================

@Serializable
data class AcademicYear(
    val id: Int,
    @SerialName("school_id")
    val schoolId: Int,
    @SerialName("solar_year")
    val solarYear: Int,
    @SerialName("qamari_year")
    val qamariYear: Int? = null,
    val label: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true
)

@Serializable
data class AcademicYearIn(
    @SerialName("school_id")
    val schoolId: Int,
    @SerialName("solar_year")
    val solarYear: Int,
    @SerialName("qamari_year")
    val qamariYear: Int? = null,
    val label: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true
)

// ==================== School Class ====================

@Serializable
data class SchoolClass(
    val id: Int,
    @SerialName("school_id")
    val schoolId: Int,
    @SerialName("academic_year_id")
    val academicYearId: Int? = null,
    val name: String,
    @SerialName("grade_level")
    val gradeLevel: String? = null,
    val section: String? = null,
    @SerialName("teacher_name")
    val teacherName: String? = null,
    @SerialName("max_absence")
    val maxAbsence: Int = 56,
    val policy: JsonObject = JsonObject(emptyMap()),
    val notes: String? = null
)

@Serializable
data class ClassIn(
    @SerialName("school_id")
    val schoolId: Int,
    @SerialName("academic_year_id")
    val academicYearId: Int? = null,
    val name: String,
    @SerialName("grade_level")
    val gradeLevel: String? = null,
    val section: String? = null,
    @SerialName("teacher_name")
    val teacherName: String? = null,
    @SerialName("max_absence")
    val maxAbsence: Int = 56,
    val policy: JsonObject = JsonObject(emptyMap()),
    val notes: String? = null
)

// ==================== Subject ====================

@Serializable
data class Subject(
    val id: Int,
    @SerialName("class_id")
    val classId: Int,
    val name: String,
    @SerialName("order_index")
    val orderIndex: Int = 0,
    val active: Boolean = true,
    @SerialName("include_in_results")
    val includeInResults: Boolean = true,
    @SerialName("mid_max")
    val midMax: Double = 40.0,
    @SerialName("annual_max")
    val annualMax: Double = 60.0,
    @SerialName("components_schema")
    val componentsSchema: JsonObject = JsonObject(emptyMap())
)

@Serializable
data class SubjectIn(
    @SerialName("class_id")
    val classId: Int,
    val name: String,
    @SerialName("order_index")
    val orderIndex: Int = 0,
    val active: Boolean = true,
    @SerialName("include_in_results")
    val includeInResults: Boolean = true,
    @SerialName("mid_max")
    val midMax: Double = 40.0,
    @SerialName("annual_max")
    val annualMax: Double = 60.0,
    @SerialName("components_schema")
    val componentsSchema: JsonObject = JsonObject(emptyMap())
)

// ==================== Student ====================

@Serializable
data class Student(
    val id: Int,
    @SerialName("class_id")
    val classId: Int,
    @SerialName("attendance_no")
    val attendanceNo: Int? = null,
    val name: String,
    val father: String? = null,
    val grandfather: String? = null,
    @SerialName("base_number")
    val baseNumber: String? = null,
    val tazkira: String? = null,
    @SerialName("mid_status")
    val midStatus: String? = null,
    @SerialName("annual_status")
    val annualStatus: String? = null,
    val notes: String? = null
)

@Serializable
data class StudentIn(
    @SerialName("class_id")
    val classId: Int,
    @SerialName("attendance_no")
    val attendanceNo: Int? = null,
    val name: String,
    val father: String? = null,
    val grandfather: String? = null,
    @SerialName("base_number")
    val baseNumber: String? = null,
    val tazkira: String? = null,
    @SerialName("mid_status")
    val midStatus: String? = null,
    @SerialName("annual_status")
    val annualStatus: String? = null,
    val notes: String? = null
)

// ==================== Score ====================

@Serializable
data class Score(
    val id: Int,
    @SerialName("student_id")
    val studentId: Int,
    @SerialName("subject_id")
    val subjectId: Int,
    val mid: Double? = null,
    val annual: Double? = null,
    @SerialName("mid_components")
    val midComponents: JsonObject = JsonObject(emptyMap()),
    @SerialName("annual_components")
    val annualComponents: JsonObject = JsonObject(emptyMap()),
    val note: String? = null
)

@Serializable
data class ScoreIn(
    @SerialName("student_id")
    val studentId: Int,
    @SerialName("subject_id")
    val subjectId: Int,
    val mid: Double? = null,
    val annual: Double? = null,
    @SerialName("mid_components")
    val midComponents: JsonObject = JsonObject(emptyMap()),
    @SerialName("annual_components")
    val annualComponents: JsonObject = JsonObject(emptyMap()),
    val note: String? = null
)

@Serializable
data class ScoreBulkIn(
    val items: List<ScoreIn>
)

// ==================== Attendance ====================

@Serializable
data class Attendance(
    val id: Int,
    @SerialName("student_id")
    val studentId: Int,
    val period: String, // "midterm" or "annual"
    @SerialName("school_days")
    val schoolDays: Int = 0,
    val present: Int = 0,
    val absent: Int = 0,
    val sick: Int = 0,
    val leave: Int = 0,
    val note: String? = null
)

@Serializable
data class AttendanceIn(
    val period: String, // "midterm" or "annual"
    @SerialName("school_days")
    val schoolDays: Int = 0,
    val present: Int = 0,
    val absent: Int = 0,
    val sick: Int = 0,
    val leave: Int = 0,
    val note: String? = null
)

@Serializable
data class AttendanceBulkItem(
    @SerialName("student_id")
    val studentId: Int,
    val period: String,
    @SerialName("school_days")
    val schoolDays: Int = 0,
    val present: Int = 0,
    val absent: Int = 0,
    val sick: Int = 0,
    val leave: Int = 0,
    val note: String? = null
)

@Serializable
data class AttendanceBulkIn(
    val items: List<AttendanceBulkItem>
)

// ==================== Approval ====================

@Serializable
data class Approval(
    val id: Int,
    @SerialName("class_id")
    val classId: Int,
    val role: String,
    @SerialName("person_name")
    val personName: String? = null,
    val note: String? = null,
    val approved: Boolean = false,
    @SerialName("class_type")
    val classType: String? = null
)

@Serializable
data class ApprovalIn(
    @SerialName("class_id")
    val classId: Int,
    val role: String,
    @SerialName("person_name")
    val personName: String? = null,
    val note: String? = null,
    val approved: Boolean = false,
    @SerialName("class_type")
    val classType: String? = null
)

// ==================== Book Distribution ====================

@Serializable
data class BookDistribution(
    val id: Int,
    @SerialName("class_id")
    val classId: Int,
    @SerialName("student_id")
    val studentId: Int,
    @SerialName("subject_id")
    val subjectId: Int,
    val quantity: Int = 1,
    val delivered: Boolean = false,
    val signed: Boolean = false,
    val notes: String? = null
)

@Serializable
data class DistributionIn(
    @SerialName("class_id")
    val classId: Int,
    @SerialName("student_id")
    val studentId: Int,
    @SerialName("subject_id")
    val subjectId: Int,
    val quantity: Int = 1,
    val delivered: Boolean = false,
    val signed: Boolean = false,
    val notes: String? = null
)

// ==================== Results & Report Models ====================

@Serializable
data class SubjectResult(
    @SerialName("subject_id")
    val subjectId: Int,
    @SerialName("subject_name")
    val subjectName: String,
    @SerialName("order_index")
    val orderIndex: Int = 0,
    val mid: Double? = null,
    val annual: Double? = null,
    val final: Double? = null,
    @SerialName("mid_components")
    val midComponents: JsonObject = JsonObject(emptyMap()),
    @SerialName("annual_components")
    val annualComponents: JsonObject = JsonObject(emptyMap()),
    @SerialName("mid_component_total")
    val midComponentTotal: Double? = null,
    @SerialName("annual_component_total")
    val annualComponentTotal: Double? = null
)

@Serializable
data class AttendanceRecord(
    @SerialName("school_days")
    val schoolDays: Int = 0,
    val present: Int = 0,
    val absent: Int = 0,
    val sick: Int = 0,
    val leave: Int = 0,
    val note: String? = null
)

@Serializable
data class StudentAttendanceSummary(
    val mid: AttendanceRecord? = null,
    val annual: AttendanceRecord? = null
)

@Serializable
data class StudentResult(
    @SerialName("student_id")
    val studentId: Int,
    @SerialName("student_name")
    val studentName: String,
    @SerialName("attendance_no")
    val attendanceNo: Int? = null,
    @SerialName("mid_result")
    val midResult: String? = null,
    @SerialName("mid_grade")
    val midGrade: String? = null,
    @SerialName("annual_result")
    val annualResult: String? = null,
    @SerialName("annual_grade")
    val annualGrade: String? = null,
    @SerialName("mid_sum")
    val midSum: Double? = null,
    @SerialName("mid_average")
    val midAverage: Double? = null,
    @SerialName("final_sum")
    val finalSum: Double? = null,
    @SerialName("final_average")
    val finalAverage: Double? = null,
    @SerialName("below_40_count")
    val below40Count: Int = 0,
    @SerialName("subject_count")
    val subjectCount: Int = 0,
    @SerialName("entered_mid_subject_count")
    val enteredMidSubjectCount: Int = 0,
    @SerialName("entered_annual_subject_count")
    val enteredAnnualSubjectCount: Int = 0,
    @SerialName("subject_results")
    val subjectResults: List<SubjectResult> = emptyList(),
    val attendance: StudentAttendanceSummary? = null,
    val message: String? = null
)

@Serializable
data class ClassResultsSummary(
    @SerialName("total_enrolled")
    val totalEnrolled: Int = 0,
    @SerialName("mid_exam_included")
    val midExamIncluded: Int = 0,
    @SerialName("annual_exam_included")
    val annualExamIncluded: Int = 0,
    val mid: Map<String, Int> = emptyMap(),
    val annual: Map<String, Int> = emptyMap()
)

@Serializable
data class ClassResultsResponse(
    @SerialName("class_id")
    val classId: Int,
    val students: List<StudentResult> = emptyList(),
    val summary: ClassResultsSummary? = null
)

@Serializable
data class CompactRegisterRow(
    @SerialName("attendance_no")
    val attendanceNo: Int? = null,
    @SerialName("student_id")
    val studentId: Int,
    val name: String,
    val father: String? = null,
    val grandfather: String? = null,
    val tazkira: String? = null,
    @SerialName("base_number")
    val baseNumber: String? = null,
    @SerialName("mid_average")
    val midAverage: Double? = null,
    @SerialName("annual_average")
    val annualAverage: Double? = null,
    @SerialName("mid_grade")
    val midGrade: String? = null,
    @SerialName("annual_grade")
    val annualGrade: String? = null,
    @SerialName("annual_result")
    val annualResult: String? = null,
    @SerialName("annual_absent")
    val annualAbsent: Int = 0
)

@Serializable
data class SubjectScoreInfo(
    val name: String,
    val mid: Double? = null,
    val annual: Double? = null,
    val final: Double? = null
)

@Serializable
data class SubjectRegisterRow(
    @SerialName("attendance_no")
    val attendanceNo: Int? = null,
    @SerialName("student_id")
    val studentId: Int,
    @SerialName("student_name")
    val studentName: String,
    val subjects: Map<String, SubjectScoreInfo> = emptyMap(),
    @SerialName("final_sum")
    val finalSum: Double? = null,
    @SerialName("final_average")
    val finalAverage: Double? = null,
    @SerialName("annual_result")
    val annualResult: String? = null,
    @SerialName("annual_grade")
    val annualGrade: String? = null,
    val attendance: StudentAttendanceSummary? = null
)

@Serializable
data class SubjectRegisterResponse(
    @SerialName("class_id")
    val classId: Int,
    val subjects: List<Subject> = emptyList(),
    val rows: List<SubjectRegisterRow> = emptyList()
)

@Serializable
data class NoticeCard(
    val school: School,
    @SerialName("class")
    val schoolClass: SchoolClass,
    val student: Student,
    val result: StudentResult,
    val message: String? = null
)

@Serializable
data class ReportCardResponse(
    val school: School,
    @SerialName("class")
    val schoolClass: SchoolClass,
    val student: Student,
    val result: StudentResult,
    val message: String? = null,
    val approvals: List<Approval> = emptyList()
)

@Serializable
data class BookStats(
    val rows: Int = 0,
    val delivered: Int = 0,
    val signed: Int = 0,
    val quantity: Int = 0
)

@Serializable
data class ApprovalStats(
    val total: Int = 0,
    val approved: Int = 0
)

@Serializable
data class ClassDashboardResponse(
    @SerialName("class_id")
    val classId: Int,
    val students: Int = 0,
    val subjects: Int = 0,
    val summary: ClassResultsSummary? = null,
    val books: BookStats? = null,
    val approvals: ApprovalStats? = null
)

@Serializable
data class RecentSummaryResponse(
    @SerialName("class_id")
    val classId: Int,
    val summary: ClassResultsSummary? = null,
    val approvals: List<Approval> = emptyList()
)

@Serializable
data class ApprovalSummaryResponse(
    @SerialName("class")
    val schoolClass: SchoolClass,
    val approvals: List<Approval> = emptyList(),
    val staff: Map<String, String?> = emptyMap()
)

@Serializable
data class ClassCoverResponse(
    val school: School,
    @SerialName("class")
    val schoolClass: SchoolClass,
    @SerialName("academic_year")
    val academicYear: AcademicYear? = null,
    val teacher: String? = null
)

@Serializable
data class BookSubjectStatus(
    @SerialName("subject_name")
    val subjectName: String,
    val delivered: Boolean = false,
    val signed: Boolean = false,
    val quantity: Int = 0
)

@Serializable
data class BookDistributionRegisterRow(
    @SerialName("student_id")
    val studentId: Int,
    @SerialName("attendance_no")
    val attendanceNo: Int? = null,
    @SerialName("student_name")
    val studentName: String,
    val result: String? = null,
    val subjects: Map<String, BookSubjectStatus> = emptyMap()
)

@Serializable
data class BookDistributionRegisterResponse(
    @SerialName("class_id")
    val classId: Int,
    val subjects: List<Subject> = emptyList(),
    val rows: List<BookDistributionRegisterRow> = emptyList()
)

@Serializable
data class ShaqahItem(
    @SerialName("subject_id")
    val subjectId: Int,
    @SerialName("subject_name")
    val subjectName: String,
    val mid: Double? = null,
    val annual: Double? = null,
    @SerialName("mid_components")
    val midComponents: JsonObject = JsonObject(emptyMap()),
    @SerialName("annual_components")
    val annualComponents: JsonObject = JsonObject(emptyMap()),
    @SerialName("mid_component_total")
    val midComponentTotal: Double? = null,
    @SerialName("annual_component_total")
    val annualComponentTotal: Double? = null
)

@Serializable
data class ShaqahResponse(
    @SerialName("student_id")
    val studentId: Int,
    @SerialName("student_name")
    val studentName: String,
    val items: List<ShaqahItem> = emptyList()
)
