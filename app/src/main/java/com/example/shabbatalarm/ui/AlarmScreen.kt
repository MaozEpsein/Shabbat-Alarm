package com.example.shabbatalarm.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.shabbatalarm.alarm.AlarmRepository
import com.example.shabbatalarm.alarm.AlarmScheduler
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scheduler = remember { AlarmScheduler(context) }
    val repository = remember { AlarmRepository(context) }
    val now = remember { Calendar.getInstance() }

    val timePickerState = rememberTimePickerState(
        initialHour = now.get(Calendar.HOUR_OF_DAY),
        initialMinute = now.get(Calendar.MINUTE),
        is24Hour = true
    )

    var scheduledLabel by rememberSaveable { mutableStateOf<String?>(null) }

    val exactAlarmSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { /* no-op */ }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* no-op */ }

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
                    formatTriggerTime(scheduled)
                } else {
                    if (scheduled != null) repository.clear()
                    null
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = "Shabbat Alarm",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = "Pick a time. The alarm fires once and plays for 15 seconds.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        TimePicker(state = timePickerState)

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
                        scheduledLabel = formatTriggerTime(triggerAt)
                    } else {
                        Toast.makeText(
                            context,
                            "Please allow exact alarms for Shabbat Alarm.",
                            Toast.LENGTH_LONG
                        ).show()
                        exactAlarmSettingsLauncher.launch(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        )
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Set Alarm",
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
                        text = "Cancel",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = scheduledLabel?.let { "Alarm set for $it" } ?: "No alarm set",
            style = MaterialTheme.typography.bodyLarge,
            color = if (scheduledLabel != null)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatTriggerTime(triggerAtMillis: Long): String {
    val target = Calendar.getInstance().apply { timeInMillis = triggerAtMillis }
    val today = Calendar.getInstance()
    val isTomorrow = target.get(Calendar.DAY_OF_YEAR) != today.get(Calendar.DAY_OF_YEAR) ||
            target.get(Calendar.YEAR) != today.get(Calendar.YEAR)

    val time = SimpleDateFormat("HH:mm", Locale.US).format(Date(triggerAtMillis))
    return if (isTomorrow) "$time (tomorrow)" else time
}
