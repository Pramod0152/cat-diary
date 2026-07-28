package com.purrcare.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.purrcare.PurrCareApplication
import com.purrcare.data.entity.CatProfile
import com.purrcare.data.entity.DailyLog
import com.purrcare.data.entity.Medication
import com.purrcare.data.entity.MedicationLog
import com.purrcare.report.VetReportPdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar

class ReportViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as PurrCareApplication).database
    private val generator = VetReportPdfGenerator(application)

    sealed class ReportState {
        data object Idle : ReportState()
        data object Generating : ReportState()
        data class Ready(val file: File, val shareIntent: Intent) : ReportState()
        data class Error(val message: String) : ReportState()
    }

    private val _state = MutableStateFlow<ReportState>(ReportState.Idle)
    val state: StateFlow<ReportState> = _state.asStateFlow()

    fun generateReport(catProfile: CatProfile, days: Int) {
        viewModelScope.launch {
            _state.value = ReportState.Generating
            try {
                val endDate = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.timeInMillis

                val startDate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -days)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val dailyLogs = withContext(Dispatchers.IO) {
                    db.dailyLogDao().getLogsForCatInRange(catProfile.id, startDate, endDate).first()
                }

                val medications = withContext(Dispatchers.IO) {
                    db.medicationDao().getMedicationsForCat(catProfile.id).first()
                }

                val medicationLogs = withContext(Dispatchers.IO) {
                    db.medicationDao().getLogsForCatInRange(catProfile.id, startDate, endDate)
                }

                val file = withContext(Dispatchers.IO) {
                    generator.generate(
                        catProfile, dailyLogs, medications, medicationLogs,
                        startDate, endDate
                    )
                }

                val uri: Uri = FileProvider.getUriForFile(
                    getApplication(),
                    "${getApplication<Application>().packageName}.fileprovider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "PurrCare Report - ${catProfile.name}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                _state.value = ReportState.Ready(file, shareIntent)
            } catch (e: Exception) {
                _state.value = ReportState.Error(e.message ?: "Failed to generate report")
            }
        }
    }

    fun resetState() {
        _state.value = ReportState.Idle
    }
}
