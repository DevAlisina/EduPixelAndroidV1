package com.edupixel.school.ui.screens.approvals

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
import com.edupixel.school.data.remote.models.Approval
import com.edupixel.school.data.remote.models.ApprovalIn
import com.edupixel.school.ui.components.*
import com.edupixel.school.ui.theme.GlassCard
import com.edupixel.school.ui.theme.GlassScreenContainer

@Composable
fun ApprovalsScreen(
    viewModel: ApprovalsViewModel = viewModel(),
    onNavigateToClasses: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val operationMessage by viewModel.operationMessage.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var approvalToEdit by remember { mutableStateOf<Approval?>(null) }
    var approvalToDelete by remember { mutableStateOf<Approval?>(null) }

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
            val hasClass = (uiState as? ApprovalsUiState.Success)?.schoolClass != null
            if (hasClass) {
                FloatingActionButton(
                    onClick = {
                        approvalToEdit = null
                        showDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "Add Approval")
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
                val currentClassName = (uiState as? ApprovalsUiState.Success)?.schoolClass?.name
                GlassTopAppBar(
                    title = "Class Approvals",
                    subtitle = currentClassName?.let { "Class: $it" } ?: "Select a class first",
                    actions = {
                        IconButton(onClick = { viewModel.loadApprovals() }) {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                        }
                    }
                )

                when (val state = uiState) {
                    is ApprovalsUiState.Loading -> {
                        LoadingState(message = "Loading approval records...")
                    }
                    is ApprovalsUiState.Error -> {
                        ErrorState(
                            message = state.message,
                            isNetworkError = state.isNetworkError,
                            onRetry = { viewModel.loadApprovals() }
                        )
                    }
                    is ApprovalsUiState.Success -> {
                        if (state.schoolClass == null) {
                            EmptyState(
                                title = "No Class Selected",
                                description = "Please select or create a class first to record approval signatories.",
                                icon = Icons.Outlined.Class,
                                actionLabel = "Go to Classes",
                                onActionClick = onNavigateToClasses
                            )
                        } else if (state.approvals.isEmpty()) {
                            EmptyState(
                                title = "No Approval Signatures",
                                description = "Add designated reviewers, headmasters, or teachers for ${state.schoolClass.name}.",
                                icon = Icons.Outlined.Verified,
                                actionLabel = "Add Signatory",
                                onActionClick = {
                                    approvalToEdit = null
                                    showDialog = true
                                }
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.approvals, key = { it.id }) { app ->
                                    ApprovalCard(
                                        approval = app,
                                        onToggleStatus = { viewModel.toggleApprovalStatus(app) },
                                        onEdit = {
                                            approvalToEdit = app
                                            showDialog = true
                                        },
                                        onDelete = { approvalToDelete = app }
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
        val currentClass = (uiState as? ApprovalsUiState.Success)?.schoolClass
        ApprovalDialog(
            approvalToEdit = approvalToEdit,
            classId = currentClass?.id ?: 1,
            isSubmitting = isSubmitting,
            onDismiss = { showDialog = false },
            onConfirm = { appIn ->
                if (approvalToEdit == null) {
                    viewModel.createApproval(appIn) { showDialog = false }
                } else {
                    viewModel.updateApproval(approvalToEdit!!.id, appIn) { showDialog = false }
                }
            }
        )
    }

    approvalToDelete?.let { app ->
        ConfirmationDialog(
            title = "Delete Approval Record",
            message = "Are you sure you want to remove the approval record for '${app.role}'?",
            onConfirm = {
                viewModel.deleteApproval(app.id)
                approvalToDelete = null
            },
            onDismiss = { approvalToDelete = null }
        )
    }
}

@Composable
private fun ApprovalCard(
    approval: Approval,
    onToggleStatus: () -> Unit,
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
                        .background(
                            if (approval.approved) Color(0xFF10B981).copy(alpha = 0.2f)
                            else Color(0xFF64748B).copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (approval.approved) Icons.Outlined.CheckCircle else Icons.Outlined.Pending,
                        contentDescription = null,
                        tint = if (approval.approved) Color(0xFF10B981) else Color(0xFF94A3B8)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = approval.role, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Signee: ${approval.personName ?: "Not Assigned"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!approval.note.isNullOrBlank()) {
                        Text(
                            text = "Note: ${approval.note}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    selected = approval.approved,
                    onClick = onToggleStatus,
                    label = { Text(if (approval.approved) "Approved" else "Pending") }
                )
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
private fun ApprovalDialog(
    approvalToEdit: Approval?,
    classId: Int,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (ApprovalIn) -> Unit
) {
    var role by remember { mutableStateOf(approvalToEdit?.role ?: "Class Teacher") }
    var personName by remember { mutableStateOf(approvalToEdit?.personName ?: "") }
    var note by remember { mutableStateOf(approvalToEdit?.note ?: "") }
    var approved by remember { mutableStateOf(approvalToEdit?.approved ?: false) }
    var classType by remember { mutableStateOf(approvalToEdit?.classType ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (approvalToEdit == null) "Add Approval Signatory" else "Edit Approval") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Role * (e.g. Principal, Headmaster, Teacher)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = personName,
                    onValueChange = { personName = it },
                    label = { Text("Signatory Person Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = classType,
                    onValueChange = { classType = it },
                    label = { Text("Class Type (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note / Remark") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = approved, onCheckedChange = { approved = it })
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mark Approved / Signed")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (role.isNotBlank()) {
                        onConfirm(
                            ApprovalIn(
                                classId = classId,
                                role = role.trim(),
                                personName = personName.trim().ifEmpty { null },
                                note = note.trim().ifEmpty { null },
                                approved = approved,
                                classType = classType.trim().ifEmpty { null }
                            )
                        )
                    }
                },
                enabled = role.isNotBlank() && !isSubmitting
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
