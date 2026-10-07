package com.edupixel.school.ui.screens.subjects

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
import com.edupixel.school.data.remote.models.Subject
import com.edupixel.school.data.remote.models.SubjectIn
import com.edupixel.school.ui.components.*
import com.edupixel.school.ui.theme.GlassCard
import com.edupixel.school.ui.theme.GlassScreenContainer

@Composable
fun SubjectsScreen(
    viewModel: SubjectsViewModel = viewModel(),
    onNavigateToClasses: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val operationMessage by viewModel.operationMessage.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var subjectToEdit by remember { mutableStateOf<Subject?>(null) }
    var subjectToDelete by remember { mutableStateOf<Subject?>(null) }

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
            val hasClass = (uiState as? SubjectsUiState.Success)?.schoolClass != null
            if (hasClass) {
                FloatingActionButton(
                    onClick = {
                        subjectToEdit = null
                        showDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "Add Subject")
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
                val currentClassName = (uiState as? SubjectsUiState.Success)?.schoolClass?.name
                GlassTopAppBar(
                    title = "Subjects",
                    subtitle = currentClassName?.let { "Class: $it" } ?: "Select a class first",
                    actions = {
                        IconButton(onClick = { viewModel.loadSubjects() }) {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                        }
                    }
                )

                when (val state = uiState) {
                    is SubjectsUiState.Loading -> {
                        LoadingState(message = "Loading subjects...")
                    }
                    is SubjectsUiState.Error -> {
                        ErrorState(
                            message = state.message,
                            isNetworkError = state.isNetworkError,
                            onRetry = { viewModel.loadSubjects() }
                        )
                    }
                    is SubjectsUiState.Success -> {
                        if (state.schoolClass == null) {
                            EmptyState(
                                title = "No Class Selected",
                                description = "Please select or create a class first to configure its curriculum and subjects.",
                                icon = Icons.Outlined.Class,
                                actionLabel = "Go to Classes",
                                onActionClick = onNavigateToClasses
                            )
                        } else if (state.subjects.isEmpty()) {
                            EmptyState(
                                title = "No Subjects Added",
                                description = "Add subjects (e.g. Dari, Mathematics, English) for ${state.schoolClass.name}.",
                                icon = Icons.Outlined.MenuBook,
                                actionLabel = "Add Subject",
                                onActionClick = {
                                    subjectToEdit = null
                                    showDialog = true
                                }
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.subjects, key = { it.id }) { subject ->
                                    SubjectCard(
                                        subject = subject,
                                        onEdit = {
                                            subjectToEdit = subject
                                            showDialog = true
                                        },
                                        onDelete = { subjectToDelete = subject }
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
        val currentClass = (uiState as? SubjectsUiState.Success)?.schoolClass
        SubjectDialog(
            subjectToEdit = subjectToEdit,
            classId = currentClass?.id ?: 1,
            isSubmitting = isSubmitting,
            onDismiss = { showDialog = false },
            onConfirm = { subjectIn ->
                if (subjectToEdit == null) {
                    viewModel.createSubject(subjectIn) { showDialog = false }
                } else {
                    viewModel.updateSubject(subjectToEdit!!.id, subjectIn) { showDialog = false }
                }
            }
        )
    }

    subjectToDelete?.let { subject ->
        ConfirmationDialog(
            title = "Delete Subject",
            message = "Are you sure you want to delete '${subject.name}'? Existing recorded scores for this subject will also be affected.",
            onConfirm = {
                viewModel.deleteSubject(subject.id)
                subjectToDelete = null
            },
            onDismiss = { subjectToDelete = null }
        )
    }
}

@Composable
private fun SubjectCard(
    subject: Subject,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
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
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${subject.orderIndex}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = subject.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (!subject.active) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            ) {
                                Text(
                                    text = "INACTIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Mid Max: ${subject.midMax.toInt()} • Annual Max: ${subject.annualMax.toInt()} • In Results: ${if (subject.includeInResults) "Yes" else "No"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
private fun SubjectDialog(
    subjectToEdit: Subject?,
    classId: Int,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (SubjectIn) -> Unit
) {
    var name by remember { mutableStateOf(subjectToEdit?.name ?: "") }
    var orderIndexStr by remember { mutableStateOf(subjectToEdit?.orderIndex?.toString() ?: "1") }
    var midMaxStr by remember { mutableStateOf(subjectToEdit?.midMax?.toString() ?: "40.0") }
    var annualMaxStr by remember { mutableStateOf(subjectToEdit?.annualMax?.toString() ?: "60.0") }
    var active by remember { mutableStateOf(subjectToEdit?.active ?: true) }
    var includeInResults by remember { mutableStateOf(subjectToEdit?.includeInResults ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (subjectToEdit == null) "Add Subject" else "Edit Subject") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subject Name * (e.g. Mathematics)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = orderIndexStr,
                    onValueChange = { orderIndexStr = it },
                    label = { Text("Display Order Index") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = midMaxStr,
                        onValueChange = { midMaxStr = it },
                        label = { Text("Mid Max (40)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = annualMaxStr,
                        onValueChange = { annualMaxStr = it },
                        label = { Text("Annual Max (60)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Checkbox(checked = active, onCheckedChange = { active = it })
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Active", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(16.dp))
                    Checkbox(checked = includeInResults, onCheckedChange = { includeInResults = it })
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Include in Results", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            SubjectIn(
                                classId = classId,
                                name = name.trim(),
                                orderIndex = orderIndexStr.toIntOrNull() ?: 0,
                                midMax = midMaxStr.toDoubleOrNull() ?: 40.0,
                                annualMax = annualMaxStr.toDoubleOrNull() ?: 60.0,
                                active = active,
                                includeInResults = includeInResults
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
