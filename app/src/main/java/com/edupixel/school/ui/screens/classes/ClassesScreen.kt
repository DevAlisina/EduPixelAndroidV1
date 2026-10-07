package com.edupixel.school.ui.screens.classes

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.edupixel.school.data.remote.models.ClassIn
import com.edupixel.school.data.remote.models.SchoolClass
import com.edupixel.school.ui.components.*
import com.edupixel.school.ui.theme.GlassCard
import com.edupixel.school.ui.theme.GlassScreenContainer
import kotlinx.serialization.json.*

@Composable
fun ClassesScreen(
    viewModel: ClassesViewModel = viewModel(),
    onNavigateToSchools: () -> Unit,
    onNavigateToStudents: () -> Unit,
    onNavigateToSubjects: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val operationMessage by viewModel.operationMessage.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var classToEdit by remember { mutableStateOf<SchoolClass?>(null) }
    var classToDelete by remember { mutableStateOf<SchoolClass?>(null) }
    var classToPolicyEdit by remember { mutableStateOf<SchoolClass?>(null) }

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
            val hasSchool = (uiState as? ClassesUiState.Success)?.school != null
            if (hasSchool) {
                FloatingActionButton(
                    onClick = {
                        classToEdit = null
                        showDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "Add Class")
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
                val currentSchoolName = (uiState as? ClassesUiState.Success)?.school?.name
                GlassTopAppBar(
                    title = "Classes",
                    subtitle = currentSchoolName?.let { "School: $it" } ?: "Select a school first",
                    actions = {
                        IconButton(onClick = { viewModel.loadClasses() }) {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                        }
                    }
                )

                when (val state = uiState) {
                    is ClassesUiState.Loading -> {
                        LoadingState(message = "Loading classes...")
                    }
                    is ClassesUiState.Error -> {
                        ErrorState(
                            message = state.message,
                            isNetworkError = state.isNetworkError,
                            onRetry = { viewModel.loadClasses() }
                        )
                    }
                    is ClassesUiState.Success -> {
                        if (state.school == null) {
                            EmptyState(
                                title = "No School Selected",
                                description = "Please select or create a school first to manage classes.",
                                icon = Icons.Outlined.School,
                                actionLabel = "Go to Schools",
                                onActionClick = onNavigateToSchools
                            )
                        } else if (state.classes.isEmpty()) {
                            EmptyState(
                                title = "No Classes Yet",
                                description = "Create your first class (e.g. Third Grade - Section 2) for ${state.school.name}.",
                                icon = Icons.Outlined.Class,
                                actionLabel = "Add Class",
                                onActionClick = {
                                    classToEdit = null
                                    showDialog = true
                                }
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(state.classes, key = { it.id }) { cls ->
                                    val isSelected = cls.id == state.selectedClassId
                                    ClassCard(
                                        cls = cls,
                                        isSelected = isSelected,
                                        onSelect = { viewModel.selectClass(cls) },
                                        onEdit = {
                                            classToEdit = cls
                                            showDialog = true
                                        },
                                        onDelete = { classToDelete = cls },
                                        onEditPolicy = { classToPolicyEdit = cls },
                                        onViewStudents = {
                                            viewModel.selectClass(cls)
                                            onNavigateToStudents()
                                        },
                                        onViewSubjects = {
                                            viewModel.selectClass(cls)
                                            onNavigateToSubjects()
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

    if (showDialog) {
        val currentSchool = (uiState as? ClassesUiState.Success)?.school
        ClassDialog(
            classToEdit = classToEdit,
            schoolId = currentSchool?.id ?: 1,
            isSubmitting = isSubmitting,
            onDismiss = { showDialog = false },
            onConfirm = { classIn ->
                if (classToEdit == null) {
                    viewModel.createClass(classIn) { showDialog = false }
                } else {
                    viewModel.updateClass(classToEdit!!.id, classIn) { showDialog = false }
                }
            }
        )
    }

    classToPolicyEdit?.let { cls ->
        ClassPolicyDialog(
            cls = cls,
            isSubmitting = isSubmitting,
            onDismiss = { classToPolicyEdit = null },
            onSavePolicy = { policyJson ->
                viewModel.updatePolicy(cls.id, policyJson) {
                    classToPolicyEdit = null
                }
            }
        )
    }

    classToDelete?.let { cls ->
        ConfirmationDialog(
            title = "Delete Class",
            message = "Are you sure you want to delete '${cls.name}'? This will delete all enrolled students, subjects, scores, and attendance.",
            onConfirm = {
                viewModel.deleteClass(cls.id)
                classToDelete = null
            },
            onDismiss = { classToDelete = null }
        )
    }
}

@Composable
private fun ClassCard(
    cls: SchoolClass,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onEditPolicy: () -> Unit,
    onViewStudents: () -> Unit,
    onViewSubjects: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Class,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = cls.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Teacher: ${cls.teacherName ?: "Unassigned"} • Max Absence: ${cls.maxAbsence}d",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isSelected) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = onSelect,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(if (isSelected) "Active" else "Select")
                    }
                    OutlinedButton(
                        onClick = onViewStudents,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Students")
                    }
                    OutlinedButton(
                        onClick = onViewSubjects,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Subjects")
                    }
                }

                Row {
                    IconButton(onClick = onEditPolicy) {
                        Icon(Icons.Outlined.Tune, contentDescription = "Policy", tint = MaterialTheme.colorScheme.secondary)
                    }
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
}

@Composable
private fun ClassDialog(
    classToEdit: SchoolClass?,
    schoolId: Int,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (ClassIn) -> Unit
) {
    var name by remember { mutableStateOf(classToEdit?.name ?: "") }
    var gradeLevel by remember { mutableStateOf(classToEdit?.gradeLevel ?: "3") }
    var section by remember { mutableStateOf(classToEdit?.section ?: "1") }
    var teacherName by remember { mutableStateOf(classToEdit?.teacherName ?: "") }
    var maxAbsence by remember { mutableStateOf(classToEdit?.maxAbsence?.toString() ?: "56") }
    var notes by remember { mutableStateOf(classToEdit?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (classToEdit == null) "Create Class" else "Edit Class") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Class Name * (e.g. Third Grade 2)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = gradeLevel,
                        onValueChange = { gradeLevel = it },
                        label = { Text("Grade Level") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = section,
                        onValueChange = { section = it },
                        label = { Text("Section") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = teacherName,
                    onValueChange = { teacherName = it },
                    label = { Text("Teacher Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = maxAbsence,
                    onValueChange = { maxAbsence = it },
                    label = { Text("Max Absence (Default: 56)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
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
                            ClassIn(
                                schoolId = schoolId,
                                name = name.trim(),
                                gradeLevel = gradeLevel.trim().ifEmpty { null },
                                section = section.trim().ifEmpty { null },
                                teacherName = teacherName.trim().ifEmpty { null },
                                maxAbsence = maxAbsence.toIntOrNull() ?: 56,
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

@Composable
private fun ClassPolicyDialog(
    cls: SchoolClass,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSavePolicy: (JsonObject) -> Unit
) {
    val existingPolicy = cls.policy
    var maxAbsence by remember {
        mutableStateOf(existingPolicy["max_absence_days"]?.jsonPrimitive?.content ?: "${cls.maxAbsence}")
    }
    var midMinSubject by remember {
        mutableStateOf(existingPolicy["mid_min_subject"]?.jsonPrimitive?.content ?: "16")
    }
    var midAvgPass by remember {
        mutableStateOf(existingPolicy["mid_avg_pass"]?.jsonPrimitive?.content ?: "20")
    }
    var annualMinSubject by remember {
        mutableStateOf(existingPolicy["annual_min_subject"]?.jsonPrimitive?.content ?: "40")
    }
    var annualAvgPass by remember {
        mutableStateOf(existingPolicy["annual_avg_pass"]?.jsonPrimitive?.content ?: "50")
    }
    var annualRepeatBelowCount by remember {
        mutableStateOf(existingPolicy["annual_repeat_below_count"]?.jsonPrimitive?.content ?: "4")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Result Policy Rules: ${cls.name}") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Configure engine thresholds for midterm passing, annual passing, and repeat criteria.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = midMinSubject,
                        onValueChange = { midMinSubject = it },
                        label = { Text("Mid Min (16)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = midAvgPass,
                        onValueChange = { midAvgPass = it },
                        label = { Text("Mid Avg (20)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = annualMinSubject,
                        onValueChange = { annualMinSubject = it },
                        label = { Text("Annual Min (40)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = annualAvgPass,
                        onValueChange = { annualAvgPass = it },
                        label = { Text("Annual Avg (50)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = maxAbsence,
                        onValueChange = { maxAbsence = it },
                        label = { Text("Max Absence (56)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = annualRepeatBelowCount,
                        onValueChange = { annualRepeatBelowCount = it },
                        label = { Text("Repeat Count (4)") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val map = mutableMapOf<String, JsonElement>()
                    maxAbsence.toIntOrNull()?.let { map["max_absence_days"] = JsonPrimitive(it) }
                    midMinSubject.toDoubleOrNull()?.let { map["mid_min_subject"] = JsonPrimitive(it) }
                    midAvgPass.toDoubleOrNull()?.let { map["mid_avg_pass"] = JsonPrimitive(it) }
                    annualMinSubject.toDoubleOrNull()?.let { map["annual_min_subject"] = JsonPrimitive(it) }
                    annualAvgPass.toDoubleOrNull()?.let { map["annual_avg_pass"] = JsonPrimitive(it) }
                    annualRepeatBelowCount.toIntOrNull()?.let { map["annual_repeat_below_count"] = JsonPrimitive(it) }

                    onSavePolicy(JsonObject(map))
                },
                enabled = !isSubmitting
            ) {
                Text(if (isSubmitting) "Updating..." else "Save Policy")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
