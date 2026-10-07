package com.edupixel.school.ui.screens.scores

import androidx.compose.foundation.background
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
import com.edupixel.school.data.remote.models.ScoreIn
import com.edupixel.school.data.remote.models.Student
import com.edupixel.school.data.remote.models.Subject
import com.edupixel.school.ui.components.*
import com.edupixel.school.ui.theme.GlassCard
import com.edupixel.school.ui.theme.GlassScreenContainer

@Composable
fun ScoreEntryScreen(
    viewModel: ScoreEntryViewModel = viewModel(),
    onNavigateToClasses: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val operationMessage by viewModel.operationMessage.collectAsState()

    var selectedMode by remember { mutableIntStateOf(0) } // 0: Individual, 1: Bulk by Subject
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
                val currentClassName = (uiState as? ScoreEntryUiState.Success)?.schoolClass?.name
                GlassTopAppBar(
                    title = "Score Entry",
                    subtitle = currentClassName?.let { "Class: $it" } ?: "Select a class first",
                    actions = {
                        IconButton(onClick = { viewModel.loadData() }) {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                        }
                    }
                )

                when (val state = uiState) {
                    is ScoreEntryUiState.Loading -> {
                        LoadingState(message = "Loading students and subjects...")
                    }
                    is ScoreEntryUiState.Error -> {
                        ErrorState(
                            message = state.message,
                            isNetworkError = state.isNetworkError,
                            onRetry = { viewModel.loadData() }
                        )
                    }
                    is ScoreEntryUiState.Success -> {
                        if (state.schoolClass == null) {
                            EmptyState(
                                title = "No Class Selected",
                                description = "Please select or create a class first to enter marks.",
                                icon = Icons.Outlined.Class,
                                actionLabel = "Go to Classes",
                                onActionClick = onNavigateToClasses
                            )
                        } else if (state.students.isEmpty() || state.subjects.isEmpty()) {
                            EmptyState(
                                title = "Missing Students or Subjects",
                                description = "Both students and subjects must be created for ${state.schoolClass.name} before scores can be entered.",
                                icon = Icons.Outlined.WarningAmber,
                                actionLabel = "Manage Class Roster",
                                onActionClick = onNavigateToClasses
                            )
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                TabRow(
                                    selectedTabIndex = selectedMode,
                                    containerColor = Color.Transparent,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Tab(
                                        selected = selectedMode == 0,
                                        onClick = { selectedMode = 0 },
                                        text = { Text("Individual Entry") }
                                    )
                                    Tab(
                                        selected = selectedMode == 1,
                                        onClick = { selectedMode = 1 },
                                        text = { Text("Bulk Grid Entry") }
                                    )
                                }

                                if (selectedMode == 0) {
                                    IndividualScoreEntryView(
                                        students = state.students,
                                        subjects = state.subjects,
                                        isSubmitting = isSubmitting,
                                        onSubmit = { studentId, subjectId, mid, annual, note ->
                                            viewModel.submitSingleScore(studentId, subjectId, mid, annual, note) {}
                                        }
                                    )
                                } else {
                                    BulkScoreEntryView(
                                        classId = state.schoolClass.id,
                                        students = state.students,
                                        subjects = state.subjects,
                                        isSubmitting = isSubmitting,
                                        onSubmitBulk = { items ->
                                            viewModel.submitBulkScores(state.schoolClass.id, items) {}
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
}

@Composable
private fun IndividualScoreEntryView(
    students: List<Student>,
    subjects: List<Subject>,
    isSubmitting: Boolean,
    onSubmit: (studentId: Int, subjectId: Int, mid: Double?, annual: Double?, note: String?) -> Unit
) {
    var selectedStudent by remember { mutableStateOf(students.first()) }
    var selectedSubject by remember { mutableStateOf(subjects.first()) }
    var midScoreStr by remember { mutableStateOf("") }
    var annualScoreStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    var showStudentMenu by remember { mutableStateOf(false) }
    var showSubjectMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "RECORD STUDENT EVALUATION",
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
                            label = { Text("Student *") },
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

                    // Subject dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = "${selectedSubject.name} (Mid Max: ${selectedSubject.midMax.toInt()}, Annual Max: ${selectedSubject.annualMax.toInt()})",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Subject *") },
                            trailingIcon = { Icon(Icons.Outlined.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showSubjectMenu = true }
                        )
                        DropdownMenu(
                            expanded = showSubjectMenu,
                            onDismissRequest = { showSubjectMenu = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            subjects.forEach { sub ->
                                DropdownMenuItem(
                                    text = { Text("${sub.name} (Max: Mid ${sub.midMax.toInt()} / Ann ${sub.annualMax.toInt()})") },
                                    onClick = {
                                        selectedSubject = sub
                                        showSubjectMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = midScoreStr,
                            onValueChange = { midScoreStr = it },
                            label = { Text("Midterm (Max ${selectedSubject.midMax.toInt()})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = annualScoreStr,
                            onValueChange = { annualScoreStr = it },
                            label = { Text("Annual (Max ${selectedSubject.annualMax.toInt()})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Client-side validation warnings
                    val midVal = midScoreStr.toDoubleOrNull()
                    val annVal = annualScoreStr.toDoubleOrNull()
                    val midExceeds = midVal != null && midVal > selectedSubject.midMax
                    val annExceeds = annVal != null && annVal > selectedSubject.annualMax

                    if (midExceeds || annExceeds) {
                        Text(
                            text = "Warning: Entered mark exceeds configured maximum for this subject.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Note (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            onSubmit(
                                selectedStudent.id,
                                selectedSubject.id,
                                midScoreStr.toDoubleOrNull(),
                                annualScoreStr.toDoubleOrNull(),
                                note
                            )
                            midScoreStr = ""
                            annualScoreStr = ""
                            note = ""
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isSubmitting) "Saving..." else "Save Score")
                    }
                }
            }
        }
    }
}

@Composable
private fun BulkScoreEntryView(
    classId: Int,
    students: List<Student>,
    subjects: List<Subject>,
    isSubmitting: Boolean,
    onSubmitBulk: (List<ScoreIn>) -> Unit
) {
    var selectedSubject by remember { mutableStateOf(subjects.first()) }
    var showSubjectMenu by remember { mutableStateOf(false) }

    // Map studentId -> Pair(mid, annual)
    val scoresMap = remember(selectedSubject.id) {
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
                        text = "BULK ENTRY: SELECT SUBJECT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = "${selectedSubject.name} (Mid Max: ${selectedSubject.midMax.toInt()}, Annual Max: ${selectedSubject.annualMax.toInt()})",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Outlined.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showSubjectMenu = true }
                        )
                        DropdownMenu(
                            expanded = showSubjectMenu,
                            onDismissRequest = { showSubjectMenu = false }
                        ) {
                            subjects.forEach { sub ->
                                DropdownMenuItem(
                                    text = { Text("${sub.name} (Max: ${sub.midMax.toInt()} / ${sub.annualMax.toInt()})") },
                                    onClick = {
                                        selectedSubject = sub
                                        showSubjectMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        items(students, key = { it.id }) { student ->
            val currentPair = scoresMap[student.id] ?: Pair("", "")
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
                        value = currentPair.first,
                        onValueChange = { newVal ->
                            scoresMap[student.id] = Pair(newVal, currentPair.second)
                        },
                        label = { Text("Mid") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = currentPair.second,
                        onValueChange = { newVal ->
                            scoresMap[student.id] = Pair(currentPair.first, newVal)
                        },
                        label = { Text("Ann") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    val list = mutableListOf<ScoreIn>()
                    scoresMap.forEach { (stId, pair) ->
                        val midVal = pair.first.toDoubleOrNull()
                        val annVal = pair.second.toDoubleOrNull()
                        if (midVal != null || annVal != null) {
                            list.add(
                                ScoreIn(
                                    studentId = stId,
                                    subjectId = selectedSubject.id,
                                    mid = midVal,
                                    annual = annVal
                                )
                            )
                        }
                    }
                    onSubmitBulk(list)
                },
                enabled = scoresMap.isNotEmpty() && !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text(if (isSubmitting) "Saving Bulk Scores..." else "Save All Entered Scores")
            }
        }
    }
}
