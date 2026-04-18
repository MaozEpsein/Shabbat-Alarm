package com.example.shabbatalarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarm fired at ${System.currentTimeMillis()} — acquiring wake lock")
        // Hold the CPU awake across the hand-off to the service and through
        // the playback window. The service releases it in onDestroy.
        AlarmWakeLock.acquire(context)

        // Weekly repeat: re-arm for +7 days BEFORE starting the service so the
        // next occurrence is scheduled even if the service/device has issues.
        val repo = AlarmRepository(context)
        if (repo.getRepeatWeekly()) {
            val currentTrigger = repo.getScheduled()
            if (currentTrigger != null) {
                val nextTrigger = advanceByOneWeek(currentTrigger)
                AlarmScheduler(context).scheduleAt(nextTrigger)
                repo.setScheduled(nextTrigger)
                Log.d(TAG, "Weekly repeat enabled — rescheduled for $nextTrigger")
            }
        }

        val serviceIntent = Intent(context, AlarmService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    /** Adds exactly 7 calendar days, preserving wall-clock time across DST transitions. */
    private fun advanceByOneWeek(triggerMillis: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = triggerMillis }
        cal.add(Calendar.DAY_OF_YEAR, 7)
        return cal.timeInMillis
    }

    companion object {
        private const val TAG = "AlarmReceiver"
    }
}
