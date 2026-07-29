package com.petwell.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import java.util.Calendar

object AlarmScheduler {

    /**
     * Recurrence contract:
     *
     * - Initial scheduling: uses alarmHour:alarmMinute to compute the next occurrence
     *   (next calendar slot for that clock time). If the reminder has a non-null
     *   [nextReminderDate] that is in the future, that date takes precedence for the
     *   FIRST scheduled alarm.
     * - After the alarm fires (ALARM_TRIGGER), [PetReminderAlarmReceiver] looks up the
     *   reminder from the database. If frequencyHours > 0 and isEnabled == true, it
     *   calls [scheduleAlarmAt] with triggerAtMillis = now + frequencyHours, ensuring
     *   the reminder recurs indefinitely at the defined interval.
     * - After Mark Done, the same recurrence logic applies, computing the next fire
     *   time from the mark-done timestamp, not from the original clock time.
     * - After a snoozed alarm fires (which is also an ALARM_TRIGGER), the normal
     *   recurrence resumption kicks in, so the reminder does not stay snoozed forever.
     * - After device reboot, [scheduleAlarm] is used with the original alarmHour:alarmMinute
     *   so the next occurrence is relative to the clock time (not the pre-boot schedule).
     */

    fun scheduleAlarm(
        context: Context,
        reminderId: Long,
        petId: Long,
        title: String,
        dosage: String,
        alarmHour: Int,
        alarmMinute: Int,
        nextReminderDate: Long? = null
    ) {
        val triggerAtMillis = if (nextReminderDate != null && nextReminderDate > System.currentTimeMillis()) {
            nextReminderDate
        } else {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, alarmHour)
                set(Calendar.MINUTE, alarmMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            calendar.timeInMillis
        }
        scheduleAt(context, reminderId, petId, title, dosage, triggerAtMillis)
    }

    fun scheduleAlarmAt(
        context: Context,
        reminderId: Long,
        petId: Long,
        title: String,
        dosage: String,
        triggerAtMillis: Long
    ) {
        scheduleAt(context, reminderId, petId, title, dosage, triggerAtMillis)
    }

    fun scheduleSnooze(
        context: Context,
        reminderId: Long,
        petId: Long,
        title: String,
        dosage: String
    ) {
        if (!canSchedule(context)) return

        val triggerAtMillis = System.currentTimeMillis() + NotificationHelper.SNOOZE_MINUTES * 60_000L
        scheduleAt(context, reminderId, petId, title, dosage, triggerAtMillis)
    }

    private fun scheduleAt(
        context: Context,
        reminderId: Long,
        petId: Long,
        title: String,
        dosage: String,
        triggerAtMillis: Long
    ) {
        if (!canSchedule(context)) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, PetReminderAlarmReceiver::class.java).apply {
            action = "com.petwell.action.ALARM_TRIGGER"
            putExtra(NotificationHelper.EXTRA_REMINDER_ID, reminderId)
            putExtra(NotificationHelper.EXTRA_PET_ID, petId)
            putExtra(NotificationHelper.EXTRA_TITLE, title)
            putExtra("dosage", dosage)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    fun cancelAlarm(context: Context, reminderId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PetReminderAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun isAlarmScheduled(context: Context, reminderId: Long): Boolean {
        val intent = Intent(context, PetReminderAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent != null
    }

    fun canSchedule(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            return alarmManager.canScheduleExactAlarms()
        }
        return true
    }

    fun getExactAlarmSettingsIntent(context: Context): Intent {
        return Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = android.net.Uri.parse("package:${context.packageName}")
        }
    }
}
