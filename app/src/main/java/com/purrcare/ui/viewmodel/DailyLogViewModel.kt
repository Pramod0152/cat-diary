package com.purrcare.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.purrcare.PurrCareApplication
import com.purrcare.data.entity.DailyLog
import com.purrcare.data.entity.enums.LitterUrination
import com.purrcare.data.entity.enums.WaterIntake
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class DailyLogViewModel(application: Application) : AndroidViewModel(application) {

    private val dailyLogDao = (application as PurrCareApplication).database.dailyLogDao()

    private var _catId: Long = 0

    data class FormState(
        val selectedDate: Long = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis,
        val weight: String = "",
        val appetiteScore: Int = 0,
        val waterIntake: WaterIntake? = null,
        val litterStoolScore: Int = 4,
        val litterUrination: LitterUrination? = null,
        val customNotes: String = "",
        val existingLogId: Long = 0,
        val isEditing: Boolean = false
    )

    private val _formState = MutableStateFlow(FormState())
    val formState: StateFlow<FormState> = _formState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    fun initialize(catId: Long) {
        _catId = catId
        loadLogForDate(_formState.value.selectedDate)
    }

    fun onDateChanged(dateMillis: Long) {
        val normalized = Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        _formState.value = _formState.value.copy(selectedDate = normalized)
        loadLogForDate(normalized)
    }

    fun updateWeight(value: String) {
        _formState.value = _formState.value.copy(weight = value)
    }

    fun updateAppetiteScore(score: Int) {
        _formState.value = _formState.value.copy(appetiteScore = score)
    }

    fun updateWaterIntake(intake: WaterIntake) {
        _formState.value = _formState.value.copy(waterIntake = intake)
    }

    fun updateLitterStoolScore(score: Int) {
        _formState.value = _formState.value.copy(litterStoolScore = score)
    }

    fun updateLitterUrination(urination: LitterUrination) {
        _formState.value = _formState.value.copy(litterUrination = urination)
    }

    fun updateCustomNotes(notes: String) {
        _formState.value = _formState.value.copy(customNotes = notes)
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }

    fun save() {
        val state = _formState.value

        val weightVal = state.weight.toFloatOrNull()
        if (weightVal == null || weightVal <= 0f) {
            _saveState.value = SaveState.Error("Please enter a valid weight.")
            return
        }
        if (state.appetiteScore !in 1..5) {
            _saveState.value = SaveState.Error("Please select an appetite score.")
            return
        }
        if (state.waterIntake == null) {
            _saveState.value = SaveState.Error("Please select water intake level.")
            return
        }
        if (state.litterUrination == null) {
            _saveState.value = SaveState.Error("Please select urination level.")
            return
        }

        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            try {
                val now = System.currentTimeMillis()
                val log = DailyLog(
                    id = state.existingLogId,
                    catId = _catId,
                    timestamp = if (state.isEditing) state.selectedDate else now,
                    weight = weightVal,
                    appetiteScore = state.appetiteScore,
                    waterIntake = state.waterIntake,
                    litterStoolScore = state.litterStoolScore,
                    litterUrination = state.litterUrination,
                    customNotes = state.customNotes
                )

                if (state.isEditing) {
                    dailyLogDao.update(log)
                } else {
                    dailyLogDao.insert(log)
                }

                _saveState.value = SaveState.Saved
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "Failed to save log.")
            }
        }
    }

    private fun loadLogForDate(dateMillis: Long) {
        viewModelScope.launch {
            val dayStart = dateMillis
            val dayEnd = dayStart + 86_399_999L
            val existingLog = dailyLogDao.getLogForCatOnDay(_catId, dayStart, dayEnd)

            val state = _formState.value
            if (existingLog != null) {
                _formState.value = FormState(
                    selectedDate = dateMillis,
                    weight = existingLog.weight.toString(),
                    appetiteScore = existingLog.appetiteScore,
                    waterIntake = existingLog.waterIntake,
                    litterStoolScore = existingLog.litterStoolScore,
                    litterUrination = existingLog.litterUrination,
                    customNotes = existingLog.customNotes,
                    existingLogId = existingLog.id,
                    isEditing = true
                )
            } else {
                _formState.value = _formState.value.copy(
                    selectedDate = dateMillis,
                    existingLogId = 0,
                    isEditing = false
                )
            }
        }
    }
}

sealed class SaveState {
    data object Idle : SaveState()
    data object Saving : SaveState()
    data object Saved : SaveState()
    data class Error(val message: String) : SaveState()
}
