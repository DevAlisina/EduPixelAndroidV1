package com.edupixel.school.ui.screens.results

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
import com.edupixel.school.data.remote.models.StudentResult
import com.edupixel.school.ui.components.*
import com.edupixel.school.ui.presentation.StatusPresentation
import com.edupixel.school.ui.theme.GlassCard
import com.edupixel.school.ui.theme.GlassScreenContainer

@Composable
fun AnnualResultsScreen(
    viewModel: AnnualResultsViewModel = viewModel(),
    onNavigateToClasses: () -> Unit,
    onStudentClick: (studentId: Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedStudentForDetail by remember { mutableStateOf<StudentResult?>(null) }

    GlassScreenContainer(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            val currentClassName = (uiState as? AnnualResultsUiState.Success)?.schoolClass?.name
            GlassTopAppBar(
                title = "Annual Results",
                subtitle = currentClassName?.let { "Class: $it (سالانه)" } ?: "Select a class first",
                actions = {
                    IconButton(onClick = { viewModel.loadAnnualResults() }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                    }
                }
            )

            when (val state = uiState) {
                is AnnualResultsUiState.Loading -> {
                    LoadingState(message = "Calculating class final annual outcomes...")
                }
                is AnnualResultsUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        isNetworkError = state.isNetworkError,
                        onRetry = { viewModel.loadAnnualResults() }
                    )
                }
                is AnnualResultsUiState.Success -> {
                    if (state.schoolClass == null) {
                        EmptyState(
                            title = "No Class Selected",
                            description = "Please select or create a class first to view its annual results.",
                            icon = Icons.Outlined.Class,
                            actionLabel = "Go to Classes",
                            onActionClick = onNavigateToClasses
                        )
                    } else if (state.resultsResponse.students.isEmpty()) {
                        EmptyState(
                            title = "No Results Available",
                            description = "No student outcome data has been calculated yet for ${state.schoolClass.name}.",
                            icon = Icons.Outlined.School
                        )
                    } else {
                        AnnualContent(
                            state = state,
                            onSearchChange = { viewModel.onSearchChanged(it) },
                            onFilterChange = { viewModel.onStatusFilterChanged(it) },
                            onSelectStudent = { selectedStudentForDetail = it }
                        )
                    }
                }
            }
        }
    }

    selectedStudentForDetail?.let { student ->
        AnnualStudentDetailDialog(
            studentResult = student,
            onDismiss = { selectedStudentForDetail = null }
        )
    }
}

@Composable
private fun AnnualContent(
    state: AnnualResultsUiState.Success,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String?) -> Unit,
    onSelectStudent: (StudentResult) -> Unit
) {
    val summary = state.resultsResponse.summary?.annual ?: emptyMap()
    val promoted = summary["ارتقا صنف"] ?: 0
    val repeat = summary["تکرار صنف"] ?: 0
    val conditional = summary["مشروط"] ?: 0
    val banned = summary["محروم"] ?: 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "ANNUAL OUTCOMES OVERVIEW",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        OutcomeCounter("Promoted", promoted, Color(0xFF10B981))
                        OutcomeCounter("Repeat", repeat, Color(0xFFEF4444))
                        OutcomeCounter("Conditional", conditional, Color(0xFFF59E0B))
                        OutcomeCounter("Banned", banned, Color(0xFFDC2626))
                    }
                }
            }
        }

        // Search and Filters
        item {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search by name or roll number...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            val filters = listOf(
                null to "All",
                "ارتقا صنف" to "Promoted (ارتقا)",
                "تکرار صنف" to "Repeat (تکرار)",
                "مشروط" to "Conditional (مشروط)",
                "معذرتی" to "Excused (معذرتی)",
                "محروم" to "Banned (محروم)"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { (rawVal, label) ->
                    FilterChip(
                        selected = state.statusFilter == rawVal,
                        onClick = { onFilterChange(rawVal) },
                        label = { Text(label) }
                    )
                }
            }
        }

        // Student Cards
        items(state.filteredStudents, key = { it.studentId }) { student ->
            AnnualStudentCard(
                student = student,
                onClick = { onSelectStudent(student) }
            )
        }
    }
}

@Composable
private fun OutcomeCounter(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.headlineSmall,
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
private fun AnnualStudentCard(
    student: StudentResult,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = student.attendanceNo?.toString() ?: "#",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = student.studentName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Below Min (<40): ${student.below40Count} • Subjects: ${student.enteredAnnualSubjectCount}/${student.subjectCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                StatusBadge(statusInfo = StatusPresentation.formatAnnualResult(student.annualResult))
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text(
                            text = "FINAL SUM",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = student.finalSum?.let { String.format("%.0f", it) } ?: "—",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text(
                            text = "AVERAGE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = student.finalAverage?.let { String.format("%.1f", it) } ?: "—",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text(
                            text = "GRADE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = student.annualGrade ?: "—",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                val annAtt = student.attendance?.annual
                if (annAtt != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "ABSENCE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${annAtt.absent} days",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (annAtt.absent > 56) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnnualStudentDetailDialog(
    studentResult: StudentResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "${studentResult.attendanceNo?.let { "#$it " } ?: ""}${studentResult.studentName}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Annual Evaluation & Subject Breakdown",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subject", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text("Mid + Ann = Final", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider()
                }

                items(studentResult.subjectResults, key = { it.subjectId }) { sub ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sub.subjectName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        val midStr = sub.mid?.let { String.format("%.0f", it) } ?: "—"
                        val annStr = sub.annual?.let { String.format("%.0f", it) } ?: "—"
                        val finStr = sub.final?.let { String.format("%.0f", it) } ?: "—"
                        Text(
                            text = "$midStr + $annStr = $finStr",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if ((sub.final ?: 0.0) < 40.0 && sub.final != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Final Total Sum:", style = MaterialTheme.typography.bodyMedium)
                        Text("${studentResult.finalSum?.toInt() ?: "—"}", fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Final Average:", style = MaterialTheme.typography.bodyMedium)
                        Text("${studentResult.finalAverage?.let { String.format("%.2f", it) } ?: "—"}", fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Annual Grade:", style = MaterialTheme.typography.bodyMedium)
                        Text("${studentResult.annualGrade ?: "—"}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Official Annual Outcome:", style = MaterialTheme.typography.bodyMedium)
                        StatusBadge(statusInfo = StatusPresentation.formatAnnualResult(studentResult.annualResult))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
