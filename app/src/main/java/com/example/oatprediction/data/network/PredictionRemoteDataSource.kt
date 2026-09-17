package com.example.oatprediction.data.network

import com.example.oatprediction.data.model.PredictionInput
import com.example.oatprediction.data.model.PredictionResult

/**
 * Boundary for a future Retrofit adapter to POST /predict.
 * The adapter will map JSON/HTTP errors and Rendah/Sedang/Tinggi to app models.
 * No HTTP client, endpoint configuration, or dummy result is created here.
 */
interface PredictionRemoteDataSource {
    suspend fun predict(input: PredictionInput): PredictionResult
}
