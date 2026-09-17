package com.example.oatprediction.data.repository

import com.example.oatprediction.data.model.PredictionInput
import com.example.oatprediction.data.model.PredictionResult

/** Application-facing access to prediction data. */
interface PredictionRepository {
    suspend fun predict(input: PredictionInput): PredictionResult
}
