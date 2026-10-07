package com.edupixel.school.ui.screens.books

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.edupixel.school.data.remote.models.BookDistribution
import com.edupixel.school.data.remote.models.DistributionIn
import com.edupixel.school.data.remote.models.Student
import com.edupixel.school.data.remote.models.Subject
import com.edupixel.school.ui.components.*
import com.edupixel.school.ui.theme.GlassCard
import com.edupixel.school.ui.theme.GlassScreenContainer

@Composable
fun BooksScreen(
    viewModel: BooksViewModel = viewModel(),
    onNavigateToClasses: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val operationMessage by viewModel.operationMessage.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var bookToEdit by remember { mutableStateOf<BookDistribution?>(null) }
    var bookToDelete by remember { mutableStateOf<BookDistribution?>(null) }

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
            val hasClass = (uiState as? BooksUiState.Success)?.schoolClass != null
            if (hasClass) {
                FloatingActionButton(
                    onClick = {
                        bookToEdit = null
                        showDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "Distribute Book")
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
                val currentClassName = (uiState as? BooksUiState.Success)?.schoolClass?.name
                GlassTopAppBar(
                    title = "Book Distribution",
                    subtitle = currentClassName?.let { "Class: $it" } ?: "Select a class first",
                    actions = {
                        IconButton(onClick = { viewModel.loadBooks() }) {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                        }
                    }
                )

                when (val state = uiState) {
                    is BooksUiState.Loading -> {
                        LoadingState(message = "Loading book distribution records...")
                    }
                    is BooksUiState.Error -> {
                        ErrorState(
                            message = state.message,
                            isNetworkError = state.isNetworkError,
                            onRetry = { viewModel.loadBooks() }
                        )
                    }
                    is BooksUiState.Success -> {
                        if (state.schoolClass == null) {
                            EmptyState(
                                title = "No Class Selected",
                                description = "Please select or create a class first to track textbook distribution.",
                                icon = Icons.Outlined.Class,
                                actionLabel = "Go to Classes",
                                onActionClick = onNavigateToClasses
                            )
                        } else if (state.distributions.isEmpty()) {
                            EmptyState(
                                title = "No Textbooks Distributed",
                                description = "Record distributed books, receipts, and signatures for ${state.schoolClass.name}.",
                                icon = Icons.Outlined.AutoStories,
                                actionLabel = "Record Distribution",
                                onActionClick = {
                                    bookToEdit = null
                                    showDialog = true
                                }
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.distributions, key = { it.id }) { dist ->
                                    val student = state.students.find { it.id == dist.studentId }
                                    val subject = state.subjects.find { it.id == dist.subjectId }
                                    BookDistributionCard(
                                        dist = dist,
                                        studentName = student?.name ?: "Student #${dist.studentId}",
                                        subjectName = subject?.name ?: "Subject #${dist.subjectId}",
                                        onEdit = {
                                            bookToEdit = dist
                                            showDialog = true
                                        },
                                        onDelete = { bookToDelete = dist }
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
        val s = uiState as? BooksUiState.Success
        if (s?.schoolClass != null && s.students.isNotEmpty() && s.subjects.isNotEmpty()) {
            BookDialog(
                bookToEdit = bookToEdit,
                classId = s.schoolClass.id,
                students = s.students,
                subjects = s.subjects,
                isSubmitting = isSubmitting,
                onDismiss = { showDialog = false },
                onConfirm = { distIn ->
                    if (bookToEdit == null) {
                        viewModel.createDistribution(distIn) { showDialog = false }
                    } else {
                        viewModel.updateDistribution(bookToEdit!!.id, distIn) { showDialog = false }
                    }
                }
            )
        }
    }

    bookToDelete?.let { dist ->
        ConfirmationDialog(
            title = "Delete Distribution Record",
            message = "Are you sure you want to remove this book distribution record?",
            onConfirm = {
                viewModel.deleteDistribution(dist.id)
                bookToDelete = null
            },
            onDismiss = { bookToDelete = null }
        )
    }
}

@Composable
private fun BookDistributionCard(
    dist: BookDistribution,
    studentName: String,
    subjectName: String,
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
                        .background(Color(0xFF06B6D4).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.AutoStories, contentDescription = null, tint = Color(0xFF06B6D4))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = studentName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Book: $subjectName • Qty: ${dist.quantity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (dist.delivered) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (dist.delivered) "DELIVERED" else "NOT DELIVERED",
                                color = if (dist.delivered) Color(0xFF10B981) else Color(0xFFEF4444),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (dist.signed) Color(0xFF3B82F6).copy(alpha = 0.2f) else Color(0xFF64748B).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (dist.signed) "SIGNED" else "UNSIGNED",
                                color = if (dist.signed) Color(0xFF3B82F6) else Color(0xFF94A3B8),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
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
private fun BookDialog(
    bookToEdit: BookDistribution?,
    classId: Int,
    students: List<Student>,
    subjects: List<Subject>,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (DistributionIn) -> Unit
) {
    var selectedStudent by remember {
        mutableStateOf(students.find { it.id == bookToEdit?.studentId } ?: students.first())
    }
    var selectedSubject by remember {
        mutableStateOf(subjects.find { it.id == bookToEdit?.subjectId } ?: subjects.first())
    }
    var quantityStr by remember { mutableStateOf(bookToEdit?.quantity?.toString() ?: "1") }
    var delivered by remember { mutableStateOf(bookToEdit?.delivered ?: true) }
    var signed by remember { mutableStateOf(bookToEdit?.signed ?: false) }
    var notes by remember { mutableStateOf(bookToEdit?.notes ?: "") }

    var showStudentMenu by remember { mutableStateOf(false) }
    var showSubjectMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (bookToEdit == null) "Record Book Distribution" else "Edit Book Record") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Student picker
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
                        onDismissRequest = { showStudentMenu = false }
                    ) {
                        students.forEach { st ->
                            DropdownMenuItem(
                                text = { Text("${st.attendanceNo?.let { "#$it " } ?: ""}${st.name}") },
                                onClick = {
                                    selectedStudent = st
                                    showStudentMenu = false
                                }
                            )
                        }
                    }
                }

                // Subject picker
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedSubject.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subject / Textbook *") },
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
                                text = { Text(sub.name) },
                                onClick = {
                                    selectedSubject = sub
                                    showSubjectMenu = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = quantityStr,
                    onValueChange = { quantityStr = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = delivered, onCheckedChange = { delivered = it })
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delivered")
                    Spacer(modifier = Modifier.width(16.dp))
                    Checkbox(checked = signed, onCheckedChange = { signed = it })
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Receipt Signed")
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
                    onConfirm(
                        DistributionIn(
                            classId = classId,
                            studentId = selectedStudent.id,
                            subjectId = selectedSubject.id,
                            quantity = quantityStr.toIntOrNull() ?: 1,
                            delivered = delivered,
                            signed = signed,
                            notes = notes.trim().ifEmpty { null }
                        )
                    )
                },
                enabled = !isSubmitting
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
