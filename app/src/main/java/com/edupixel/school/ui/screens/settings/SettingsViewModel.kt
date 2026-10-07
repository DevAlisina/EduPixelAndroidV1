package com.edupixel.school.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edupixel.school.core.config.ApiConfig
import com.edupixel.school.core.network.NetworkResult
import com.edupixel.school.data.remote.models.HealthResponse
import com.edupixel.school.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HealthUiState {
    data object Idle : HealthUiState
    data object Checking : HealthUiState
    data class Success(val health: HealthResponse) : HealthUiState
    data class Error(val message: String, val isNetworkError: Boolean) : HealthUiState
}

class SettingsViewModel(
    private val reportRepository: ReportRepository = ReportRepository()
) : ViewModel() {

    private val _currentBaseUrl = MutableStateFlow(ApiConfig.baseUrl)
    val currentBaseUrl: StateFlow<String> = _currentBaseUrl.asStateFlow()

    private val _healthState = MutableStateFlow<HealthUiState>(HealthUiState.Idle)
    val healthState: StateFlow<HealthUiState> = _healthState.asStateFlow()

    init {
        checkHealth()
    }

    fun checkHealth() {
        viewModelScope.launch {
            _healthState.value = HealthUiState.Checking
            when (val res = reportRepository.getHealth()) {
                is NetworkResult.Success -> {
                    _healthState.value = HealthUiState.Success(res.data)
                }
                is NetworkResult.Error -> {
                    _healthState.value = HealthUiState.Error(res.message, res.isNetworkError)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun updateBaseUrl(newUrl: String) {
        ApiConfig.baseUrl = newUrl
        _currentBaseUrl.value = ApiConfig.baseUrl
        checkHealth()
    }

    fun resetToDefault() {
        ApiConfig.resetToDefault()
        _currentBaseUrl.value = ApiConfig.baseUrl
        checkHealth()
    }
}
