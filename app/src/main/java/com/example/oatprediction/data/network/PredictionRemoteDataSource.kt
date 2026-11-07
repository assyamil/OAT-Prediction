package com.example.oatprediction.data.network

import com.example.oatprediction.data.model.PredictionInput
import com.example.oatprediction.data.model.PredictionResult

/**
 * Boundary implemented by RetrofitPredictionRemoteDataSource.
 * Failures propagate to the caller; cancellation is never converted to a result.
 */
interface PredictionRemoteDataSource {
    suspend fun predict(input: PredictionInput): PredictionResult
}
