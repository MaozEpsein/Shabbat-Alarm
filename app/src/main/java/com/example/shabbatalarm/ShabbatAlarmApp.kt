package com.example.shabbatalarm

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class ShabbatAlarmApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(NotificationManager::class.java)
        createAlarmChannel(nm)
        createReminderChannel(nm)
    }

    private fun createAlarmChannel(nm: NotificationManager) {
        val channel = NotificationChannel(
            ALARM_CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.notification_channel_description)
            setSound(null, null) // we play audio ourselves via MediaPlayer on the ALARM stream
            enableVibration(false)
        }
        nm.createNotificationChannel(channel)
    }

    private fun createReminderChannel(nm: NotificationManager) {
        val channel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = getString(R.string.reminder_channel_description)
        }
        nm.createNotificationChannel(channel)
    }

    companion object {
        const val ALARM_CHANNEL_ID = "shabbat_alarm_channel"
        const val REMINDER_CHANNEL_ID = "shabbat_reminder_channel"
    }
}
