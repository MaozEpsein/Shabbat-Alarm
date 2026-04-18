# Shabbat Alarm 🕯️

A minimalist Android alarm app designed for Shabbat use. The alarm fires **once**, plays for **exactly 15 seconds**, and stops automatically — no user interaction required. Built to reliably wake the device even from Doze mode.

---

## 🎯 Core Requirements

| Requirement | Description |
|---|---|
| **Single-shot** | Alarm triggers once and does not repeat |
| **Auto-stop** | Audio plays for exactly 15 seconds, then stops itself |
| **Doze-resilient** | Fires even when device is in Doze / idle mode |
| **Alarm stream** | Audio respects the system alarm volume |
| **No interaction** | User never needs to dismiss or snooze |

---

## 🛠 Tech Stack

- **Language:** Kotlin
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 34+ (Android 14+)
- **UI:** Jetpack Compose (Material 3) — simplest for a one-screen app
- **Build system:** Gradle Kotlin DSL
- **Async:** Coroutines (for the 15-second timer inside the service)

---

## 🧩 Architecture

```
┌──────────────────┐   setExactAndAllowWhileIdle   ┌──────────────────┐
│  MainActivity    │ ─────────────────────────────▶│   AlarmManager   │
│  (Compose UI)    │                                └────────┬─────────┘
│  TimePicker +    │                                         │ PendingIntent
│  "Set Alarm" btn │                                         ▼
└──────────────────┘                                ┌──────────────────┐
                                                    │ AlarmReceiver    │
                                                    │ (BroadcastRcv)   │
                                                    └────────┬─────────┘
                                                             │ startForegroundService
                                                             ▼
                                                    ┌──────────────────┐
                                                    │  AlarmService    │
                                                    │ (Foreground Svc) │
                                                    │ ─ MediaPlayer    │
                                                    │ ─ 15s coroutine  │
                                                    │ ─ stopSelf()     │
                                                    └──────────────────┘
```

### Components

1. **`MainActivity`** — Compose screen with a `TimePicker` and a "Set Alarm" button. Handles `SCHEDULE_EXACT_ALARM` permission on Android 12+.
2. **`AlarmScheduler`** — Thin wrapper around `AlarmManager` that schedules a `PendingIntent` with `setExactAndAllowWhileIdle`.
3. **`AlarmReceiver`** — `BroadcastReceiver` that receives the alarm and immediately starts `AlarmService` as a foreground service.
4. **`AlarmService`** — Foreground service that:
   - Posts a notification (required for foreground services on API 26+).
   - Acquires a partial `WakeLock`.
   - Plays a bundled sound via `MediaPlayer` on `AudioAttributes.USAGE_ALARM`.
   - Launches a coroutine that delays 15 000 ms, then calls `stopSelf()`.

---

## 🔐 Permissions

Declared in `AndroidManifest.xml`:

| Permission | Purpose |
|---|---|
| `SCHEDULE_EXACT_ALARM` | Required on API 31+ for exact alarms |
| `USE_EXACT_ALARM` | Granted by default on API 33+ for alarm-clock apps |
| `FOREGROUND_SERVICE` | Required to run the playback service |
| `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Required on API 34+ for media-playback FGS type |
| `WAKE_LOCK` | Keep CPU awake during the 15 s playback |
| `POST_NOTIFICATIONS` | Required on API 33+ for the FGS notification |

---

## 📱 UI (Minimalist)

A single screen containing:
- A `TimePicker` (Compose `TimePicker` / `TimePickerDialog`)
- A **"Set Alarm"** button
- A small status text showing the scheduled time (or "No alarm set")

---

## 🚦 Development Stages (to MVP)

Each stage is self-contained and verifiable before moving on.

### **Stage 1 — Project Scaffolding**
- Create Gradle project (Kotlin DSL, Compose, Material 3).
- Configure `minSdk = 26`, `targetSdk = 34`.
- Add dependencies: Compose BOM, Activity-Compose, Lifecycle, Coroutines.
- Set up `AndroidManifest.xml` with all permissions.
- **Verification:** App builds and launches a blank Compose screen.

### **Stage 2 — Minimal UI**
- Build the Compose screen: `TimePicker` + "Set Alarm" button + status text.
- Add time-picking state with `rememberSaveable`.
- **Verification:** User can pick a time; selected time is displayed.

### **Stage 3 — Alarm Scheduling**
- Implement `AlarmScheduler` with `setExactAndAllowWhileIdle`.
- Build the `PendingIntent` targeting `AlarmReceiver`.
- Handle `SCHEDULE_EXACT_ALARM` runtime check (API 31+): if not granted, open `ACTION_REQUEST_SCHEDULE_EXACT_ALARM` settings.
- Compute next occurrence: if selected time already passed today → schedule for tomorrow.
- **Verification:** `adb shell dumpsys alarm | grep <package>` shows the scheduled alarm.

### **Stage 4 — Broadcast Receiver**
- Create `AlarmReceiver` registered in manifest (not runtime — survives process death).
- On receive → `ContextCompat.startForegroundService(…)`.
- **Verification:** Log statement in receiver fires at the scheduled time (even if app killed).

### **Stage 5 — Foreground Service + Audio**
- Create `AlarmService` extending `Service`.
- In `onStartCommand`:
  - Build notification channel (`IMPORTANCE_HIGH`).
  - Call `startForeground(…)` with `FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK`.
  - Configure `MediaPlayer` with `AudioAttributes.USAGE_ALARM` + `CONTENT_TYPE_SONIFICATION`.
  - Start playback.
- Bundle a short `.mp3`/`.ogg` in `res/raw/`.
- **Verification:** Service starts and plays audio on the ALARM stream.

### **Stage 6 — 15-Second Auto-Stop**
- Inside service, launch a `CoroutineScope(Dispatchers.Main)` that:
  - `delay(15_000)`
  - `mediaPlayer.stop()` / `release()`
  - `stopSelf()`
- Cancel the scope in `onDestroy` for safety.
- **Verification:** Audio plays exactly 15 s (± ~50 ms) then stops.

### **Stage 7 — Doze-Mode Hardening**
- Acquire a partial `WakeLock` in the receiver, release in the service's `onDestroy`.
- Confirm `setExactAndAllowWhileIdle` is used (not `setExact`).
- **Verification:** Test with `adb shell dumpsys deviceidle force-idle` then wait for alarm → alarm fires.

### **Stage 8 — MVP Polish**
- Add a "Cancel Alarm" button that cancels the `PendingIntent`.
- Persist the scheduled time in `SharedPreferences` so the status text survives relaunch.
- Handle `BOOT_COMPLETED` (optional — re-schedule after reboot).
- **Verification:** Full happy path works end-to-end on a real device.

---

## 🧪 Testing Checklist (MVP Done Definition)

- [ ] Pick a time 2 minutes in the future → alarm fires at the right second
- [ ] Audio plays on the alarm stream (volume key shows "Alarm")
- [ ] Audio stops exactly after 15 seconds without user input
- [ ] Lock the phone before firing → alarm still fires
- [ ] Force Doze (`adb shell dumpsys deviceidle force-idle`) → alarm still fires
- [ ] Kill the app from recents → alarm still fires (manifest-registered receiver)
- [ ] Only fires **once** (does not repeat the next day)

---

## 📂 Project Structure (planned)

```
app/
├── src/main/
│   ├── java/com/example/shabbatalarm/
│   │   ├── MainActivity.kt
│   │   ├── ui/AlarmScreen.kt
│   │   ├── alarm/AlarmScheduler.kt
│   │   ├── alarm/AlarmReceiver.kt
│   │   └── alarm/AlarmService.kt
│   ├── res/
│   │   └── raw/alarm_tone.ogg
│   └── AndroidManifest.xml
└── build.gradle.kts
```

---

## 🎨 Post-MVP Polish (after end-to-end works)

- [ ] **Custom app launcher icon** — replace the placeholder vector with a graphically designed icon (candle / Shabbat theme)
- [ ] Themed background & gradient
- [ ] Distinct visual state for "alarm set" vs "no alarm"
- [ ] Candle or clock accent icon near the title
- [ ] Improved typography hierarchy

## ✅ Out of Scope (for MVP)

- Hebrew calendar / automatic Shabbat times
- Multiple alarms
- Repeating alarms
- Snooze / dismiss UI
- Localization / RTL polish

---

**Next step:** once this plan is approved, development will begin at **Stage 1**.
