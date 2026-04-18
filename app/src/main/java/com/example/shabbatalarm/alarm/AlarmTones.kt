package com.example.shabbatalarm.alarm

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log

data class AlarmTone(
    val title: String,
    val uri: Uri
)

object AlarmTones {

    private const val TAG = "AlarmTones"

    /**
     * Lists the alarm ringtones available on the device. The current system-default
     * tone is placed first for easy identification.
     */
    fun loadAvailable(context: Context): List<AlarmTone> {
        val tones = mutableListOf<AlarmTone>()
        val defaultUri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        try {
            val manager = RingtoneManager(context)
            manager.setType(RingtoneManager.TYPE_ALARM)
            val cursor = manager.cursor
            while (cursor.moveToNext()) {
                val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                val uri = manager.getRingtoneUri(cursor.position)
                if (!title.isNullOrBlank() && uri != null) {
                    tones.add(AlarmTone(title = title, uri = uri))
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to enumerate alarm ringtones", t)
        }

        // Move the system default to the top if we can identify it.
        if (defaultUri != null) {
            val defaultIndex = tones.indexOfFirst { it.uri == defaultUri }
            if (defaultIndex > 0) {
                val defaultTone = tones.removeAt(defaultIndex)
                tones.add(0, defaultTone)
            } else if (defaultIndex == -1) {
                tones.add(0, AlarmTone(title = "System default", uri = defaultUri))
            }
        }

        return tones
    }
}
