package com.example.oatprediction.data.model

/** The five approved predictors sent to the MLR and CART pipelines. */
data class PredictionInput(
    val puskesmas: String,
    val pemakaian: Long,
    val sisaStok: Long,
    val penerimaan: Long,
    val kasusBaruTbc: Long,
)
