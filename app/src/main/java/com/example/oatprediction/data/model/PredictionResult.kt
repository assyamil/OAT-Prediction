package com.example.oatprediction.data.model

/** Independent outputs supplied by the backend, never computed by Android. */
data class PredictionResult(
    val prediksiMlr: Double,
    val klasifikasiDt: NeedLevel,
)

enum class NeedLevel {
    LOW,
    MEDIUM,
    HIGH,
}
