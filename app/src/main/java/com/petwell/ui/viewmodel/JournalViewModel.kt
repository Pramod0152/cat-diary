package com.petwell.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.petwell.PetWellApplication
import com.petwell.data.entity.JournalEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class JournalViewModel(application: Application) : AndroidViewModel(application) {

    private val journalDao = (application as PetWellApplication).database.journalDao()

    private var _petId: Long = 0

    private val _entries = MutableStateFlow<List<JournalEntry>>(emptyList())
    val entries: StateFlow<List<JournalEntry>> = _entries.asStateFlow()

    private val _operationState = MutableStateFlow<JournalOpState>(JournalOpState.Idle)
    val operationState: StateFlow<JournalOpState> = _operationState.asStateFlow()

    fun initialize(petId: Long) {
        _petId = petId
        viewModelScope.launch {
            journalDao.getEntriesForPet(petId).collect { _entries.value = it }
        }
    }

    fun addEntry(title: String, content: String) {
        viewModelScope.launch {
            try {
                val entry = JournalEntry(
                    petId = _petId,
                    date = System.currentTimeMillis(),
                    title = title.trim(),
                    content = content.trim()
                )
                journalDao.insert(entry)
                _operationState.value = JournalOpState.Success("Entry saved")
            } catch (e: Exception) {
                _operationState.value = JournalOpState.Error(e.message ?: "Failed to save")
            }
        }
    }

    fun updateEntry(entry: JournalEntry) {
        viewModelScope.launch {
            try {
                journalDao.update(entry)
                _operationState.value = JournalOpState.Success("Entry updated")
            } catch (e: Exception) {
                _operationState.value = JournalOpState.Error(e.message ?: "Failed to update")
            }
        }
    }

    fun deleteEntry(entry: JournalEntry) {
        viewModelScope.launch {
            try {
                journalDao.delete(entry)
                _operationState.value = JournalOpState.Success("Entry deleted")
            } catch (e: Exception) {
                _operationState.value = JournalOpState.Error(e.message ?: "Failed to delete")
            }
        }
    }

    fun resetOperationState() { _operationState.value = JournalOpState.Idle }
}

sealed class JournalOpState {
    data object Idle : JournalOpState()
    data class Success(val message: String) : JournalOpState()
    data class Error(val message: String) : JournalOpState()
}
