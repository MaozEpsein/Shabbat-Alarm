package com.example.shabbatalarm.alarm

import android.content.Context

/**
 * Persists the currently scheduled alarm trigger time so that the UI can restore
 * its status text across app relaunches and the BootReceiver can re-arm the alarm
 * after a reboot.
 */
class AlarmRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun setScheduled(triggerAtMillis: Long) {
        prefs.edit().putLong(KEY_TRIGGER_AT, triggerAtMillis).apply()
    }

    /** Returns the scheduled trigger time in millis, or null if nothing is scheduled. */
    fun getScheduled(): Long? {
        val value = prefs.getLong(KEY_TRIGGER_AT, -1L)
        return if (value > 0L) value else null
    }

    fun clear() {
        prefs.edit().remove(KEY_TRIGGER_AT).apply()
    }

    /** User-chosen alarm playback duration in seconds (5–60, default 15). */
    fun getDurationSeconds(): Int =
        prefs.getInt(KEY_DURATION_SECONDS, DEFAULT_DURATION_SECONDS)

    fun setDurationSeconds(seconds: Int) {
        prefs.edit().putInt(KEY_DURATION_SECONDS, seconds).apply()
    }

    /** Whether the alarm should re-arm every 7 days after it fires (default false). */
    fun getRepeatWeekly(): Boolean = prefs.getBoolean(KEY_REPEAT_WEEKLY, false)

    fun setRepeatWeekly(repeat: Boolean) {
        prefs.edit().putBoolean(KEY_REPEAT_WEEKLY, repeat).apply()
    }

    companion object {
        private const val PREFS_NAME = "shabbat_alarm_prefs"
        private const val KEY_TRIGGER_AT = "trigger_at_millis"
        private const val KEY_DURATION_SECONDS = "duration_seconds"
        private const val KEY_REPEAT_WEEKLY = "repeat_weekly"

        const val DEFAULT_DURATION_SECONDS = 15
        const val MIN_DURATION_SECONDS = 5
        const val MAX_DURATION_SECONDS = 60
        const val DURATION_STEP_SECONDS = 5
    }
}
