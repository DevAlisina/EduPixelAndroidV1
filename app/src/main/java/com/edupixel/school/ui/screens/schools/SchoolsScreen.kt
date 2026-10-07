package com.edupixel.school.ui.screens.schools

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
import com.edupixel.school.data.remote.models.School
import com.edupixel.school.data.remote.models.SchoolIn
import com.edupixel.school.ui.components.*
import com.edupixel.school.ui.theme.GlassCard
import com.edupixel.school.ui.theme.GlassScreenContainer

@Composable
fun SchoolsScreen(
    viewModel: SchoolsViewModel = viewModel(),
    onNavigateToClasses: () -> Unit,
    onNavigateToYears: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val operationMessage by viewModel.operationMessage.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var schoolToEdit by remember { mutableStateOf<School?>(null) }
    var schoolToDelete by remember { mutableStateOf<School?>(null) }

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
            FloatingActionButton(
                onClick = {
                    schoolToEdit = null
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Add School")
            }
        }
    ) { paddingValues ->
        GlassScreenContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                GlassTopAppBar(
                    title = "Schools",
                    subtitle = "Manage registered institutions",
                    actions = {
                        IconButton(onClick = { viewModel.loadSchools() }) {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                        }
                    }
                )

                when (val state = uiState) {
                    is SchoolsUiState.Loading -> {
                        LoadingState(message = "Loading schools...")
                    }
                    is SchoolsUiState.Error -> {
                        ErrorState(
                            message = state.message,
                            isNetworkError = state.isNetworkError,
                            onRetry = { viewModel.loadSchools() }
                        )
                    }
                    is SchoolsUiState.Success -> {
                        if (state.schools.isEmpty()) {
                            EmptyState(
                                title = "No Schools Configured",
                                description = "Add your school to begin creating academic years and classes.",
                                icon = Icons.Outlined.School,
                                actionLabel = "Add School",
                                onActionClick = {
                                    schoolToEdit = null
                                    showDialog = true
                                }
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(state.schools, key = { it.id }) { school ->
                                    val isSelected = school.id == state.selectedSchoolId
                                    SchoolCard(
                                        school = school,
                                        isSelected = isSelected,
                                        onSelect = { viewModel.selectSchool(school) },
                                        onEdit = {
                                            schoolToEdit = school
                                            showDialog = true
                                        },
                                        onDelete = { schoolToDelete = school },
                                        onViewYears = onNavigateToYears,
                                        onViewClasses = onNavigateToClasses
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
        SchoolDialog(
            schoolToEdit = schoolToEdit,
            isSubmitting = isSubmitting,
            onDismiss = { showDialog = false },
            onConfirm = { schoolIn ->
                if (schoolToEdit == null) {
                    viewModel.createSchool(schoolIn) { showDialog = false }
                } else {
                    viewModel.updateSchool(schoolToEdit!!.id, schoolIn) { showDialog = false }
                }
            }
        )
    }

    schoolToDelete?.let { school ->
        ConfirmationDialog(
            title = "Delete School",
            message = "Are you sure you want to delete '${school.name}'? This will remove all associated classes and student records.",
            onConfirm = {
                viewModel.deleteSchool(school.id)
                schoolToDelete = null
            },
            onDismiss = { schoolToDelete = null }
        )
    }
}

@Composable
private fun SchoolCard(
    school: School,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewYears: () -> Unit,
    onViewClasses: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
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
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = school.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        val loc = listOfNotNull(school.district, school.province).joinToString(", ")
                        if (loc.isNotBlank()) {
                            Text(
                                text = loc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Principal: ${school.principalName ?: "—"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Manager: ${school.managerName ?: "—"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Solar Year: ${school.academicYearSolar ?: "—"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Qamari Year: ${school.academicYearQamari ?: "—"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(if (isSelected) "Selected" else "Set Active")
                    }
                    OutlinedButton(
                        onClick = onViewClasses,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Classes")
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
}

@Composable
private fun SchoolDialog(
    schoolToEdit: School?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (SchoolIn) -> Unit
) {
    var name by remember { mutableStateOf(schoolToEdit?.name ?: "") }
    var province by remember { mutableStateOf(schoolToEdit?.province ?: "") }
    var district by remember { mutableStateOf(schoolToEdit?.district ?: "") }
    var managerName by remember { mutableStateOf(schoolToEdit?.managerName ?: "") }
    var principalName by remember { mutableStateOf(schoolToEdit?.principalName ?: "") }
    var solarYear by remember { mutableStateOf(schoolToEdit?.academicYearSolar?.toString() ?: "1404") }
    var qamariYear by remember { mutableStateOf(schoolToEdit?.academicYearQamari?.toString() ?: "1447") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (schoolToEdit == null) "Create School" else "Edit School") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("School Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = province,
                        onValueChange = { province = it },
                        label = { Text("Province") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = district,
                        onValueChange = { district = it },
                        label = { Text("District") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = principalName,
                    onValueChange = { principalName = it },
                    label = { Text("Principal Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = managerName,
                    onValueChange = { managerName = it },
                    label = { Text("Manager Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = solarYear,
                        onValueChange = { solarYear = it },
                        label = { Text("Solar Year") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = qamariYear,
                        onValueChange = { qamariYear = it },
                        label = { Text("Qamari Year") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            SchoolIn(
                                name = name.trim(),
                                province = province.trim().ifEmpty { null },
                                district = district.trim().ifEmpty { null },
                                managerName = managerName.trim().ifEmpty { null },
                                principalName = principalName.trim().ifEmpty { null },
                                academicYearSolar = solarYear.toIntOrNull(),
                                academicYearQamari = qamariYear.toIntOrNull()
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
