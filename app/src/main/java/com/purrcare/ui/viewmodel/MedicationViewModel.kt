package com.purrcare.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.purrcare.PurrCareApplication
import com.purrcare.data.entity.Medication
import com.purrcare.notification.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MedicationViewModel(application: Application) : AndroidViewModel(application) {

    private val medicationDao = (application as PurrCareApplication).database.medicationDao()

    private var _catId: Long = 0

    private val _medications = MutableStateFlow<List<Medication>>(emptyList())
    val medications: StateFlow<List<Medication>> = _medications.asStateFlow()

    private val _saveState = MutableStateFlow<MedSaveState>(MedSaveState.Idle)
    val saveState: StateFlow<MedSaveState> = _saveState.asStateFlow()

    fun initialize(catId: Long) {
        _catId = catId
        viewModelScope.launch {
            medicationDao.getMedicationsForCat(catId).collect { list ->
                _medications.value = list
            }
        }
    }

    fun addMedication(
        medName: String,
        dosage: String,
        frequencyHours: Int,
        alarmHour: Int,
        alarmMinute: Int,
        isEnabled: Boolean
    ) {
        viewModelScope.launch {
            _saveState.value = MedSaveState.Saving
            try {
                val med = Medication(
                    catId = _catId,
                    medName = medName,
                    dosage = dosage,
                    frequencyHours = frequencyHours,
                    alarmHour = alarmHour,
                    alarmMinute = alarmMinute,
                    isEnabled = isEnabled
                )
                val id = medicationDao.insert(med)
                if (isEnabled) {
                    scheduleAlarm(id, medName, dosage, alarmHour, alarmMinute)
                }
                _saveState.value = MedSaveState.Saved
            } catch (e: Exception) {
                _saveState.value = MedSaveState.Error(e.message ?: "Failed to save")
            }
        }
    }

    fun toggleEnabled(medication: Medication) {
        viewModelScope.launch {
            val updated = medication.copy(isEnabled = !medication.isEnabled)
            medicationDao.update(updated)
            if (updated.isEnabled) {
                scheduleAlarm(
                    updated.id, updated.medName, updated.dosage,
                    updated.alarmHour, updated.alarmMinute
                )
            } else {
                AlarmScheduler.cancelAlarm(getApplication(), updated.id)
            }
        }
    }

    fun deleteMedication(medication: Medication) {
        viewModelScope.launch {
            AlarmScheduler.cancelAlarm(getApplication(), medication.id)
            medicationDao.delete(medication)
        }
    }

    fun resetSaveState() {
        _saveState.value = MedSaveState.Idle
    }

    private fun scheduleAlarm(
        medicationId: Long,
        medName: String,
        dosage: String,
        alarmHour: Int,
        alarmMinute: Int
    ) {
        AlarmScheduler.scheduleAlarm(
            getApplication(),
            medicationId,
            _catId,
            medName,
            dosage,
            alarmHour,
            alarmMinute
        )
    }
}

sealed class MedSaveState {
    data object Idle : MedSaveState()
    data object Saving : MedSaveState()
    data object Saved : MedSaveState()
    data class Error(val message: String) : MedSaveState()
}
