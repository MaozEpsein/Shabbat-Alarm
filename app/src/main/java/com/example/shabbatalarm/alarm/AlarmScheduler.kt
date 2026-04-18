package com.example.shabbatalarm.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun canScheduleExactAlarms(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

    /**
     * Schedules a one-shot exact alarm at [hour]:[minute]. If the time has already passed
     * today, schedules for the same time tomorrow. Returns the absolute trigger timestamp
     * in millis (useful for display).
     */
    fun schedule(hour: Int, minute: Int): Long {
        val triggerAtMillis = computeNextTrigger(hour, minute)
        scheduleAt(triggerAtMillis)
        return triggerAtMillis
    }

    /** Re-arms the alarm at an absolute timestamp (used by [BootReceiver]). */
    fun scheduleAt(triggerAtMillis: Long) {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            buildPendingIntent()
        )
    }

    fun cancel() {
        alarmManager.cancel(buildPendingIntent())
    }

    private fun buildPendingIntent(): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_FIRE
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun computeNextTrigger(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!target.after(now)) {
            target.add(Calendar.DAY_OF_MONTH, 1)
        }
        return target.timeInMillis
    }

    companion object {
        const val ACTION_FIRE = "com.example.shabbatalarm.ACTION_FIRE"
        private const val REQUEST_CODE = 1001
    }
}
