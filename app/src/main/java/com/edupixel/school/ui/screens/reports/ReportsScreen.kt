package com.edupixel.school.ui.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.edupixel.school.data.remote.models.*
import com.edupixel.school.ui.components.*
import com.edupixel.school.ui.presentation.StatusPresentation
import com.edupixel.school.ui.theme.GlassCard
import com.edupixel.school.ui.theme.GlassScreenContainer

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = viewModel(),
    onNavigateToClasses: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedIndex by viewModel.selectedReportIndex.collectAsState()

    val reportTitles = listOf(
        "Compact Register",
        "Subject Register",
        "Notice Cards",
        "Passing List",
        "Conditional List",
        "Failed / Banned",
        "Cover Sheet",
        "Approval Summary"
    )

    GlassScreenContainer(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            val currentClassName = (uiState as? ReportsUiState.Success)?.schoolClass?.name
            GlassTopAppBar(
                title = "Reports & Registers",
                subtitle = currentClassName?.let { "Class: $it" } ?: "Select a class first",
                actions = {
                    IconButton(onClick = { viewModel.loadCurrentReport() }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                    }
                }
            )

            // Horizontal Tab Bar of Available Reports
            ScrollableTabRow(
                selectedTabIndex = selectedIndex,
                containerColor = Color.Transparent,
                edgePadding = 16.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                reportTitles.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedIndex == idx,
                        onClick = { viewModel.selectReport(idx) },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (selectedIndex == idx) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            when (val state = uiState) {
                is ReportsUiState.Loading -> {
                    LoadingState(message = "Loading official report data...")
                }
                is ReportsUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        isNetworkError = state.isNetworkError,
                        onRetry = { viewModel.loadCurrentReport() }
                    )
                }
                is ReportsUiState.Success -> {
                    val reportData = state.reportData
                    if (state.schoolClass == null) {
                        EmptyState(
                            title = "No Class Selected",
                            description = "Please select or create a class first to view its records and registers.",
                            icon = Icons.Outlined.Class,
                            actionLabel = "Go to Classes",
                            onActionClick = onNavigateToClasses
                        )
                    } else if (reportData == null) {
                        EmptyState(
                            title = "Report Unavailable",
                            description = "Data could not be retrieved for the selected report."
                        )
                    } else {
                        when (reportData) {
                            is ReportData.Compact -> CompactRegisterView(reportData.rows)
                            is ReportData.SubjectRegister -> SubjectRegisterView(reportData.data)
                            is ReportData.Notice -> NoticeCardsView(reportData.cards)
                            is ReportData.StudentList -> StudentListView(reportData.title, reportData.list)
                            is ReportData.Cover -> CoverSheetView(reportData.cover)
                            is ReportData.Approvals -> ApprovalSummaryView(reportData.summary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactRegisterView(rows: List<CompactRegisterRow>) {
    if (rows.isEmpty()) {
        EmptyState(title = "Empty Register", description = "No students enrolled in this class register.")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(rows, key = { it.studentId }) { row ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = row.attendanceNo?.toString() ?: "#",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = row.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Father: ${row.father ?: "—"} • Tazkira: ${row.tazkira ?: "—"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        StatusBadge(statusInfo = StatusPresentation.formatAnnualResult(row.annualResult))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Avg: ${row.annualAverage?.let { String.format("%.1f", it) } ?: "—"} • Absent: ${row.annualAbsent}d",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectRegisterView(data: SubjectRegisterResponse) {
    if (data.rows.isEmpty()) {
        EmptyState(title = "Empty Subject Register", description = "No marks or students recorded.")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(data.rows, key = { it.studentId }) { row ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${row.attendanceNo?.let { "#$it " } ?: ""}${row.studentName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        StatusBadge(statusInfo = StatusPresentation.formatAnnualResult(row.annualResult))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Horizontally scrollable row of subject scores for this student
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        data.subjects.forEach { sub ->
                            val score = row.subjects[sub.id.toString()]
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = sub.name, style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = "${score?.final?.let { String.format("%.0f", it) } ?: "—"}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total Final: ${row.finalSum?.toInt() ?: "—"} • Average: ${row.finalAverage?.let { String.format("%.1f", it) } ?: "—"} • Grade: ${row.annualGrade ?: "—"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun NoticeCardsView(cards: List<NoticeCard>) {
    if (cards.isEmpty()) {
        EmptyState(title = "No Notice Cards", description = "No student cards available.")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(cards, key = { it.student.id }) { card ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = card.school.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${card.student.attendanceNo?.let { "#$it " } ?: ""}${card.student.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Father: ${card.student.father ?: "—"} • Class: ${card.schoolClass.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Annual: ${card.result.finalAverage?.let { String.format("%.1f", it) } ?: "—"} (${card.result.annualGrade ?: "—"})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        StatusBadge(statusInfo = StatusPresentation.formatAnnualResult(card.result.annualResult))
                    }

                    if (!card.message.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = card.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentListView(title: String, list: List<StudentResult>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "$title (${list.size} students)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        if (list.isEmpty()) {
            item {
                EmptyState(title = "No Students", description = "No students meet this criteria.")
            }
        } else {
            items(list, key = { it.studentId }) { st ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${st.attendanceNo?.let { "#$it " } ?: ""}${st.studentName}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Avg: ${st.finalAverage?.let { String.format("%.1f", it) } ?: "—"} • Grade: ${st.annualGrade ?: "—"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StatusBadge(statusInfo = StatusPresentation.formatAnnualResult(st.annualResult))
                    }
                }
            }
        }
    }
}

@Composable
private fun CoverSheetView(cover: ClassCoverResponse) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "CLASS COVER SHEET (پوش جدول)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = cover.school.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Province: ${cover.school.province ?: "—"} • District: ${cover.school.district ?: "—"}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    HorizontalDivider()

                    Text(text = "Class: ${cover.schoolClass.name}", style = MaterialTheme.typography.titleMedium)
                    Text(text = "Grade: ${cover.schoolClass.gradeLevel ?: "—"} • Section: ${cover.schoolClass.section ?: "—"}")
                    Text(text = "Assigned Teacher: ${cover.teacher ?: "—"}")
                    Text(text = "Academic Year: ${cover.academicYear?.label ?: cover.academicYear?.solarYear?.toString() ?: "—"}")
                    Text(text = "Principal: ${cover.school.principalName ?: "—"}")
                    Text(text = "Manager: ${cover.school.managerName ?: "—"}")
                }
            }
        }
    }
}

@Composable
private fun ApprovalSummaryView(summary: ApprovalSummaryResponse) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "STAFF RESPONSIBLE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Teacher: ${summary.staff["teacher"] ?: "—"}")
                    Text(text = "Principal: ${summary.staff["principal"] ?: "—"}")
                    Text(text = "Manager: ${summary.staff["manager"] ?: "—"}")
                }
            }
        }

        item {
            Text(
                text = "RECORDED APPROVALS (${summary.approvals.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (summary.approvals.isEmpty()) {
            item {
                EmptyState(title = "No Approvals", description = "No signatures recorded for this class yet.")
            }
        } else {
            items(summary.approvals, key = { it.id }) { app ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = app.role, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                text = app.personName ?: "Pending Signature",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (app.approved) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF64748B).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (app.approved) "SIGNED" else "PENDING",
                                color = if (app.approved) Color(0xFF10B981) else Color(0xFF94A3B8),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
