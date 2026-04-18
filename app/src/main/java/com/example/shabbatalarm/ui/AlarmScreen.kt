package com.example.shabbatalarm.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.shabbatalarm.R
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.shabbatalarm.alarm.AlarmRepository
import com.example.shabbatalarm.alarm.AlarmScheduler
import com.example.shabbatalarm.alarm.ShabbatReminderScheduler
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val shareScope = rememberCoroutineScope()
    val scheduler = remember { AlarmScheduler(context) }
    val reminderScheduler = remember { ShabbatReminderScheduler(context) }
    val repository = remember { AlarmRepository(context) }
    val now = remember { Calendar.getInstance() }

    val timePickerState = rememberTimePickerState(
        initialHour = now.get(Calendar.HOUR_OF_DAY),
        initialMinute = now.get(Calendar.MINUTE),
        is24Hour = true
    )

    var scheduledLabel by rememberSaveable { mutableStateOf<String?>(null) }
    var showShabbatTimes by rememberSaveable { mutableStateOf(false) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var durationSeconds by rememberSaveable { mutableStateOf(repository.getDurationSeconds()) }
    var isBatteryOptimized by rememberSaveable { mutableStateOf(false) }
    var repeatWeekly by rememberSaveable { mutableStateOf(repository.getRepeatWeekly()) }
    var vibrationEnabled by rememberSaveable { mutableStateOf(repository.getVibrationEnabled()) }
    var alarmToneUri by rememberSaveable { mutableStateOf(repository.getAlarmToneUri()) }
    var reminderEnabled by rememberSaveable { mutableStateOf(repository.getPreShabbatReminderEnabled()) }
    var defaultCityIndex by rememberSaveable { mutableStateOf(repository.getDefaultCityIndex()) }

    // Pre-load strings that are referenced from non-composable lambdas.
    val tomorrowSuffix = stringResource(R.string.tomorrow_suffix)
    val allowExactAlarmToast = stringResource(R.string.allow_exact_alarm_toast)

    val exactAlarmSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { /* no-op */ }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* no-op */ }

    val batterySettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // After user returns from the battery-optimization dialog, re-check on next resume.
        isBatteryOptimized = !isIgnoringBatteryOptimizations(context)
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Keep the displayed status in sync with the persisted state whenever the screen resumes.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val scheduled = repository.getScheduled()
                scheduledLabel = if (scheduled != null && scheduled > System.currentTimeMillis()) {
                    formatTriggerTime(scheduled, tomorrowSuffix)
                } else {
                    if (scheduled != null) repository.clear()
                    null
                }
                isBatteryOptimized = !isIgnoringBatteryOptimizations(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val isLit = scheduledLabel != null
    val topTint by animateColorAsState(
        targetValue = if (isLit)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        else
            Color.Transparent,
        animationSpec = tween(durationMillis = 800),
        label = "backgroundTint"
    )
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(topTint, Color.Transparent),
        startY = 0f,
        endY = 600f
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { showShabbatTimes = true }) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = stringResource(R.string.cd_shabbat_times),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = { showSettings = true }) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.cd_settings),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (isBatteryOptimized) {
            BatteryOptimizationCard(
                onFixClick = {
                    batterySettingsLauncher.launch(
                        buildIgnoreBatteryOptIntent(context.packageName)
                    )
                }
            )
        }

        AnimatedCandle(
            isLit = isLit,
            modifier = Modifier.size(width = 140.dp, height = 120.dp)
        )

        Text(
            text = stringResource(R.string.screen_title),
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = stringResource(R.string.screen_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val timePickerColors = TimePickerDefaults.colors(
            timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )

        TimePicker(state = timePickerState, colors = timePickerColors)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (scheduler.canScheduleExactAlarms()) {
                        val triggerAt = scheduler.schedule(
                            timePickerState.hour,
                            timePickerState.minute
                        )
                        repository.setScheduled(triggerAt)
                        scheduledLabel = formatTriggerTime(triggerAt, tomorrowSuffix)
                    } else {
                        Toast.makeText(context, allowExactAlarmToast, Toast.LENGTH_LONG).show()
                        exactAlarmSettingsLauncher.launch(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        )
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.set_alarm),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (scheduledLabel != null) {
                OutlinedButton(
                    onClick = {
                        scheduler.cancel()
                        repository.clear()
                        scheduledLabel = null
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when {
                scheduledLabel == null -> stringResource(R.string.no_alarm_set)
                repeatWeekly -> stringResource(R.string.alarm_set_for_weekly, scheduledLabel!!)
                else -> stringResource(R.string.alarm_set_for, scheduledLabel!!)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = if (scheduledLabel != null)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (showShabbatTimes) {
        ShabbatTimesDialog(
            defaultCityIndex = defaultCityIndex,
            onCityChange = {
                defaultCityIndex = it
                repository.setDefaultCityIndex(it)
            },
            onDismiss = { showShabbatTimes = false }
        )
    }

    if (showSettings) {
        SettingsDialog(
            currentDurationSeconds = durationSeconds,
            repeatWeekly = repeatWeekly,
            vibrationEnabled = vibrationEnabled,
            currentToneUri = alarmToneUri,
            reminderEnabled = reminderEnabled,
            onDurationChange = {
                durationSeconds = it
                repository.setDurationSeconds(it)
            },
            onRepeatChange = {
                repeatWeekly = it
                repository.setRepeatWeekly(it)
            },
            onVibrationChange = {
                vibrationEnabled = it
                repository.setVibrationEnabled(it)
            },
            onToneChange = {
                alarmToneUri = it
                repository.setAlarmToneUri(it)
            },
            onReminderEnabledChange = {
                reminderEnabled = it
                repository.setPreShabbatReminderEnabled(it)
                if (it) reminderScheduler.scheduleNext() else reminderScheduler.cancel()
            },
            onShareApp = { ApkSharer.share(context, shareScope) },
            onDismiss = { showSettings = false }
        )
    }
}

private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return pm.isIgnoringBatteryOptimizations(context.packageName)
}

private fun buildIgnoreBatteryOptIntent(packageName: String): Intent =
    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
        data = Uri.parse("package:$packageName")
    }

private fun formatTriggerTime(triggerAtMillis: Long, tomorrowSuffix: String): String {
    val target = Calendar.getInstance().apply { timeInMillis = triggerAtMillis }
    val today = Calendar.getInstance()
    val isTomorrow = target.get(Calendar.DAY_OF_YEAR) != today.get(Calendar.DAY_OF_YEAR) ||
            target.get(Calendar.YEAR) != today.get(Calendar.YEAR)

    val time = SimpleDateFormat("HH:mm", Locale.US).format(Date(triggerAtMillis))
    return if (isTomorrow) "$time ($tomorrowSuffix)" else time
}
