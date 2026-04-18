package com.example.shabbatalarm.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

/**
 * Schedules / cancels individual alarms via AlarmManager. Each alarm uses its
 * unique id as the PendingIntent request code, so multiple alarms can coexist
 * without overriding each other.
 */
class AlarmScheduler(private val context: Context) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun canScheduleExactAlarms(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else true

    /**
     * Schedules the given alarm. Returns the alarm object (useful if the caller
     * needs to persist the trigger time elsewhere).
     */
    fun schedule(alarm: ScheduledAlarm) {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alarm.triggerMillis,
            buildPendingIntent(alarm.id)
        )
    }

    /** Cancels the AlarmManager entry for the given alarm id. */
    fun cancel(alarmId: Int) {
        alarmManager.cancel(buildPendingIntent(alarmId))
    }

    /**
     * Computes the next trigger timestamp for the given hour/minute: today if
     * still in the future, otherwise the same time tomorrow.
     */
    fun computeNextTrigger(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!target.after(now)) target.add(Calendar.DAY_OF_MONTH, 1)
        return target.timeInMillis
    }

    private fun buildPendingIntent(alarmId: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_FIRE
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        return PendingIntent.getBroadcast(
            context,
            alarmId, // request code per alarm
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val ACTION_FIRE = "com.example.shabbatalarm.ACTION_FIRE"
        const val EXTRA_ALARM_ID = "alarm_id"
    }
}
