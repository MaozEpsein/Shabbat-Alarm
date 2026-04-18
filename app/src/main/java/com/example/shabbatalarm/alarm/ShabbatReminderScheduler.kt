package com.example.shabbatalarm.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Schedules a weekly reminder that fires 30 minutes before candle lighting in
 * the user-chosen default city. When the reminder fires (see
 * [ShabbatReminderReceiver]) it also re-arms itself for the next week.
 */
class ShabbatReminderScheduler(private val context: Context) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Arms the next reminder if the feature is enabled. Safe to call anytime —
     * becomes a no-op if the feature is disabled.
     */
    fun scheduleNext() {
        val repo = AlarmRepository(context)
        if (!repo.getPreShabbatReminderEnabled()) {
            cancel()
            return
        }

        val cityIndex = repo.getDefaultCityIndex()
            .coerceIn(0, ShabbatTimesCalculator.CITIES.lastIndex)
        val city = ShabbatTimesCalculator.CITIES[cityIndex]

        val candleLighting = ShabbatTimesCalculator.computeNextCandleLighting(city)
        if (candleLighting == null) {
            Log.e(TAG, "Could not compute candle lighting for ${city.nameEn}")
            return
        }

        val triggerAt = candleLighting.time - REMINDER_OFFSET_MS
        if (triggerAt <= System.currentTimeMillis()) {
            // The reminder for this Friday has already passed; KosherJava has
            // rolled to next week in computeNextCandleLighting — but double-guard.
            Log.d(TAG, "Reminder time $triggerAt is in the past; skipping")
            return
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            buildPendingIntent()
        )
        Log.d(TAG, "Reminder scheduled for $triggerAt (${city.nameHe})")
    }

    fun cancel() {
        alarmManager.cancel(buildPendingIntent())
    }

    private fun buildPendingIntent(): PendingIntent {
        val intent = Intent(context, ShabbatReminderReceiver::class.java).apply {
            action = ACTION_FIRE
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val TAG = "ShabbatReminder"
        private const val REQUEST_CODE = 2001
        private const val REMINDER_OFFSET_MS = 30 * 60 * 1_000L // 30 minutes
        const val ACTION_FIRE = "com.example.shabbatalarm.ACTION_REMINDER_FIRE"
    }
}
