package com.petwell.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.petwell.PetWellApplication
import com.petwell.data.entity.DailyLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val dailyLogDao = (application as PetWellApplication).database.dailyLogDao()

    private val _recentLogs = MutableStateFlow<List<DailyLog>>(emptyList())
    val recentLogs: StateFlow<List<DailyLog>> = _recentLogs

    fun initialize(petId: Long) {
        viewModelScope.launch {
            dailyLogDao.getLatestLogs(petId, 5).collect { _recentLogs.value = it }
        }
    }
}
