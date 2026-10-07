package com.edupixel.school.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.edupixel.school.data.remote.models.School
import com.edupixel.school.data.remote.models.SchoolClass
import com.edupixel.school.ui.components.EmptyState
import com.edupixel.school.ui.components.ErrorState
import com.edupixel.school.ui.components.GlassTopAppBar
import com.edupixel.school.ui.components.LoadingState
import com.edupixel.school.ui.theme.GlassCard
import com.edupixel.school.ui.theme.GlassScreenContainer

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    onNavigateToSchools: () -> Unit,
    onNavigateToClasses: () -> Unit,
    onNavigateToStudents: () -> Unit,
    onNavigateToMidterm: () -> Unit,
    onNavigateToAnnual: () -> Unit,
    onNavigateToScores: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToReports: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    GlassScreenContainer(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            GlassTopAppBar(
                title = "EduPixel School",
                subtitle = "Academic Management System",
                actions = {
                    IconButton(onClick = { viewModel.loadData() }) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )

            when (val state = uiState) {
                is DashboardUiState.Loading -> {
                    LoadingState(message = "Loading dashboard metrics...")
                }
                is DashboardUiState.Error -> {
                    ErrorState(
                        message = state.message,
                        isNetworkError = state.isNetworkError,
                        onRetry = { viewModel.loadData() }
                    )
                }
                is DashboardUiState.Success -> {
                    if (state.schools.isEmpty()) {
                        EmptyState(
                            title = "No Schools Found",
                            description = "Start by creating a school to manage classes, students, and academic records.",
                            icon = Icons.Outlined.School,
                            actionLabel = "Add School",
                            onActionClick = onNavigateToSchools
                        )
                    } else {
                        DashboardContent(
                            state = state,
                            onSelectSchool = { viewModel.selectSchool(it) },
                            onSelectClass = { viewModel.selectClass(it) },
                            onNavigateToSchools = onNavigateToSchools,
                            onNavigateToClasses = onNavigateToClasses,
                            onNavigateToStudents = onNavigateToStudents,
                            onNavigateToMidterm = onNavigateToMidterm,
                            onNavigateToAnnual = onNavigateToAnnual,
                            onNavigateToScores = onNavigateToScores,
                            onNavigateToAttendance = onNavigateToAttendance,
                            onNavigateToReports = onNavigateToReports
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState.Success,
    onSelectSchool: (School) -> Unit,
    onSelectClass: (SchoolClass) -> Unit,
    onNavigateToSchools: () -> Unit,
    onNavigateToClasses: () -> Unit,
    onNavigateToStudents: () -> Unit,
    onNavigateToMidterm: () -> Unit,
    onNavigateToAnnual: () -> Unit,
    onNavigateToScores: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToReports: () -> Unit
) {
    var showSchoolMenu by remember { mutableStateOf(false) }
    var showClassMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Context Selector Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "ACTIVE CONTEXT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // School Dropdown Button
                        Box(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showSchoolMenu = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.School,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = state.selectedSchool?.name ?: "Select School",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(Icons.Outlined.ArrowDropDown, contentDescription = null)
                                }
                            }
                            DropdownMenu(
                                expanded = showSchoolMenu,
                                onDismissRequest = { showSchoolMenu = false }
                            ) {
                                state.schools.forEach { school ->
                                    DropdownMenuItem(
                                        text = { Text(school.name) },
                                        onClick = {
                                            onSelectSchool(school)
                                            showSchoolMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Class Dropdown Button
                        Box(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showClassMenu = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.Class,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = state.selectedClass?.name ?: "Select Class",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(Icons.Outlined.ArrowDropDown, contentDescription = null)
                                }
                            }
                            DropdownMenu(
                                expanded = showClassMenu,
                                onDismissRequest = { showClassMenu = false }
                            ) {
                                if (state.classes.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("No classes (Tap to manage)") },
                                        onClick = {
                                            showClassMenu = false
                                            onNavigateToClasses()
                                        }
                                    )
                                } else {
                                    state.classes.forEach { cls ->
                                        DropdownMenuItem(
                                            text = { Text(cls.name) },
                                            onClick = {
                                                onSelectClass(cls)
                                                showClassMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (state.selectedClass != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Teacher: ${state.selectedClass.teacherName ?: "Not Assigned"} • Max Absence: ${state.selectedClass.maxAbsence} days",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Overview Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Students",
                    value = "${state.classDashboard?.students ?: 0}",
                    icon = Icons.Outlined.People,
                    color = Color(0xFF38BDF8),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToStudents
                )
                MetricCard(
                    title = "Subjects",
                    value = "${state.classDashboard?.subjects ?: 0}",
                    icon = Icons.Outlined.MenuBook,
                    color = Color(0xFF818CF8),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToClasses
                )
            }
        }

        // Midterm Progress Card
        item {
            val midSummary = state.classDashboard?.summary?.mid ?: emptyMap()
            val successfulCount = midSummary["موفق"] ?: 0
            val effortCount = midSummary["تلاش بیشتر"] ?: 0
            val absentCount = midSummary["غایب"] ?: 0
            val totalMid = state.classDashboard?.summary?.midExamIncluded ?: 0

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToMidterm
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Assessment, contentDescription = null, tint = Color(0xFF10B981))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Midterm Results",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "$totalMid Students Included",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ProgressPill(label = "Successful", count = successfulCount, color = Color(0xFF10B981))
                        ProgressPill(label = "Need Effort", count = effortCount, color = Color(0xFFF59E0B))
                        ProgressPill(label = "Absent", count = absentCount, color = Color(0xFFEF4444))
                    }
                }
            }
        }

        // Annual Progress Card
        item {
            val annualSummary = state.classDashboard?.summary?.annual ?: emptyMap()
            val promotedCount = annualSummary["ارتقا صنف"] ?: 0
            val repeatCount = annualSummary["تکرار صنف"] ?: 0
            val conditionalCount = annualSummary["مشروط"] ?: 0
            val totalAnnual = state.classDashboard?.summary?.annualExamIncluded ?: 0

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToAnnual
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3B82F6).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.School, contentDescription = null, tint = Color(0xFF3B82F6))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Annual / Final Results",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "$totalAnnual Students Evaluated",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ProgressPill(label = "Promoted", count = promotedCount, color = Color(0xFF10B981))
                        ProgressPill(label = "Repeat", count = repeatCount, color = Color(0xFFEF4444))
                        ProgressPill(label = "Conditional", count = conditionalCount, color = Color(0xFFF59E0B))
                    }
                }
            }
        }

        // Book & Approvals summary row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val books = state.classDashboard?.books
                val approvals = state.classDashboard?.approvals

                GlassCard(
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.AutoStories, contentDescription = null, tint = Color(0xFF06B6D4), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Textbooks", style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${books?.delivered ?: 0} / ${books?.quantity ?: 0}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${books?.signed ?: 0} signed receipts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                GlassCard(
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Verified, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Approvals", style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${approvals?.approved ?: 0} / ${approvals?.total ?: 0}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Administrative signs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Quick Actions Section
        item {
            Text(
                text = "QUICK ACTIONS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    title = "Enter Scores",
                    icon = Icons.Outlined.EditNote,
                    color = Color(0xFF2563EB),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToScores
                )
                QuickActionButton(
                    title = "Attendance",
                    icon = Icons.Outlined.CalendarMonth,
                    color = Color(0xFF059669),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToAttendance
                )
                QuickActionButton(
                    title = "Reports",
                    icon = Icons.Outlined.Summarize,
                    color = Color(0xFF7C3AED),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToReports
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProgressPill(
    label: String,
    count: Int,
    color: Color
) {
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
private fun QuickActionButton(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}
