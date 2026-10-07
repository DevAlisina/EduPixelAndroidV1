package com.edupixel.school.ui.screens.studentdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.edupixel.school.data.remote.models.Attendance
import com.edupixel.school.data.remote.models.ReportCardResponse
import com.edupixel.school.data.remote.models.Student
import com.edupixel.school.data.remote.models.StudentResult
import com.edupixel.school.data.remote.models.SubjectResult
import com.edupixel.school.ui.components.*
import com.edupixel.school.ui.presentation.StatusPresentation
import com.edupixel.school.ui.theme.GlassCard
import com.edupixel.school.ui.theme.GlassScreenContainer

@Composable
fun StudentDetailScreen(
    studentId: Int,
    viewModel: StudentDetailViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(studentId) {
        viewModel.loadStudentDetail(studentId)
    }

    GlassScreenContainer(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            GlassTopAppBar(
                title = "Student Details",
                subtitle = "Comprehensive academic profile",
                onBackClick = onBackClick,
                actions = {
                    IconButton(onClick = { viewModel.loadStudentDetail(studentId) }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                    }
                }
            )

            when (val state = uiState) {
                is StudentDetailUiState.Loading -> {
                    LoadingState(message = "Loading student record...")
                }
                is StudentDetailUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        isNetworkError = state.isNetworkError,
                        onRetry = { viewModel.loadStudentDetail(studentId) }
                    )
                }
                is StudentDetailUiState.Success -> {
                    StudentDetailContent(
                        student = state.student,
                        result = state.result,
                        attendanceList = state.attendanceList,
                        reportCard = state.reportCard
                    )
                }
            }
        }
    }
}

@Composable
private fun StudentDetailContent(
    student: Student,
    result: StudentResult?,
    attendanceList: List<Attendance>,
    reportCard: ReportCardResponse?
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Academic Results", "Attendance", "Official Report Card")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = student.attendanceNo?.toString() ?: "#",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = student.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            val fDetails = listOfNotNull(
                                student.father?.let { "Father: $it" },
                                student.grandfather?.let { "Grandfather: $it" }
                            ).joinToString(" • ")
                            if (fDetails.isNotBlank()) {
                                Text(
                                    text = fDetails,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            val idDetails = listOfNotNull(
                                student.tazkira?.let { "Tazkira: $it" },
                                student.baseNumber?.let { "Base ID: $it" }
                            ).joinToString(" • ")
                            if (idDetails.isNotBlank()) {
                                Text(
                                    text = idDetails,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    if (result != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "MIDTERM RESULT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                StatusBadge(statusInfo = StatusPresentation.formatMidtermResult(result.midResult))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Avg: ${result.midAverage?.let { String.format("%.1f", it) } ?: "—"} • Grade: ${result.midGrade ?: "—"}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "ANNUAL RESULT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                StatusBadge(statusInfo = StatusPresentation.formatAnnualResult(result.annualResult))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Avg: ${result.finalAverage?.let { String.format("%.1f", it) } ?: "—"} • Grade: ${result.annualGrade ?: "—"}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }

        // Navigation Tabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> {
                // Academic Results
                if (result == null || result.subjectResults.isEmpty()) {
                    item {
                        EmptyState(
                            title = "No Scores Entered",
                            description = "No grades or evaluation marks have been entered for this student yet."
                        )
                    }
                } else {
                    items(result.subjectResults, key = { it.subjectId }) { subjectResult ->
                        SubjectScoreRow(subjectResult)
                    }

                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "ACADEMIC TOTALS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Midterm Sum: ${result.midSum?.toInt() ?: "—"}")
                                    Text("Final Sum: ${result.finalSum?.toInt() ?: "—"}")
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Below Min (<40): ${result.below40Count}")
                                    Text("Total Subjects: ${result.subjectCount}")
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Attendance
                if (attendanceList.isEmpty()) {
                    item {
                        EmptyState(
                            title = "No Attendance Recorded",
                            description = "Attendance records for midterm or annual periods have not been logged."
                        )
                    }
                } else {
                    items(attendanceList, key = { it.id }) { att ->
                        AttendanceDetailCard(att)
                    }
                }
            }

            2 -> {
                // Official Report Card
                if (reportCard == null) {
                    item {
                        EmptyState(
                            title = "Report Card Unavailable",
                            description = "Could not generate report card data for this student."
                        )
                    }
                } else {
                    item {
                        OfficialReportCardView(reportCard)
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectScoreRow(subjectResult: SubjectResult) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subjectResult.subjectName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Mid: ${subjectResult.mid?.let { String.format("%.0f", it) } ?: "—"} • Annual: ${subjectResult.annual?.let { String.format("%.0f", it) } ?: "—"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Final: ${subjectResult.final?.let { String.format("%.0f", it) } ?: "—"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if ((subjectResult.final ?: 0.0) < 40.0 && subjectResult.final != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun AttendanceDetailCard(attendance: Attendance) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = attendance.period.replaceFirstChar { it.uppercase() } + " Attendance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${attendance.schoolDays} School Days",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AttendanceCounter("Present", attendance.present, Color(0xFF10B981))
                AttendanceCounter("Absent", attendance.absent, Color(0xFFEF4444))
                AttendanceCounter("Sick", attendance.sick, Color(0xFFF59E0B))
                AttendanceCounter("Leave", attendance.leave, Color(0xFF3B82F6))
            }

            if (!attendance.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Note: ${attendance.note}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AttendanceCounter(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OfficialReportCardView(reportCard: ReportCardResponse) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = reportCard.school.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Class: ${reportCard.schoolClass.name} • Academic Report",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(14.dp))

            if (!reportCard.message.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = reportCard.message,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(14.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            Text(
                text = "ADMINISTRATIVE APPROVALS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (reportCard.approvals.isEmpty()) {
                Text(
                    text = "No approval signatures recorded yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                reportCard.approvals.forEach { approval ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${approval.role}: ${approval.personName ?: "Pending"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (approval.approved) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF64748B).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (approval.approved) "APPROVED" else "PENDING",
                                color = if (approval.approved) Color(0xFF10B981) else Color(0xFF94A3B8),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
