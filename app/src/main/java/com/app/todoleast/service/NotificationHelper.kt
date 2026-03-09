package com.app.todoleast.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.app.todoleast.MainActivity
import com.app.todoleast.R
import com.app.todoleast.model.Task

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID_OVERDUE = "overdue_tasks"
        const val CHANNEL_ID_UPCOMING = "upcoming_tasks"
        const val CHANNEL_ID_URGENT = "urgent_tasks"
        const val NOTIFICATION_ID_OVERDUE = 1001
        const val NOTIFICATION_ID_UPCOMING = 1002
        const val NOTIFICATION_ID_SUMMARY = 1003
        const val NOTIFICATION_ID_DUE_SOON = 1004
        const val NOTIFICATION_ID_PERIODIC_MISSED = 1005
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val overdueChannel = NotificationChannel(
            CHANNEL_ID_OVERDUE,
            "Taches en retard",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications pour les taches en retard"
            enableVibration(true)
        }

        val upcomingChannel = NotificationChannel(
            CHANNEL_ID_UPCOMING,
            "Taches a venir",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications pour les taches arrivant a echeance"
        }

        val urgentChannel = NotificationChannel(
            CHANNEL_ID_URGENT,
            "Taches urgentes",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications pour les taches dans moins de 30 minutes"
            enableVibration(true)
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(overdueChannel)
        notificationManager.createNotificationChannel(upcomingChannel)
        notificationManager.createNotificationChannel(urgentChannel)
    }

    fun showOverdueNotification(overdueTasks: List<Task>) {
        if (overdueTasks.isEmpty()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (overdueTasks.size == 1) {
            "1 tache en retard"
        } else {
            "${overdueTasks.size} taches en retard"
        }

        val content = if (overdueTasks.size == 1) {
            overdueTasks.first().title
        } else {
            overdueTasks.take(3).joinToString(", ") { it.title } +
                    if (overdueTasks.size > 3) "..." else ""
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_OVERDUE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID_OVERDUE, notification)

        vibrate()
    }

    fun showUpcomingNotification(upcomingTasks: List<Task>) {
        if (upcomingTasks.isEmpty()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (upcomingTasks.size == 1) {
            "1 tache arrive a echeance"
        } else {
            "${upcomingTasks.size} taches arrivent a echeance"
        }

        val content = if (upcomingTasks.size == 1) {
            upcomingTasks.first().title
        } else {
            upcomingTasks.take(3).joinToString(", ") { it.title } +
                    if (upcomingTasks.size > 3) "..." else ""
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_UPCOMING)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID_UPCOMING, notification)
    }

    fun showDueSoonNotification(dueSoonTasks: List<Task>) {
        if (dueSoonTasks.isEmpty()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            3,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (dueSoonTasks.size == 1) {
            "Tache dans moins de 30 min!"
        } else {
            "${dueSoonTasks.size} taches dans moins de 30 min!"
        }

        val content = dueSoonTasks.first().title

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_URGENT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID_DUE_SOON, notification)
        vibrate()
    }

    fun showMissedPeriodicNotification(missedTasks: List<Task>) {
        if (missedTasks.isEmpty()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            4,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (missedTasks.size == 1) {
            "Tache recurrente non faite!"
        } else {
            "${missedTasks.size} taches recurrentes non faites!"
        }

        val content = missedTasks.first().title

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_OVERDUE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID_PERIODIC_MISSED, notification)
        vibrate()
    }

    fun showSummaryNotification(todoCount: Int, completedCount: Int, overdueCount: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            2,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val content = "A faire: $todoCount | Terminees: $completedCount | En retard: $overdueCount"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_UPCOMING)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Recap de vos taches")
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID_SUMMARY, notification)
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val effect = VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(effect)
    }
}
