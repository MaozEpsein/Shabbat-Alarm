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

    /**
     * User-selected alarm tone URI. Null means fall back to the system default
     * (RingtoneManager.getDefaultUri(TYPE_ALARM)).
     */
    fun getAlarmToneUri(): String? = prefs.getString(KEY_ALARM_TONE_URI, null)

    fun setAlarmToneUri(uri: String?) {
        prefs.edit().apply {
            if (uri == null) remove(KEY_ALARM_TONE_URI) else putString(KEY_ALARM_TONE_URI, uri)
        }.apply()
    }

    /** Whether to vibrate the device in addition to playing the tone (default false). */
    fun getVibrationEnabled(): Boolean = prefs.getBoolean(KEY_VIBRATION, false)

    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATION, enabled).apply()
    }

    /** Weekly pre-Shabbat reminder (30 min before candle lighting). Default: false. */
    fun getPreShabbatReminderEnabled(): Boolean =
        prefs.getBoolean(KEY_REMINDER_ENABLED, false)

    fun setPreShabbatReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply()
    }

    /** Index into ShabbatTimesCalculator.CITIES used by the reminder. Default: 0 (Jerusalem). */
    fun getDefaultCityIndex(): Int = prefs.getInt(KEY_DEFAULT_CITY_INDEX, 0)

    fun setDefaultCityIndex(index: Int) {
        prefs.edit().putInt(KEY_DEFAULT_CITY_INDEX, index).apply()
    }

    companion object {
        private const val PREFS_NAME = "shabbat_alarm_prefs"
        private const val KEY_TRIGGER_AT = "trigger_at_millis"
        private const val KEY_DURATION_SECONDS = "duration_seconds"
        private const val KEY_REPEAT_WEEKLY = "repeat_weekly"
        private const val KEY_ALARM_TONE_URI = "alarm_tone_uri"
        private const val KEY_VIBRATION = "vibration_enabled"
        private const val KEY_REMINDER_ENABLED = "pre_shabbat_reminder_enabled"
        private const val KEY_DEFAULT_CITY_INDEX = "default_city_index"

        const val DEFAULT_DURATION_SECONDS = 15
        const val MIN_DURATION_SECONDS = 5
        const val MAX_DURATION_SECONDS = 60
        const val DURATION_STEP_SECONDS = 5
    }
}
