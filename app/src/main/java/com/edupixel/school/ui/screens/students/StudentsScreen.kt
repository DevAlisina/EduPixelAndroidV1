package com.edupixel.school.ui.screens.students

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.edupixel.school.data.remote.models.Student
import com.edupixel.school.data.remote.models.StudentIn
import com.edupixel.school.ui.components.*
import com.edupixel.school.ui.theme.GlassCard
import com.edupixel.school.ui.theme.GlassScreenContainer

@Composable
fun StudentsScreen(
    viewModel: StudentsViewModel = viewModel(),
    onNavigateToClasses: () -> Unit,
    onStudentClick: (studentId: Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val operationMessage by viewModel.operationMessage.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var studentToEdit by remember { mutableStateOf<Student?>(null) }
    var studentToDelete by remember { mutableStateOf<Student?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(operationMessage) {
        operationMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            val hasClass = (uiState as? StudentsUiState.Success)?.schoolClass != null
            if (hasClass) {
                FloatingActionButton(
                    onClick = {
                        studentToEdit = null
                        showDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Outlined.PersonAdd, contentDescription = "Add Student")
                }
            }
        }
    ) { paddingValues ->
        GlassScreenContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                val currentClassName = (uiState as? StudentsUiState.Success)?.schoolClass?.name
                GlassTopAppBar(
                    title = "Students",
                    subtitle = currentClassName?.let { "Class: $it" } ?: "Select a class first",
                    actions = {
                        IconButton(onClick = { viewModel.loadStudents() }) {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                        }
                    }
                )

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("Search by student name, father, or ID...") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Outlined.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                when (val state = uiState) {
                    is StudentsUiState.Loading -> {
                        LoadingState(message = "Loading student roster...")
                    }
                    is StudentsUiState.Error -> {
                        ErrorState(
                            message = state.message,
                            isNetworkError = state.isNetworkError,
                            onRetry = { viewModel.loadStudents() }
                        )
                    }
                    is StudentsUiState.Success -> {
                        if (state.schoolClass == null) {
                            EmptyState(
                                title = "No Class Selected",
                                description = "Please select or create a class first to manage enrolled students.",
                                icon = Icons.Outlined.Class,
                                actionLabel = "Go to Classes",
                                onActionClick = onNavigateToClasses
                            )
                        } else if (state.students.isEmpty()) {
                            EmptyState(
                                title = if (searchQuery.isNotBlank()) "No Matching Students" else "No Students Enrolled",
                                description = if (searchQuery.isNotBlank()) "Try searching for a different name or clear the filter." else "Enroll students into ${state.schoolClass.name}.",
                                icon = Icons.Outlined.People,
                                actionLabel = if (searchQuery.isBlank()) "Enroll Student" else null,
                                onActionClick = {
                                    studentToEdit = null
                                    showDialog = true
                                }
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.students, key = { it.id }) { student ->
                                    StudentCard(
                                        student = student,
                                        onClick = { onStudentClick(student.id) },
                                        onEdit = {
                                            studentToEdit = student
                                            showDialog = true
                                        },
                                        onDelete = { studentToDelete = student }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        val currentClass = (uiState as? StudentsUiState.Success)?.schoolClass
        StudentDialog(
            studentToEdit = studentToEdit,
            classId = currentClass?.id ?: 1,
            isSubmitting = isSubmitting,
            onDismiss = { showDialog = false },
            onConfirm = { studentIn ->
                if (studentToEdit == null) {
                    viewModel.createStudent(studentIn) { showDialog = false }
                } else {
                    viewModel.updateStudent(studentToEdit!!.id, studentIn) { showDialog = false }
                }
            }
        )
    }

    studentToDelete?.let { student ->
        ConfirmationDialog(
            title = "Delete Student",
            message = "Are you sure you want to delete '${student.name}'? This will delete all of their scores and attendance records.",
            onConfirm = {
                viewModel.deleteStudent(student.id)
                studentToDelete = null
            },
            onDismiss = { studentToDelete = null }
        )
    }
}

@Composable
private fun StudentCard(
    student: Student,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
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
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    val details = listOfNotNull(
                        student.father?.let { "Father: $it" },
                        student.tazkira?.let { "Tazkira: $it" }
                    ).joinToString(" • ")
                    if (details.isNotBlank()) {
                        Text(
                            text = details,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!student.baseNumber.isNullOrBlank()) {
                        Text(
                            text = "Base ID: ${student.baseNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun StudentDialog(
    studentToEdit: Student?,
    classId: Int,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (StudentIn) -> Unit
) {
    var attendanceNoStr by remember { mutableStateOf(studentToEdit?.attendanceNo?.toString() ?: "") }
    var name by remember { mutableStateOf(studentToEdit?.name ?: "") }
    var father by remember { mutableStateOf(studentToEdit?.father ?: "") }
    var grandfather by remember { mutableStateOf(studentToEdit?.grandfather ?: "") }
    var baseNumber by remember { mutableStateOf(studentToEdit?.baseNumber ?: "") }
    var tazkira by remember { mutableStateOf(studentToEdit?.tazkira ?: "") }
    var notes by remember { mutableStateOf(studentToEdit?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (studentToEdit == null) "Enroll Student" else "Edit Student") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = attendanceNoStr,
                        onValueChange = { attendanceNoStr = it },
                        label = { Text("Roll No") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Student Name *") },
                        modifier = Modifier.weight(2f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = father,
                        onValueChange = { father = it },
                        label = { Text("Father Name") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = grandfather,
                        onValueChange = { grandfather = it },
                        label = { Text("Grandfather") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tazkira,
                        onValueChange = { tazkira = it },
                        label = { Text("Tazkira No") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = baseNumber,
                        onValueChange = { baseNumber = it },
                        label = { Text("Base Number") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            StudentIn(
                                classId = classId,
                                attendanceNo = attendanceNoStr.toIntOrNull(),
                                name = name.trim(),
                                father = father.trim().ifEmpty { null },
                                grandfather = grandfather.trim().ifEmpty { null },
                                baseNumber = baseNumber.trim().ifEmpty { null },
                                tazkira = tazkira.trim().ifEmpty { null },
                                notes = notes.trim().ifEmpty { null }
                            )
                        )
                    }
                },
                enabled = name.isNotBlank() && !isSubmitting
            ) {
                Text(if (isSubmitting) "Saving..." else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
