package com.petwell.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.petwell.PetWellApplication
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
                rescheduleRecurrence(context, reminderId)
            }
            NotificationHelper.ACTION_MARK_DONE -> {
                NotificationHelper.cancelNotification(context, reminderId)
                recordReminderDone(context, reminderId)
                rescheduleRecurrence(context, reminderId)
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

    private fun rescheduleRecurrence(context: Context, reminderId: Long) {
        val app = context.applicationContext as PetWellApplication
        val db = app.database

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminder = db.petReminderDao().getReminderByIdOnce(reminderId) ?: return@launch
                if (!reminder.isEnabled || reminder.frequencyHours <= 0) return@launch

                val nextTrigger = System.currentTimeMillis() + reminder.frequencyHours * 3_600_000L
                AlarmScheduler.scheduleAlarmAt(
                    context, reminder.id, reminder.petId,
                    reminder.title, reminder.dosage, nextTrigger
                )
            } catch (_: Exception) { }
        }
    }

    private fun recordReminderDone(context: Context, reminderId: Long) {
        val app = context.applicationContext as PetWellApplication
        val db = app.database

        CoroutineScope(Dispatchers.IO).launch {
            val log = PetReminderLog(
                reminderId = reminderId,
                timestamp = System.currentTimeMillis(),
                wasAdministered = true
            )
            db.petReminderDao().insertLog(log)
        }
    }

    private fun rescheduleAllEnabledAlarms(context: Context) {
        val app = context.applicationContext as PetWellApplication
        val db = app.database

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pets = db.petDao().getAllPets().first()
                pets.forEach { pet ->
                    val enabledReminders = db.petReminderDao().getEnabledRemindersForPet(pet.id).first()
                    enabledReminders.forEach { r ->
                        AlarmScheduler.scheduleAlarm(
                            context, r.id, r.petId, r.title, r.dosage,
                            r.alarmHour, r.alarmMinute, r.nextReminderDate
                        )
                    }
                }
            } catch (_: Exception) { }
        }
    }
}
