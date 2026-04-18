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

    companion object {
        private const val PREFS_NAME = "shabbat_alarm_prefs"
        private const val KEY_TRIGGER_AT = "trigger_at_millis"
    }
}
