package com.example.shabbatalarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar

/**
 * Re-arms the scheduled alarm after the device reboots. AlarmManager does not
 * survive reboots, so we persist the trigger time and re-schedule here.
 * Handles weekly-repeat by fast-forwarding past any missed occurrences.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val repo = AlarmRepository(context)

        // Re-arm the weekly Shabbat reminder (no-op if disabled).
        ShabbatReminderScheduler(context).scheduleNext()

        val triggerAt = repo.getScheduled() ?: return
        val scheduler = AlarmScheduler(context)

        val now = System.currentTimeMillis()
        if (triggerAt > now) {
            Log.d(TAG, "Re-arming alarm for $triggerAt after boot")
            scheduler.scheduleAt(triggerAt)
            return
        }

        // Trigger time already passed while the device was off.
        if (repo.getRepeatWeekly()) {
            // Fast-forward to the next future Friday at the same wall-clock time.
            val cal = Calendar.getInstance().apply { timeInMillis = triggerAt }
            while (cal.timeInMillis <= now) {
                cal.add(Calendar.DAY_OF_YEAR, 7)
            }
            val nextTrigger = cal.timeInMillis
            Log.d(TAG, "Weekly alarm missed during reboot — advancing to $nextTrigger")
            scheduler.scheduleAt(nextTrigger)
            repo.setScheduled(nextTrigger)
        } else {
            Log.d(TAG, "One-shot alarm already passed while device was off — clearing")
            repo.clear()
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
