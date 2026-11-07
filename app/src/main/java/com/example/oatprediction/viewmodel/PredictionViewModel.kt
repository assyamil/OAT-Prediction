package com.example.oatprediction.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.oatprediction.data.model.PredictionInput
import com.example.oatprediction.data.model.HistoryRecord
import com.example.oatprediction.data.model.ResultSource
import com.example.oatprediction.data.repository.HistoryRepository
import java.util.UUID
import com.example.oatprediction.data.network.InvalidPredictionResponseException
import com.example.oatprediction.data.repository.PredictionRepository
import java.io.IOException
import java.io.InterruptedIOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

class PredictionViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: PredictionRepository,
    private val historyRepository: HistoryRepository? = null,
) : ViewModel() {
    var formState by mutableStateOf(
        PredictionFormState(
            recordingMonth = savedStateHandle.get<Int>("recordingMonth")
                ?: (java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1),
            recordingYear = savedStateHandle.get<String>("recordingYear")
                ?: java.util.Calendar.getInstance().get(java.util.Calendar.YEAR).toString(),
            values = PredictionField.entries.associateWith {
                savedStateHandle.get<String>(it.name).orEmpty()
            },
        ),
    )
        private set

    var uiState: PredictionUiState by mutableStateOf(PredictionUiState.Idle)
        private set
    var resultNavigationPending by mutableStateOf(false)
        private set
    private var predictionJob: Job? = null
    var saveState by mutableStateOf(HistorySaveState.IDLE)
        private set
    var resultSnapshot: HistoryRecord? = null
        private set

    init {
        savedStateHandle["recordingMonth"] = formState.recordingMonth
        savedStateHandle["recordingYear"] = formState.recordingYear
    }

    fun updatePeriod(month: Int, year: String) {
        if (uiState == PredictionUiState.Loading || saveState == HistorySaveState.SAVING) return
        savedStateHandle["recordingMonth"] = month
        savedStateHandle["recordingYear"] = year
        formState = formState.copy(recordingMonth = month, recordingYear = year,
            periodError = formState.validationAttempted && !validRecordingPeriod(month, year))
    }

    var formSessionId by mutableStateOf(0)
        private set

    private fun resetForm() {
        PredictionField.entries.forEach { savedStateHandle.remove<String>(it.name) }
        formState = PredictionFormState()
        savedStateHandle["recordingMonth"] = formState.recordingMonth
        savedStateHandle["recordingYear"] = formState.recordingYear
        formSessionId++
    }

    fun repeatPrediction() { leaveForm() }

    fun updateField(field: PredictionField, value: String) {
        if (uiState == PredictionUiState.Loading) return
        savedStateHandle[field.name] = value
        val values = formState.values + (field to value)
        formState = formState.copy(
            values = values,
            errors = if (formState.validationAttempted) {
                PredictionFormValidator.validate(values)
            } else {
                emptyMap()
            },
        )
    }

    fun validateForm(): Boolean {
        formState = formState.copy(
            errors = PredictionFormValidator.validate(formState.values),
            validationAttempted = true,
            periodError = !validRecordingPeriod(formState.recordingMonth, formState.recordingYear),
        )
        return formState.isValid
    }

    fun submit() {
        if (uiState == PredictionUiState.Loading || saveState == HistorySaveState.SAVING) return
        saveState = HistorySaveState.IDLE
        resultSnapshot = null
        resultNavigationPending = false
        if (!validateForm()) {
            uiState = PredictionUiState.Idle
            return
        }
        val input = PredictionInput(
            puskesmas = formState.value(PredictionField.PUSKESMAS),
            pemakaian = formState.value(PredictionField.PEMAKAIAN).toLong(),
            sisaStok = formState.value(PredictionField.SISA_STOK).toLong(),
            penerimaan = formState.value(PredictionField.PENERIMAAN).toLong(),
            kasusBaruTbc = formState.value(PredictionField.KASUS_BARU_TBC).toLong(),
        )
        val month = formState.recordingMonth
        val year = formState.recordingYear.toInt()
        uiState = PredictionUiState.Loading
        predictionJob = viewModelScope.launch {
            try {
                val result = repository.predict(input)
                ensureActive()
                resultSnapshot = HistoryRecord(UUID.randomUUID().toString(), System.currentTimeMillis(), input, result, result.source, month, year)
                uiState = PredictionUiState.Success(result)
                resultNavigationPending = true
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                ensureActive()
                uiState = when (error) {
                    is HttpException -> PredictionUiState.Error(PredictionFailure.HTTP, error.code())
                    is InvalidPredictionResponseException, is SerializationException ->
                        PredictionUiState.Error(PredictionFailure.INVALID_RESPONSE)
                    is InterruptedIOException -> PredictionUiState.Error(PredictionFailure.TIMEOUT)
                    is IOException -> PredictionUiState.Error(PredictionFailure.CONNECTION)
                    else -> PredictionUiState.Error(PredictionFailure.UNKNOWN)
                }
            }
        }
    }

    fun onResultNavigated() {
        resultNavigationPending = false
    }

    fun leaveForm() {
        if (saveState == HistorySaveState.SAVING) return
        clearPrediction()
        resetForm()
    }

    private fun clearPrediction() {
        predictionJob?.cancel()
        predictionJob = null
        resultNavigationPending = false
        uiState = PredictionUiState.Idle
        resultSnapshot = null
        saveState = HistorySaveState.IDLE
    }

    fun saveResult() {
        val snapshot = resultSnapshot ?: return
        if (saveState == HistorySaveState.SAVING || saveState == HistorySaveState.SAVED) return
        saveState = HistorySaveState.SAVING
        viewModelScope.launch {
            try {
                checkNotNull(historyRepository).save(snapshot)
                saveState = HistorySaveState.SAVED
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                saveState = HistorySaveState.ERROR
            }
        }
    }

    companion object {
        fun factory(repository: PredictionRepository, history: HistoryRepository) = viewModelFactory {
            initializer { PredictionViewModel(createSavedStateHandle(), repository, history) }
        }
    }
}

enum class HistorySaveState { IDLE, SAVING, SAVED, ERROR }
