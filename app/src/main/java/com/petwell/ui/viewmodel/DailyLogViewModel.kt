package com.petwell.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.petwell.PetWellApplication
import com.petwell.data.entity.DailyLog
import com.petwell.data.entity.enums.LitterUrination
import com.petwell.data.entity.enums.WaterIntake
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class DailyLogViewModel(application: Application) : AndroidViewModel(application) {

    private val dailyLogDao = (application as PetWellApplication).database.dailyLogDao()

    private var _petId: Long = 0

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
        val customNotes: String = ""
    )

    private val _formState = MutableStateFlow(FormState())
    val formState: StateFlow<FormState> = _formState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    fun initialize(petId: Long) { _petId = petId }

    fun onDateChanged(dateMillis: Long) {
        val normalized = Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        _formState.value = FormState(selectedDate = normalized)
    }

    fun updateWeight(value: String) { _formState.value = _formState.value.copy(weight = value) }
    fun updateAppetiteScore(score: Int) { _formState.value = _formState.value.copy(appetiteScore = score) }
    fun updateWaterIntake(intake: WaterIntake) { _formState.value = _formState.value.copy(waterIntake = intake) }
    fun updateLitterStoolScore(score: Int) { _formState.value = _formState.value.copy(litterStoolScore = score) }
    fun updateLitterUrination(urination: LitterUrination) { _formState.value = _formState.value.copy(litterUrination = urination) }
    fun updateCustomNotes(notes: String) { _formState.value = _formState.value.copy(customNotes = notes) }
    fun resetSaveState() { _saveState.value = SaveState.Idle }

    fun save() {
        val state = _formState.value
        val weightVal = state.weight.toFloatOrNull()
        if (weightVal == null || weightVal <= 0f) { _saveState.value = SaveState.Error("Please enter a valid weight."); return }
        if (state.appetiteScore !in 1..5) { _saveState.value = SaveState.Error("Please select an appetite score."); return }
        if (state.waterIntake == null) { _saveState.value = SaveState.Error("Please select water intake level."); return }
        if (state.litterUrination == null) { _saveState.value = SaveState.Error("Please select urination level."); return }

        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            try {
                val log = DailyLog(
                    petId = _petId,
                    timestamp = System.currentTimeMillis(),
                    weight = weightVal, appetiteScore = state.appetiteScore,
                    waterIntake = state.waterIntake, litterStoolScore = state.litterStoolScore,
                    litterUrination = state.litterUrination, customNotes = state.customNotes
                )
                dailyLogDao.insert(log)
                _formState.value = FormState(selectedDate = state.selectedDate)
                _saveState.value = SaveState.Saved
            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "Failed to save log.")
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
