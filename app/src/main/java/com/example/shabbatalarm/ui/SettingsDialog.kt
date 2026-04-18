package com.example.shabbatalarm.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.shabbatalarm.alarm.AlarmRepository
import com.example.shabbatalarm.alarm.AlarmTone
import com.example.shabbatalarm.alarm.AlarmTones

@Composable
fun SettingsDialog(
    currentDurationSeconds: Int,
    repeatWeekly: Boolean,
    currentToneUri: String?,
    onDurationChange: (Int) -> Unit,
    onRepeatChange: (Boolean) -> Unit,
    onToneChange: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tones = remember { AlarmTones.loadAvailable(context) }
    val preview = remember { TonePreview(context, scope) }

    var showAlarmSoundPicker by rememberSaveable { mutableStateOf(false) }

    val effectiveSelectedUri = currentToneUri ?: tones.firstOrNull()?.uri?.toString()
    val currentToneTitle = tones.firstOrNull { it.uri.toString() == effectiveSelectedUri }
        ?.title ?: "System default"

    DisposableEffect(Unit) {
        onDispose { preview.release() }
    }

    Dialog(onDismissRequest = {
        preview.release()
        onDismiss()
    }) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                if (showAlarmSoundPicker) {
                    AlarmSoundPickerView(
                        tones = tones,
                        selectedUri = effectiveSelectedUri,
                        onSelect = { tone ->
                            preview.play(tone.uri)
                            onToneChange(tone.uri.toString())
                        },
                        onBack = {
                            preview.release()
                            showAlarmSoundPicker = false
                        },
                        onDismiss = {
                            preview.release()
                            onDismiss()
                        }
                    )
                } else {
                    MainSettingsView(
                        currentDurationSeconds = currentDurationSeconds,
                        repeatWeekly = repeatWeekly,
                        currentToneTitle = currentToneTitle,
                        onDurationChange = onDurationChange,
                        onRepeatChange = onRepeatChange,
                        onOpenSoundPicker = { showAlarmSoundPicker = true },
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun MainSettingsView(
    currentDurationSeconds: Int,
    repeatWeekly: Boolean,
    currentToneTitle: String,
    onDurationChange: (Int) -> Unit,
    onRepeatChange: (Boolean) -> Unit,
    onOpenSoundPicker: () -> Unit,
    onDismiss: () -> Unit
) {
    Text(
        text = "Settings",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(20.dp))

    // ── Alarm sound (opens sub-view) ───────────────────────────────────────
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenSoundPicker)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Alarm sound",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = currentToneTitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Spacer(modifier = Modifier.height(12.dp))
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    Spacer(modifier = Modifier.height(16.dp))

    // ── Alarm duration ─────────────────────────────────────────────────────
    Text(
        text = "Alarm duration: $currentDurationSeconds seconds",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
    Slider(
        value = currentDurationSeconds.toFloat(),
        onValueChange = { onDurationChange(it.toInt()) },
        valueRange = AlarmRepository.MIN_DURATION_SECONDS.toFloat()
                ..AlarmRepository.MAX_DURATION_SECONDS.toFloat(),
        steps = ((AlarmRepository.MAX_DURATION_SECONDS
                - AlarmRepository.MIN_DURATION_SECONDS)
                / AlarmRepository.DURATION_STEP_SECONDS) - 1,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    // ── Repeat weekly ──────────────────────────────────────────────────────
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Repeat every week",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Fires again at the same time next week.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = repeatWeekly, onCheckedChange = onRepeatChange)
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(onClick = onDismiss) {
            Text("Close")
        }
    }
}

@Composable
private fun AlarmSoundPickerView(
    tones: List<AlarmTone>,
    selectedUri: String?,
    onSelect: (AlarmTone) -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Alarm sound",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Tap a tone to preview for 5 seconds and select it.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(12.dp))

    Column(
        modifier = Modifier
            .heightIn(max = 360.dp)
            .verticalScroll(rememberScrollState())
    ) {
        tones.forEach { tone ->
            ToneRow(
                tone = tone,
                selected = tone.uri.toString() == selectedUri,
                onSelect = { onSelect(tone) }
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(onClick = onDismiss) {
            Text("Close")
        }
    }
}

@Composable
private fun ToneRow(
    tone: AlarmTone,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = tone.title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
        )
    }
}
