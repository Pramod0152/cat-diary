package com.purrcare.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.purrcare.MainActivity

object NotificationHelper {

    const val CHANNEL_ID = "medication_reminder"
    const val CHANNEL_NAME = "Medication Reminders"

    const val ACTION_MARK_TAKEN = "com.purrcare.action.MARK_TAKEN"
    const val ACTION_SNOOZE = "com.purrcare.action.SNOOZE"

    const val EXTRA_MEDICATION_ID = "medication_id"
    const val EXTRA_MED_NAME = "med_name"
    const val EXTRA_CAT_ID = "cat_id"
    const val EXTRA_NOTIFICATION_ID = "notification_id"

    const val SNOOZE_MINUTES = 15L

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for cat medication doses"
                enableVibration(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun showMedicationReminder(
        context: Context,
        medicationId: Long,
        catId: Long,
        medName: String,
        dosage: String
    ) {
        val notificationId = medicationId.toInt()

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context, notificationId, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val markTakenIntent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            action = ACTION_MARK_TAKEN
            putExtra(EXTRA_MEDICATION_ID, medicationId)
            putExtra(EXTRA_CAT_ID, catId)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val markTakenPendingIntent = PendingIntent.getBroadcast(
            context, notificationId + 1000, markTakenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_MEDICATION_ID, medicationId)
            putExtra(EXTRA_CAT_ID, catId)
            putExtra(EXTRA_MED_NAME, medName)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context, notificationId + 2000, snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Medication Reminder")
            .setContentText("$medName - $dosage")
            .setSubText("Time for ${medName}'s dose")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_input_add, "Mark as Taken", markTakenPendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Snooze ($SNOOZE_MINUTES min)", snoozePendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    fun cancelNotification(context: Context, medicationId: Long) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(medicationId.toInt())
    }
}
