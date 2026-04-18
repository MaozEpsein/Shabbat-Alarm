package com.example.shabbatalarm.alarm

import android.annotation.SuppressLint
import android.content.Context
import android.os.PowerManager
import android.util.Log

/**
 * Keeps the CPU running from the moment the alarm fires (inside the receiver)
 * until the playback service is destroyed.
 *
 * A foreground service alone does NOT keep the CPU awake on Android — the system
 * can still let the SoC sleep during `delay(...)` in our coroutine. This is a
 * PARTIAL_WAKE_LOCK that stays alive across the receiver → service handoff.
 */
object AlarmWakeLock {

    private const val TAG = "AlarmWakeLock"
    private const val WAKE_LOCK_TAG = "ShabbatAlarm:AlarmWakeLock"

    /**
     * Safety timeout. The service should always release first, but if something goes
     * wrong we don't want to drain the battery.
     */
    private const val SAFETY_TIMEOUT_MS = 60_000L

    @SuppressLint("StaticFieldLeak")
    private var wakeLock: PowerManager.WakeLock? = null

    @Synchronized
    fun acquire(context: Context) {
        if (wakeLock?.isHeld == true) {
            Log.d(TAG, "acquire(): already held, no-op")
            return
        }
        val pm = context.applicationContext
            .getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG).apply {
            setReferenceCounted(false)
            acquire(SAFETY_TIMEOUT_MS)
        }
        Log.d(TAG, "acquire(): wake lock acquired")
    }

    @Synchronized
    fun release() {
        wakeLock?.let { wl ->
            if (wl.isHeld) {
                runCatching { wl.release() }
                Log.d(TAG, "release(): wake lock released")
            }
        }
        wakeLock = null
    }
}
