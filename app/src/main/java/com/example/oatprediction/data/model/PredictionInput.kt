package com.example.oatprediction.data.model

/**
 * In-memory application input, not a serialized API request.
 * Numeric domain rules and units still require research validation.
 */
data class PredictionInput(
    val puskesmas: String,
    val kategoriOat: String,
    val stokAwal: Double,
    val penerimaan: Double,
    val pemakaian: Double,
    val jumlahPasien: Double,
    // null means X7 is not established; it must never be converted to zero.
    // Its wire representation must be decided before Retrofit integration.
    val permintaanSebelumnya: Double? = null,
)
