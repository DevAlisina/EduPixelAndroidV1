package com.edupixel.school

import com.edupixel.school.core.network.RetrofitClient
import com.edupixel.school.data.remote.models.*
import kotlinx.serialization.decodeFromString
import org.junit.Assert.*
import org.junit.Test

class ApiModelsSerializationTest {

    private val json = RetrofitClient.json

    @Test
    fun testHealthResponseSerialization() {
        val payload = """
            {
              "status": "ok",
              "service": "edupixel-school",
              "excel_runtime_dependency": "none"
            }
        """.trimIndent()

        val response = json.decodeFromString<HealthResponse>(payload)
        assertEquals("ok", response.status)
        assertEquals("edupixel-school", response.service)
        assertEquals("none", response.excelRuntimeDependency)
    }

    @Test
    fun testSchoolSerialization() {
        val payload = """
            {
              "id": 1,
              "name": "Herat Experimental School",
              "province": "Herat",
              "district": "District 1",
              "manager_name": "Ahmad",
              "principal_name": "Karim",
              "academic_year_solar": 1404,
              "academic_year_qamari": 1447
            }
        """.trimIndent()

        val school = json.decodeFromString<School>(payload)
        assertEquals(1, school.id)
        assertEquals("Herat Experimental School", school.name)
        assertEquals("Herat", school.province)
        assertEquals(1404, school.academicYearSolar)
    }

    @Test
    fun testClassResultsResponseSerialization() {
        val payload = """
            {
              "class_id": 1,
              "students": [
                {
                  "student_id": 101,
                  "student_name": "Ali Sina",
                  "attendance_no": 1,
                  "mid_result": "موفق",
                  "mid_grade": "ب",
                  "annual_result": "ارتقا صنف",
                  "annual_grade": "ب",
                  "mid_sum": 285.0,
                  "mid_average": 28.5,
                  "final_sum": 745.0,
                  "final_average": 74.5,
                  "below_40_count": 0,
                  "subject_count": 10,
                  "entered_mid_subject_count": 10,
                  "entered_annual_subject_count": 10,
                  "subject_results": [
                    {
                      "subject_id": 1,
                      "subject_name": "Mathematics",
                      "order_index": 1,
                      "mid": 32.0,
                      "annual": 58.0,
                      "final": 90.0
                    }
                  ],
                  "attendance": {
                    "mid": {
                      "school_days": 81,
                      "present": 78,
                      "absent": 1,
                      "sick": 1,
                      "leave": 1
                    }
                  },
                  "message": "Student promoted"
                }
              ],
              "summary": {
                "total_enrolled": 30,
                "mid_exam_included": 30,
                "annual_exam_included": 30,
                "mid": { "موفق": 24, "تلاش بیشتر": 6 },
                "annual": { "ارتقا صنف": 25, "تکرار صنف": 5 }
              }
            }
        """.trimIndent()

        val response = json.decodeFromString<ClassResultsResponse>(payload)
        assertEquals(1, response.classId)
        assertEquals(1, response.students.size)
        val st = response.students.first()
        assertEquals("Ali Sina", st.studentName)
        assertEquals("موفق", st.midResult)
        assertEquals("ارتقا صنف", st.annualResult)
        assertEquals(28.5, st.midAverage!!, 0.01)
        assertEquals(74.5, st.finalAverage!!, 0.01)
        assertEquals(1, st.subjectResults.size)
        assertEquals("Mathematics", st.subjectResults.first().subjectName)
        assertEquals(90.0, st.subjectResults.first().final!!, 0.01)
    }

    @Test
    fun testClassDashboardSerialization() {
        val payload = """
            {
              "class_id": 1,
              "students": 30,
              "subjects": 10,
              "summary": {
                "total_enrolled": 30,
                "mid_exam_included": 30,
                "annual_exam_included": 30,
                "mid": { "موفق": 20 },
                "annual": { "ارتقا صنف": 20 }
              },
              "books": {
                "rows": 300,
                "delivered": 280,
                "signed": 250,
                "quantity": 300
              },
              "approvals": {
                "total": 3,
                "approved": 2
              }
            }
        """.trimIndent()

        val dash = json.decodeFromString<ClassDashboardResponse>(payload)
        assertEquals(1, dash.classId)
        assertEquals(30, dash.students)
        assertEquals(10, dash.subjects)
        assertEquals(280, dash.books?.delivered)
        assertEquals(2, dash.approvals?.approved)
    }
}
