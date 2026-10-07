package com.edupixel.school.ui.navigation

sealed class Screen(val route: String, val title: String) {
    data object Dashboard : Screen("dashboard", "Dashboard")
    data object Schools : Screen("schools", "Schools")
    data object AcademicYears : Screen("academic_years", "Academic Years")
    data object Classes : Screen("classes", "Classes")
    data object Subjects : Screen("subjects", "Subjects")
    data object Students : Screen("students", "Students")
    data object StudentDetail : Screen("student_detail/{studentId}", "Student Detail") {
        fun createRoute(studentId: Int) = "student_detail/$studentId"
    }
    data object Scores : Screen("scores", "Scores Entry")
    data object Attendance : Screen("attendance", "Attendance Entry")
    data object MidtermResults : Screen("midterm_results", "Midterm Results")
    data object AnnualResults : Screen("annual_results", "Annual Results")
    data object Reports : Screen("reports", "Reports & Registers")
    data object Books : Screen("books", "Book Distribution")
    data object Approvals : Screen("approvals", "Approvals")
    data object Settings : Screen("settings", "Settings")
}
