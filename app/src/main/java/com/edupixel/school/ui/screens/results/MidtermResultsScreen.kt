package com.edupixel.school.ui.screens.results

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.edupixel.school.data.remote.models.StudentResult
import com.edupixel.school.ui.components.*
import com.edupixel.school.ui.presentation.StatusPresentation
import com.edupixel.school.ui.theme.GlassCard
import com.edupixel.school.ui.theme.GlassScreenContainer

@Composable
fun MidtermResultsScreen(
    viewModel: MidtermResultsViewModel = viewModel(),
    onNavigateToClasses: () -> Unit,
    onStudentClick: (studentId: Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedStudentForDetail by remember { mutableStateOf<StudentResult?>(null) }

    GlassScreenContainer(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            val currentClassName = (uiState as? MidtermResultsUiState.Success)?.schoolClass?.name
            GlassTopAppBar(
                title = "Midterm Results",
                subtitle = currentClassName?.let { "Class: $it (۴.۵ ماهه)" } ?: "Select a class first",
                actions = {
                    IconButton(onClick = { viewModel.loadMidtermResults() }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                    }
                }
            )

            when (val state = uiState) {
                is MidtermResultsUiState.Loading -> {
                    LoadingState(message = "Calculating class midterm results...")
                }
                is MidtermResultsUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        isNetworkError = state.isNetworkError,
                        onRetry = { viewModel.loadMidtermResults() }
                    )
                }
                is MidtermResultsUiState.Success -> {
                    if (state.schoolClass == null) {
                        EmptyState(
                            title = "No Class Selected",
                            description = "Please select or create a class first to view its midterm results.",
                            icon = Icons.Outlined.Class,
                            actionLabel = "Go to Classes",
                            onActionClick = onNavigateToClasses
                        )
                    } else if (state.resultsResponse.students.isEmpty()) {
                        EmptyState(
                            title = "No Results Recorded",
                            description = "Midterm examination results have not been recorded yet for ${state.schoolClass.name}.",
                            icon = Icons.Outlined.Assessment
                        )
                    } else {
                        MidtermContent(
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
        MidtermStudentDetailDialog(
            studentResult = student,
            onDismiss = { selectedStudentForDetail = null }
        )
    }
}

@Composable
private fun MidtermContent(
    state: MidtermResultsUiState.Success,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String?) -> Unit,
    onSelectStudent: (StudentResult) -> Unit
) {
    val totalStudents = state.resultsResponse.students.size
    val evaluatedCount = state.resultsResponse.students.count { it.midResult != null }
    val completionPercent = if (totalStudents > 0) (evaluatedCount * 100) / totalStudents else 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Class Metrics Header Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "MIDTERM COMPLETION",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "$evaluatedCount / $totalStudents Students Evaluated",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "$completionPercent%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { completionPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
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

            // Filter Chips
            val filters = listOf(
                null to "All",
                "موفق" to "Successful (موفق)",
                "تلاش بیشتر" to "Need Effort (تلاش بیشتر)",
                "معذرتي" to "Excused (معذرتی)",
                "غایب" to "Absent (غایب)"
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

        // Student Results Cards
        items(state.filteredStudents, key = { it.studentId }) { student ->
            MidtermStudentCard(
                student = student,
                onClick = { onSelectStudent(student) }
            )
        }
    }
}

@Composable
private fun MidtermStudentCard(
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
                            text = "${student.enteredMidSubjectCount} of ${student.subjectCount} subjects entered",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                StatusBadge(statusInfo = StatusPresentation.formatMidtermResult(student.midResult))
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
                            text = "TOTAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = student.midSum?.let { String.format("%.0f", it) } ?: "—",
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
                            text = student.midAverage?.let { String.format("%.1f", it) } ?: "—",
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
                            text = student.midGrade ?: "—",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                val midAtt = student.attendance?.mid
                if (midAtt != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "ATTENDANCE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${midAtt.present}/${midAtt.schoolDays}d",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MidtermStudentDetailDialog(
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
                    text = "Midterm Examination Breakdown",
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
                        Text("Score / Max", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
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
                        Text(
                            text = "${sub.mid?.let { String.format("%.0f", it) } ?: "—"} / 40",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if ((sub.mid ?: 0.0) < 16.0 && sub.mid != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
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
                        Text("Midterm Total Sum:", style = MaterialTheme.typography.bodyMedium)
                        Text("${studentResult.midSum?.toInt() ?: "—"}", fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Midterm Average:", style = MaterialTheme.typography.bodyMedium)
                        Text("${studentResult.midAverage?.let { String.format("%.2f", it) } ?: "—"}", fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Grade:", style = MaterialTheme.typography.bodyMedium)
                        Text("${studentResult.midGrade ?: "—"}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Final Midterm Result:", style = MaterialTheme.typography.bodyMedium)
                        StatusBadge(statusInfo = StatusPresentation.formatMidtermResult(studentResult.midResult))
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
