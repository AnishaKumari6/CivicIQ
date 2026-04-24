package com.civiciq.app.data.local

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.civiciq.app.MainActivity
import java.util.concurrent.TimeUnit

class DailyReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "civiciq_daily_reminder"
        const val CHANNEL_NAME = "Daily Study Reminder"
        const val NOTIFICATION_ID = 1001
        const val WORK_NAME = "civiciq_daily_reminder_work"

        fun schedule(context: Context, hourOfDay: Int = 9) {
            val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            val currentMinute = java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE)

            val minutesUntilTarget = run {
                val targetMinutes = hourOfDay * 60
                val currentMinutes = currentHour * 60 + currentMinute
                val diff = targetMinutes - currentMinutes
                if (diff <= 0) diff + 24 * 60 else diff
            }

            val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(
                repeatInterval = 24,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setInitialDelay(minutesUntilTarget.toLong(), TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Daily reminders to study civic topics"
                    enableVibration(true)
                }
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    private val reminderMessages = listOf(
        "📚 Time to brush up on the Constitution!",
        "⚖️ Do you know your fundamental rights today?",
        "🏛 A quick quiz to keep your civic knowledge sharp!",
        "🇮🇳 Strengthen your understanding of Indian democracy!",
        "💡 5 minutes of civic learning can change how you see the world."
    )

    override suspend fun doWork(): Result {
        createNotificationChannel(context)
        showNotification()
        return Result.success()
    }

    private fun showNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val message = reminderMessages.random()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("CivicIQ Daily Reminder 🏛")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
