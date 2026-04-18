package com.example.shabbatalarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Re-arms the scheduled alarm after the device reboots. AlarmManager does not
 * survive reboots, so we persist the trigger time and re-schedule here.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            return
        }

        val repo = AlarmRepository(context)
        val triggerAt = repo.getScheduled() ?: return

        if (triggerAt <= System.currentTimeMillis()) {
            Log.d(TAG, "Scheduled alarm already passed while device was off — clearing")
            repo.clear()
            return
        }

        Log.d(TAG, "Re-arming alarm for $triggerAt after boot")
        AlarmScheduler(context).scheduleAt(triggerAt)
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
