package com.edupixel.school.ui.screens.years

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
import com.edupixel.school.data.remote.models.AcademicYear
import com.edupixel.school.ui.components.*
import com.edupixel.school.ui.theme.GlassCard
import com.edupixel.school.ui.theme.GlassScreenContainer

@Composable
fun AcademicYearsScreen(
    viewModel: AcademicYearsViewModel = viewModel(),
    onNavigateToSchools: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val operationMessage by viewModel.operationMessage.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var yearToEdit by remember { mutableStateOf<AcademicYear?>(null) }
    var yearToDelete by remember { mutableStateOf<AcademicYear?>(null) }

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
            val hasSchool = (uiState as? YearsUiState.Success)?.school != null
            if (hasSchool) {
                FloatingActionButton(
                    onClick = {
                        yearToEdit = null
                        showDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "Add Year")
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
                val currentSchoolName = (uiState as? YearsUiState.Success)?.school?.name
                GlassTopAppBar(
                    title = "Academic Years",
                    subtitle = currentSchoolName?.let { "School: $it" } ?: "Select a school first",
                    actions = {
                        IconButton(onClick = { viewModel.loadYears() }) {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                        }
                    }
                )

                when (val state = uiState) {
                    is YearsUiState.Loading -> {
                        LoadingState(message = "Loading academic years...")
                    }
                    is YearsUiState.Error -> {
                        ErrorState(
                            message = state.message,
                            isNetworkError = state.isNetworkError,
                            onRetry = { viewModel.loadYears() }
                        )
                    }
                    is YearsUiState.Success -> {
                        if (state.school == null) {
                            EmptyState(
                                title = "No School Selected",
                                description = "Please select or create a school first to configure academic years.",
                                icon = Icons.Outlined.School,
                                actionLabel = "Go to Schools",
                                onActionClick = onNavigateToSchools
                            )
                        } else if (state.years.isEmpty()) {
                            EmptyState(
                                title = "No Academic Years",
                                description = "Create an academic year for ${state.school.name} (e.g. Solar 1404 / Qamari 1447).",
                                icon = Icons.Outlined.CalendarMonth,
                                actionLabel = "Add Academic Year",
                                onActionClick = {
                                    yearToEdit = null
                                    showDialog = true
                                }
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(state.years, key = { it.id }) { year ->
                                    YearCard(
                                        year = year,
                                        onEdit = {
                                            yearToEdit = year
                                            showDialog = true
                                        },
                                        onDelete = { yearToDelete = year }
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
        YearDialog(
            yearToEdit = yearToEdit,
            isSubmitting = isSubmitting,
            onDismiss = { showDialog = false },
            onConfirm = { solar, qamari, label, isActive ->
                if (yearToEdit == null) {
                    viewModel.createYear(solar, qamari, label, isActive) { showDialog = false }
                } else {
                    viewModel.updateYear(yearToEdit!!.id, solar, qamari, label, isActive) { showDialog = false }
                }
            }
        )
    }

    yearToDelete?.let { year ->
        ConfirmationDialog(
            title = "Delete Academic Year",
            message = "Are you sure you want to delete year '${year.label ?: year.solarYear}'?",
            onConfirm = {
                viewModel.deleteYear(year.id)
                yearToDelete = null
            },
            onDismiss = { yearToDelete = null }
        )
    }
}

@Composable
private fun YearCard(
    year: AcademicYear,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (year.isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = if (year.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = year.label ?: "Solar ${year.solarYear}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (year.isActive) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    color = Color(0xFF10B981),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Solar: ${year.solarYear} • Qamari: ${year.qamariYear ?: "—"}",
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
private fun YearDialog(
    yearToEdit: AcademicYear?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (solar: Int, qamari: Int?, label: String?, isActive: Boolean) -> Unit
) {
    var solarYearStr by remember { mutableStateOf(yearToEdit?.solarYear?.toString() ?: "1404") }
    var qamariYearStr by remember { mutableStateOf(yearToEdit?.qamariYear?.toString() ?: "1447") }
    var label by remember { mutableStateOf(yearToEdit?.label ?: "1404-1447") }
    var isActive by remember { mutableStateOf(yearToEdit?.isActive ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (yearToEdit == null) "Create Academic Year" else "Edit Academic Year") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = solarYearStr,
                    onValueChange = { solarYearStr = it },
                    label = { Text("Solar Year *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = qamariYearStr,
                    onValueChange = { qamariYearStr = it },
                    label = { Text("Qamari Year") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Display Label") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Checkbox(checked = isActive, onCheckedChange = { isActive = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Set as Active Year", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            val solar = solarYearStr.toIntOrNull()
            Button(
                onClick = {
                    if (solar != null) {
                        onConfirm(solar, qamariYearStr.toIntOrNull(), label.trim().ifEmpty { null }, isActive)
                    }
                },
                enabled = solar != null && !isSubmitting
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
