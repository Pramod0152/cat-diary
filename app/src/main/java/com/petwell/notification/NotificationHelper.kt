package com.petwell.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.petwell.MainActivity

object NotificationHelper {

    const val CHANNEL_ID = "pet_reminder"
    const val CHANNEL_NAME = "Pet Reminders"

    const val ACTION_MARK_DONE = "com.petwell.action.MARK_DONE"
    const val ACTION_SNOOZE = "com.petwell.action.SNOOZE"

    const val EXTRA_REMINDER_ID = "reminder_id"
    const val EXTRA_TITLE = "title"
    const val EXTRA_PET_ID = "pet_id"
    const val EXTRA_NOTIFICATION_ID = "notification_id"

    const val SNOOZE_MINUTES = 15L

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for pet care"
                enableVibration(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun showReminderNotification(
        context: Context,
        reminderId: Long,
        petId: Long,
        title: String,
        dosage: String
    ) {
        val notificationId = reminderId.toInt()

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context, notificationId, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val markDoneIntent = Intent(context, PetReminderAlarmReceiver::class.java).apply {
            action = ACTION_MARK_DONE
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_PET_ID, petId)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val markDonePendingIntent = PendingIntent.getBroadcast(
            context, notificationId + 1000, markDoneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, PetReminderAlarmReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_PET_ID, petId)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context, notificationId + 2000, snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(dosage)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_input_add, "Mark as Done", markDonePendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Snooze ($SNOOZE_MINUTES min)", snoozePendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    fun cancelNotification(context: Context, reminderId: Long) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(reminderId.toInt())
    }
}
