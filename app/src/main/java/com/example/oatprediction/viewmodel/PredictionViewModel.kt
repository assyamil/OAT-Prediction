package com.example.oatprediction.viewmodel

import androidx.lifecycle.ViewModel

/**
 * Lifecycle owner for future prediction state.
 * Submission, validation, and repository wiring belong to the form/state phase.
 */
class PredictionViewModel : ViewModel() {
    val uiState: PredictionUiState = PredictionUiState.Idle
}
