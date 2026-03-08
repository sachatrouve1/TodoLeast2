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
        const val NOTIFICATION_ID_OVERDUE = 1001
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

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(overdueChannel)
        notificationManager.createNotificationChannel(upcomingChannel)
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
