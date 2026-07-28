package com.purrcare.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.purrcare.PurrCareApplication
import com.purrcare.data.entity.DailyLog
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val dailyLogDao = (application as PurrCareApplication).database.dailyLogDao()

    private var _catId: Long = 0

    private val _recentLogs = kotlinx.coroutines.flow.MutableStateFlow<List<DailyLog>>(emptyList())
    val recentLogs: StateFlow<List<DailyLog>> = _recentLogs

    fun initialize(catId: Long) {
        _catId = catId
        viewModelScope.launch {
            dailyLogDao.getLatestLogs(catId, 5).collect { logs ->
                _recentLogs.value = logs
            }
        }
    }
}
