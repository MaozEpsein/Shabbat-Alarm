package com.example.shabbatalarm.alarm

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.shabbatalarm.MainActivity
import com.example.shabbatalarm.R
import com.example.shabbatalarm.ShabbatAlarmApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Foreground service that plays the alarm tone on the ALARM audio stream for
 * [PLAYBACK_DURATION_MS] and then stops itself.
 *
 * The 15-second auto-stop is driven by a Coroutine on the main dispatcher, scoped
 * to the service lifecycle and cancelled in onDestroy.
 */
class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var autoStopJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "AlarmService onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "AlarmService onStartCommand")

        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        startPlayback()
        scheduleAutoStop()

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        autoStopJob?.cancel()
        serviceScope.cancel()
        releasePlayer()
        // The one-shot alarm has fired; clear the persisted pending-alarm record.
        AlarmRepository(this).clear()
        AlarmWakeLock.release()
        Log.d(TAG, "AlarmService onDestroy")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startPlayback() {
        val toneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: Settings.System.DEFAULT_ALARM_ALERT_URI

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            isLooping = true
            setOnErrorListener { _, what, extra ->
                Log.e(TAG, "MediaPlayer error: what=$what extra=$extra")
                stopSelf()
                true
            }
            try {
                setDataSource(this@AlarmService, toneUri)
                prepare()
                start()
                Log.d(TAG, "Playback started on ALARM stream")
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to start playback", t)
                stopSelf()
            }
        }
    }

    private fun scheduleAutoStop() {
        autoStopJob = serviceScope.launch {
            delay(PLAYBACK_DURATION_MS)
            Log.d(TAG, "15s elapsed — stopping service")
            stopSelf()
        }
    }

    private fun releasePlayer() {
        mediaPlayer?.let { mp ->
            runCatching { if (mp.isPlaying) mp.stop() }
            runCatching { mp.release() }
        }
        mediaPlayer = null
    }

    private fun buildNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, ShabbatAlarmApp.ALARM_CHANNEL_ID)
            .setContentTitle("Shabbat Alarm")
            .setContentText("Alarm ringing…")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .build()
    }

    companion object {
        private const val TAG = "AlarmService"
        private const val NOTIFICATION_ID = 1001
        private const val PLAYBACK_DURATION_MS = 15_000L
    }
}
