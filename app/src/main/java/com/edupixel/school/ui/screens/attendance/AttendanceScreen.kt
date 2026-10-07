package com.edupixel.school.ui.screens.attendance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.edupixel.school.data.remote.models.AttendanceBulkItem
import com.edupixel.school.data.remote.models.AttendanceIn
import com.edupixel.school.data.remote.models.Student
import com.edupixel.school.ui.components.*
import com.edupixel.school.ui.theme.GlassCard
import com.edupixel.school.ui.theme.GlassScreenContainer

@Composable
fun AttendanceScreen(
    viewModel: AttendanceViewModel = viewModel(),
    onNavigateToClasses: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val activePeriod by viewModel.activePeriod.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val operationMessage by viewModel.operationMessage.collectAsState()

    var selectedMode by remember { mutableIntStateOf(0) } // 0: Individual, 1: Class-Wide Bulk
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(operationMessage) {
        operationMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        GlassScreenContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                val currentClassName = (uiState as? AttendanceUiState.Success)?.schoolClass?.name
                GlassTopAppBar(
                    title = "Attendance Entry",
                    subtitle = currentClassName?.let { "Class: $it" } ?: "Select a class first",
                    actions = {
                        IconButton(onClick = { viewModel.loadData() }) {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                        }
                    }
                )

                // Period Selector Tabs: Midterm vs Annual
                TabRow(
                    selectedTabIndex = if (activePeriod == "midterm") 0 else 1,
                    containerColor = Color.Transparent,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Tab(
                        selected = activePeriod == "midterm",
                        onClick = { viewModel.setPeriod("midterm") },
                        text = { Text("Midterm Period (۴.۵ ماهه)") }
                    )
                    Tab(
                        selected = activePeriod == "annual",
                        onClick = { viewModel.setPeriod("annual") },
                        text = { Text("Annual Period (سالانه)") }
                    )
                }

                when (val state = uiState) {
                    is AttendanceUiState.Loading -> {
                        LoadingState(message = "Loading student roster...")
                    }
                    is AttendanceUiState.Error -> {
                        ErrorState(
                            message = state.message,
                            isNetworkError = state.isNetworkError,
                            onRetry = { viewModel.loadData() }
                        )
                    }
                    is AttendanceUiState.Success -> {
                        if (state.schoolClass == null) {
                            EmptyState(
                                title = "No Class Selected",
                                description = "Please select or create a class first to record attendance.",
                                icon = Icons.Outlined.Class,
                                actionLabel = "Go to Classes",
                                onActionClick = onNavigateToClasses
                            )
                        } else if (state.students.isEmpty()) {
                            EmptyState(
                                title = "No Students Enrolled",
                                description = "Enroll students into ${state.schoolClass.name} to track attendance.",
                                icon = Icons.Outlined.People
                            )
                        } else {
                            // Sub-mode tabs: Individual vs Bulk
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = selectedMode == 0,
                                    onClick = { selectedMode = 0 },
                                    label = { Text("Individual Entry") }
                                )
                                FilterChip(
                                    selected = selectedMode == 1,
                                    onClick = { selectedMode = 1 },
                                    label = { Text("Class-Wide Bulk Entry") }
                                )
                            }

                            if (selectedMode == 0) {
                                IndividualAttendanceView(
                                    period = activePeriod,
                                    students = state.students,
                                    isSubmitting = isSubmitting,
                                    onSubmit = { studentId, attendanceIn ->
                                        viewModel.submitSingleAttendance(studentId, attendanceIn) {}
                                    }
                                )
                            } else {
                                BulkAttendanceView(
                                    period = activePeriod,
                                    students = state.students,
                                    classId = state.schoolClass.id,
                                    isSubmitting = isSubmitting,
                                    onSubmitBulk = { items ->
                                        viewModel.submitBulkAttendance(state.schoolClass.id, items) {}
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IndividualAttendanceView(
    period: String,
    students: List<Student>,
    isSubmitting: Boolean,
    onSubmit: (studentId: Int, AttendanceIn) -> Unit
) {
    var selectedStudent by remember { mutableStateOf(students.first()) }
    var showStudentMenu by remember { mutableStateOf(false) }

    var schoolDaysStr by remember(period) { mutableStateOf(if (period == "midterm") "81" else "185") }
    var presentStr by remember { mutableStateOf("") }
    var absentStr by remember { mutableStateOf("0") }
    var sickStr by remember { mutableStateOf("0") }
    var leaveStr by remember { mutableStateOf("0") }
    var note by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "RECORD ${period.uppercase()} ATTENDANCE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Student dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = "${selectedStudent.attendanceNo?.let { "#$it " } ?: ""}${selectedStudent.name}",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Student *") },
                            trailingIcon = { Icon(Icons.Outlined.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showStudentMenu = true }
                        )
                        DropdownMenu(
                            expanded = showStudentMenu,
                            onDismissRequest = { showStudentMenu = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            students.forEach { st ->
                                DropdownMenuItem(
                                    text = { Text("${st.attendanceNo?.let { "#$it " } ?: ""}${st.name} (${st.father ?: ""})") },
                                    onClick = {
                                        selectedStudent = st
                                        showStudentMenu = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = schoolDaysStr,
                        onValueChange = { schoolDaysStr = it },
                        label = { Text("Total School Days") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = presentStr,
                            onValueChange = { presentStr = it },
                            label = { Text("Present") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = absentStr,
                            onValueChange = { absentStr = it },
                            label = { Text("Absent") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = sickStr,
                            onValueChange = { sickStr = it },
                            label = { Text("Sick") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = leaveStr,
                            onValueChange = { leaveStr = it },
                            label = { Text("Leave") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Note (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val att = AttendanceIn(
                                period = period,
                                schoolDays = schoolDaysStr.toIntOrNull() ?: 0,
                                present = presentStr.toIntOrNull() ?: 0,
                                absent = absentStr.toIntOrNull() ?: 0,
                                sick = sickStr.toIntOrNull() ?: 0,
                                leave = leaveStr.toIntOrNull() ?: 0,
                                note = note.trim().ifEmpty { null }
                            )
                            onSubmit(selectedStudent.id, att)
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isSubmitting) "Saving..." else "Save Attendance")
                    }
                }
            }
        }
    }
}

@Composable
private fun BulkAttendanceView(
    period: String,
    students: List<Student>,
    classId: Int,
    isSubmitting: Boolean,
    onSubmitBulk: (List<AttendanceBulkItem>) -> Unit
) {
    var globalDays by remember(period) { mutableStateOf(if (period == "midterm") "81" else "185") }

    // Map studentId -> (present, absent)
    val map = remember(period) {
        mutableStateMapOf<Int, Pair<String, String>>()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CLASS-WIDE BULK ATTENDANCE (${period.uppercase()})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = globalDays,
                        onValueChange = { globalDays = it },
                        label = { Text("School Days in Period") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        items(students, key = { it.id }) { student ->
            val pair = map[student.id] ?: Pair("", "0")
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text(
                            text = "${student.attendanceNo?.let { "#$it " } ?: ""}${student.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = student.father ?: "—",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedTextField(
                        value = pair.first,
                        onValueChange = { map[student.id] = Pair(it, pair.second) },
                        label = { Text("Present") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = pair.second,
                        onValueChange = { map[student.id] = Pair(pair.first, it) },
                        label = { Text("Absent") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    val days = globalDays.toIntOrNull() ?: 0
                    val items = map.mapNotNull { (stId, p) ->
                        val present = p.first.toIntOrNull()
                        val absent = p.second.toIntOrNull() ?: 0
                        if (present != null) {
                            AttendanceBulkItem(
                                studentId = stId,
                                period = period,
                                schoolDays = days,
                                present = present,
                                absent = absent,
                                sick = 0,
                                leave = 0
                            )
                        } else null
                    }
                    onSubmitBulk(items)
                },
                enabled = map.isNotEmpty() && !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text(if (isSubmitting) "Saving Bulk Attendance..." else "Save Bulk Attendance")
            }
        }
    }
}
