package com.example.shabbatalarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarm fired at ${System.currentTimeMillis()} — acquiring wake lock")
        // Hold the CPU awake across the hand-off to the service and through
        // the 15-second playback window. The service releases it in onDestroy.
        AlarmWakeLock.acquire(context)

        val serviceIntent = Intent(context, AlarmService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    companion object {
        private const val TAG = "AlarmReceiver"
    }
}
