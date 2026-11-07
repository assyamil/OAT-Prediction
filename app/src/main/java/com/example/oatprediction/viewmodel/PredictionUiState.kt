package com.example.oatprediction.viewmodel

import com.example.oatprediction.data.model.PredictionResult

sealed interface PredictionUiState {
    data object Idle : PredictionUiState
    data object Loading : PredictionUiState
    data class Success(val result: PredictionResult) : PredictionUiState
    data class Error(val reason: PredictionFailure, val httpCode: Int? = null) : PredictionUiState
}

enum class PredictionFailure { CONNECTION, TIMEOUT, HTTP, INVALID_RESPONSE, UNKNOWN }
