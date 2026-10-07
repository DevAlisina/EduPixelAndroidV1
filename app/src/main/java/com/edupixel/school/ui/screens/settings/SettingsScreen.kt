package com.edupixel.school.ui.screens.settings

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.edupixel.school.core.config.ApiConfig
import com.edupixel.school.ui.components.GlassTopAppBar
import com.edupixel.school.ui.theme.GlassCard
import com.edupixel.school.ui.theme.GlassScreenContainer

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val currentBaseUrl by viewModel.currentBaseUrl.collectAsState()
    val healthState by viewModel.healthState.collectAsState()

    var inputUrl by remember(currentBaseUrl) { mutableStateOf(currentBaseUrl) }

    GlassScreenContainer(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            GlassTopAppBar(
                title = "Settings",
                subtitle = "API Connection & Configuration"
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Connection Health Card
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val (iconColor, icon) = when (healthState) {
                                        is HealthUiState.Success -> Pair(Color(0xFF10B981), Icons.Outlined.CheckCircle)
                                        is HealthUiState.Error -> Pair(Color(0xFFEF4444), Icons.Outlined.ErrorOutline)
                                        is HealthUiState.Checking -> Pair(Color(0xFF38BDF8), Icons.Outlined.Sync)
                                        is HealthUiState.Idle -> Pair(Color(0xFF94A3B8), Icons.Outlined.CloudQueue)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(iconColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(icon, contentDescription = null, tint = iconColor)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Backend Status",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = when (healthState) {
                                                is HealthUiState.Success -> "Online & Responding (/health OK)"
                                                is HealthUiState.Error -> "Offline / Cannot Connect"
                                                is HealthUiState.Checking -> "Probing connection..."
                                                is HealthUiState.Idle -> "Ready"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Button(
                                    onClick = { viewModel.checkHealth() },
                                    enabled = healthState !is HealthUiState.Checking
                                ) {
                                    Text("Test")
                                }
                            }

                            when (val state = healthState) {
                                is HealthUiState.Success -> {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Service: ${state.health.service}", style = MaterialTheme.typography.bodySmall)
                                    Text("Status: ${state.health.status}", style = MaterialTheme.typography.bodySmall)
                                    Text("Excel Runtime Dependency: ${state.health.excelRuntimeDependency ?: "None"}", style = MaterialTheme.typography.bodySmall)
                                }
                                is HealthUiState.Error -> {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = state.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                else -> {}
                            }
                        }
                    }
                }

                // API Base URL Configuration Card
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(
                                text = "API BASE URL CONFIGURATION",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = "The application connects directly to the FastAPI server. During local development, the default is http://127.0.0.1:7878/.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = inputUrl,
                                onValueChange = { inputUrl = it },
                                label = { Text("API Server Base URL") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.updateBaseUrl(inputUrl.trim()) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Apply & Save")
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.resetToDefault()
                                        inputUrl = ApiConfig.DEFAULT_API_BASE_URL
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Reset Default")
                                }
                            }
                        }
                    }
                }

                // Architecture and Environment Info Card
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "SYSTEM INFORMATION",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("Application: EduPixel School Management Client v1.0", style = MaterialTheme.typography.bodySmall)
                            Text("Backend API: EduPixel School API v0.3.0", style = MaterialTheme.typography.bodySmall)
                            Text("Default Port: 7878", style = MaterialTheme.typography.bodySmall)
                            Text("Cleartext HTTP: Permitted for local development", style = MaterialTheme.typography.bodySmall)
                            Text("Engine: Excel-Independent Normalized Rules Engine", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
