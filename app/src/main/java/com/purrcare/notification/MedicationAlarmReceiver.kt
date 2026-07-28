package com.purrcare.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.room.Room
import com.purrcare.data.database.PurrCareDatabase
import com.purrcare.data.entity.MedicationLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MedicationAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medicationId = intent.getLongExtra(NotificationHelper.EXTRA_MEDICATION_ID, 0)
        val catId = intent.getLongExtra(NotificationHelper.EXTRA_CAT_ID, 0)
        val medName = intent.getStringExtra(NotificationHelper.EXTRA_MED_NAME) ?: "Medication"
        val dosage = intent.getStringExtra("dosage") ?: ""

        when (intent.action) {
            "com.purrcare.action.ALARM_TRIGGER" -> {
                NotificationHelper.showMedicationReminder(
                    context, medicationId, catId, medName, dosage
                )
            }
            NotificationHelper.ACTION_MARK_TAKEN -> {
                NotificationHelper.cancelNotification(context, medicationId)
                recordMedicationTaken(context, medicationId)
            }
            NotificationHelper.ACTION_SNOOZE -> {
                NotificationHelper.cancelNotification(context, medicationId)
                AlarmScheduler.scheduleSnooze(context, medicationId, catId, medName, dosage)
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                rescheduleAllEnabledAlarms(context)
            }
        }
    }

    private fun recordMedicationTaken(context: Context, medicationId: Long) {
        val db = Room.databaseBuilder(
            context.applicationContext,
            PurrCareDatabase::class.java,
            "purrcare.db"
        ).fallbackToDestructiveMigration().build()

        CoroutineScope(Dispatchers.IO).launch {
            val log = MedicationLog(
                medicationId = medicationId,
                timestamp = System.currentTimeMillis(),
                wasAdministered = true
            )
            db.medicationDao().insertLog(log)
            db.close()
        }
    }

    private fun rescheduleAllEnabledAlarms(context: Context) {
        val db = Room.databaseBuilder(
            context.applicationContext,
            PurrCareDatabase::class.java,
            "purrcare.db"
        ).fallbackToDestructiveMigration().build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val cats = db.catDao().getAllCats().first()
                cats.forEach { cat ->
                    val enabledMeds = db.medicationDao().getEnabledMedicationsForCat(cat.id).first()
                    enabledMeds.forEach { med ->
                        AlarmScheduler.scheduleAlarm(
                            context,
                            med.id,
                            med.catId,
                            med.medName,
                            med.dosage,
                            med.alarmHour,
                            med.alarmMinute
                        )
                    }
                }
            } catch (_: Exception) { }
            db.close()
        }
    }
}
