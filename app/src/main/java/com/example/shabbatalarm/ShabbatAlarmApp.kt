package com.example.shabbatalarm

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager

class ShabbatAlarmApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createAlarmChannel()
    }

    private fun createAlarmChannel() {
        val channel = NotificationChannel(
            ALARM_CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.notification_channel_description)
            setSound(null, null) // we play audio ourselves via MediaPlayer on the ALARM stream
            enableVibration(false)
        }
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }

    companion object {
        const val ALARM_CHANNEL_ID = "shabbat_alarm_channel"
    }
}
