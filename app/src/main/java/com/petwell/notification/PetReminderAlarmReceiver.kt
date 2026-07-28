package com.petwell.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.room.Room
import com.petwell.data.database.PetWellDatabase
import com.petwell.data.entity.PetReminderLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PetReminderAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, 0)
        val petId = intent.getLongExtra(NotificationHelper.EXTRA_PET_ID, 0)
        val title = intent.getStringExtra(NotificationHelper.EXTRA_TITLE) ?: "Reminder"
        val dosage = intent.getStringExtra("dosage") ?: ""

        when (intent.action) {
            "com.petwell.action.ALARM_TRIGGER" -> {
                NotificationHelper.showReminderNotification(
                    context, reminderId, petId, title, dosage
                )
            }
            NotificationHelper.ACTION_MARK_DONE -> {
                NotificationHelper.cancelNotification(context, reminderId)
                recordReminderDone(context, reminderId)
            }
            NotificationHelper.ACTION_SNOOZE -> {
                NotificationHelper.cancelNotification(context, reminderId)
                AlarmScheduler.scheduleSnooze(context, reminderId, petId, title, dosage)
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                rescheduleAllEnabledAlarms(context)
            }
        }
    }

    private fun recordReminderDone(context: Context, reminderId: Long) {
        val db = Room.databaseBuilder(
            context.applicationContext,
            PetWellDatabase::class.java,
            "petwell.db"
        ).fallbackToDestructiveMigration().build()

        CoroutineScope(Dispatchers.IO).launch {
            val log = PetReminderLog(
                reminderId = reminderId,
                timestamp = System.currentTimeMillis(),
                wasAdministered = true
            )
            db.petReminderDao().insertLog(log)
            db.close()
        }
    }

    private fun rescheduleAllEnabledAlarms(context: Context) {
        val db = Room.databaseBuilder(
            context.applicationContext,
            PetWellDatabase::class.java,
            "petwell.db"
        ).fallbackToDestructiveMigration().build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pets = db.petDao().getAllPets().first()
                pets.forEach { pet ->
                    val enabledReminders = db.petReminderDao().getEnabledRemindersForPet(pet.id).first()
                    enabledReminders.forEach { r ->
                        AlarmScheduler.scheduleAlarm(
                            context, r.id, r.petId, r.title, r.dosage,
                            r.alarmHour, r.alarmMinute
                        )
                    }
                }
            } catch (_: Exception) { }
            db.close()
        }
    }
}
