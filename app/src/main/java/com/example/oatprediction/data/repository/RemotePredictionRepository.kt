package com.example.oatprediction.data.repository

import com.example.oatprediction.data.model.PredictionInput
import com.example.oatprediction.data.model.PredictionResult
import com.example.oatprediction.data.network.PredictionRemoteDataSource

/** Receives its data source explicitly; does not construct a network client. */
class RemotePredictionRepository(
    private val remoteDataSource: PredictionRemoteDataSource,
) : PredictionRepository {
    override suspend fun predict(input: PredictionInput): PredictionResult =
        remoteDataSource.predict(input)
}
