package com.purrcare.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object AlarmScheduler {

    fun scheduleAlarm(
        context: Context,
        medicationId: Long,
        catId: Long,
        medName: String,
        dosage: String,
        alarmHour: Int,
        alarmMinute: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarmHour)
            set(Calendar.MINUTE, alarmMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            action = "com.purrcare.action.ALARM_TRIGGER"
            putExtra(NotificationHelper.EXTRA_MEDICATION_ID, medicationId)
            putExtra(NotificationHelper.EXTRA_CAT_ID, catId)
            putExtra(NotificationHelper.EXTRA_MED_NAME, medName)
            putExtra("dosage", dosage)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicationId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmClockInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
    }

    fun scheduleSnooze(
        context: Context,
        medicationId: Long,
        catId: Long,
        medName: String,
        dosage: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance().apply {
            add(Calendar.MINUTE, NotificationHelper.SNOOZE_MINUTES.toInt())
        }

        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            action = "com.purrcare.action.ALARM_TRIGGER"
            putExtra(NotificationHelper.EXTRA_MEDICATION_ID, medicationId)
            putExtra(NotificationHelper.EXTRA_CAT_ID, catId)
            putExtra(NotificationHelper.EXTRA_MED_NAME, medName)
            putExtra("dosage", dosage)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicationId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmClockInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis, pendingIntent)
        alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
    }

    fun cancelAlarm(context: Context, medicationId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MedicationAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicationId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun isAlarmScheduled(context: Context, medicationId: Long): Boolean {
        val intent = Intent(context, MedicationAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicationId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent != null
    }
}
