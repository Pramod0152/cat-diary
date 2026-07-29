package com.petwell.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.petwell.PetWellApplication
import com.petwell.data.entity.PetReminder
import com.petwell.data.entity.enums.ReminderType
import com.petwell.notification.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PetReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val reminderDao = (application as PetWellApplication).database.petReminderDao()
    private var _petId: Long = 0

    private val _reminders = MutableStateFlow<List<PetReminder>>(emptyList())
    val reminders: StateFlow<List<PetReminder>> = _reminders.asStateFlow()

    private val _saveState = MutableStateFlow<RemSaveState>(RemSaveState.Idle)
    val saveState: StateFlow<RemSaveState> = _saveState.asStateFlow()

    fun initialize(petId: Long) {
        _petId = petId
        viewModelScope.launch {
            reminderDao.getRemindersForPet(petId).collect { _reminders.value = it }
        }
    }

    fun addReminder(
        title: String, dosage: String, reminderType: ReminderType,
        frequencyHours: Int, alarmHour: Int, alarmMinute: Int, isEnabled: Boolean,
        nextReminderDate: Long?
    ) {
        viewModelScope.launch {
            _saveState.value = RemSaveState.Saving
            try {
                val rem = PetReminder(
                    petId = _petId, title = title, dosage = dosage,
                    reminderType = reminderType, frequencyHours = frequencyHours,
                    alarmHour = alarmHour, alarmMinute = alarmMinute, isEnabled = isEnabled,
                    nextReminderDate = nextReminderDate
                )
                val id = reminderDao.insert(rem)
                if (isEnabled && !scheduleAlarm(id, title, dosage, alarmHour, alarmMinute, nextReminderDate)) {
                    _saveState.value = RemSaveState.Error("Saved but enable exact alarms in Settings for reminders.")
                    return@launch
                }
                _saveState.value = RemSaveState.Saved
            } catch (e: Exception) {
                _saveState.value = RemSaveState.Error(e.message ?: "Failed to save")
            }
        }
    }

    fun toggleEnabled(reminder: PetReminder) {
        viewModelScope.launch {
            val updated = reminder.copy(isEnabled = !reminder.isEnabled)
            reminderDao.update(updated)
            if (updated.isEnabled) {
                if (!scheduleAlarm(updated.id, updated.title, updated.dosage, updated.alarmHour, updated.alarmMinute, updated.nextReminderDate)) {
                    reminderDao.update(reminder)
                    _saveState.value = RemSaveState.Error("Enable exact alarms in Settings to turn on reminders.")
                    return@launch
                }
            } else {
                AlarmScheduler.cancelAlarm(getApplication(), updated.id)
            }
        }
    }

    fun deleteReminder(reminder: PetReminder) {
        viewModelScope.launch {
            AlarmScheduler.cancelAlarm(getApplication(), reminder.id)
            reminderDao.delete(reminder)
        }
    }

    fun resetSaveState() { _saveState.value = RemSaveState.Idle }

    private fun scheduleAlarm(id: Long, title: String, dosage: String, h: Int, m: Int, nextDate: Long?): Boolean {
        if (!AlarmScheduler.canSchedule(getApplication())) return false
        AlarmScheduler.scheduleAlarm(getApplication(), id, _petId, title, dosage, h, m, nextDate)
        return true
    }
}

sealed class RemSaveState {
    data object Idle : RemSaveState()
    data object Saving : RemSaveState()
    data object Saved : RemSaveState()
    data class Error(val message: String) : RemSaveState()
}
