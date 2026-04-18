# שעון מעורר - לשבת 🕯️

אפליקציית אנדרואיד מינימליסטית לשעון מעורר שבתי. מוגדר פעם אחת, פועל פעם אחת, ונעצר לבד אחרי 15 שניות (ניתן לשינוי).

---

## 📲 התקנה מהירה

### אופציה 1 — קישור הורדה ישיר

> 🔗 **[הורד את ShabbatAlarm.apk](PLACEHOLDER_DOWNLOAD_URL)**
>
> _(גודל: ~20 MB · דרוש Android 8.0 ומעלה)_
>
> **שים לב:** החלף את `PLACEHOLDER_DOWNLOAD_URL` בקישור אמיתי אחרי שתעלה את ה-APK ל-GitHub Releases / Google Drive / Dropbox.

### אופציה 2 — מחבר שיש לו את האפליקציה

קבל את קובץ ה-APK דרך WhatsApp: החבר פותח את האפליקציה → **⚙️ הגדרות** → **"שתף את האפליקציה"** → בוחר אותך ב-WhatsApp.

### שלבי התקנה (פעם ראשונה)

1. פתח את קובץ ה-`.apk` בטלפון (דרך Downloads או קובץ שהתקבל ב-WhatsApp)
2. אנדרואיד יציג אזהרה **"מקורות לא ידועים"** — לחץ "הגדרות" → הפעל "אפשר מהמקור הזה"
3. חזור → לחץ **"Install"**
4. בפתיחה הראשונה אשר הרשאות:
   - 🔔 **התראות** — כדי שתראה את האזעקה
   - ⏰ **אזעקות מדויקות** — כדי שהשעון יפעל בזמן
5. ב-**Settings → Apps → Shabbat Alarm → Battery** → בטל אופטימיזציית סוללה (או לחץ "תקן עכשיו" בכרטיסייה האדומה שמופיעה בתוך האפליקציה)

---

## ✨ מה האפליקציה עושה

### הבסיס
- ✅ **שעון מעורר חד-פעמי** — מופעל פעם אחת בלבד
- ✅ **עצירה אוטומטית** — 15 שניות (ניתן לשנות 5-60 שניות)
- ✅ **עמיד ל-Doze Mode** — פועל גם כשהטלפון ישן זמן רב
- ✅ **משתמש בערוץ ALARM** — מכבד את עוצמת האזעקות במערכת

### פיצ'רים מתקדמים
- 🕯️ **זמני שבת לשמונה ערים מרכזיות בישראל** — ירושלים, תל אביב, חיפה, באר שבע, אילת, טבריה, נתניה, אשדוד (חישוב אוטומטי, ללא אינטרנט)
- 📅 **תאריך עברי** בדיאלוג — "י״א באייר התשפ״ו"
- 🔁 **חזרה שבועית** — אופציה להפעיל כל יום שישי באותה שעה
- ⏰ **תזכורת מקדימה** — 30 דקות לפני כניסת שבת (התראה עדינה, ללא צליל)
- 🔊 **בחירת צליל אזעקה** — מתוך צלילי המערכת, עם תצוגה מקדימה של 5 שניות
- 📳 **רטט אופציונלי** — בנוסף לצליל
- 🌅 **Fade-in עוצמה** — מתחיל חלש ועולה בהדרגה
- 🎨 **ממשק עברי מלא** — RTL, טיפוגרפיה סריף, ערכת צבעי זהב ולילה
- 🕯️ **נר מונפש** במסך הראשי — מרצד כשיש שעון מעורר פעיל
- 📤 **שיתוף APK** ישירות מהאפליקציה — כפתור שולח את קובץ ההתקנה ב-WhatsApp

### אבטחה ואמינות
- 🔒 **אזהרת Battery Optimization** — מתריע כשהמערכת עלולה לעכב את האזעקה
- 💪 **WakeLock** — מבטיח שה-CPU ער בזמן הניגון
- 🔄 **שחזור אחרי reboot** — שעון מעורר מתוזמן שורד אתחול של הטלפון
- 🔁 **Self-healing** — אם האזעקה השבועית פוספסה (טלפון כבוי שבוע), מקפיצה אוטומטית ליום שישי הבא

---

## 🏗 ארכיטקטורה טכנית (תמצית)

### ערימת טכנולוגיות
| רכיב | בחירה |
|---|---|
| שפה | **Kotlin 2.0** |
| UI | **Jetpack Compose** + Material 3 |
| תזמון | `AlarmManager.setExactAndAllowWhileIdle` |
| ניגון | `MediaPlayer` על `USAGE_ALARM` |
| אמינות | `PARTIAL_WAKE_LOCK` + Foreground Service |
| זמני שבת | **KosherJava 2.5.0** |
| אחסון | SharedPreferences |
| Concurrency | Kotlin Coroutines |

### זרימת האזעקה
```
MainActivity (Compose UI)
         ↓ Set Alarm
AlarmScheduler → AlarmManager.setExactAndAllowWhileIdle
         ↓ (alarm fires)
AlarmReceiver → acquire WakeLock + start Service
         ↓
AlarmService → MediaPlayer + vibration + 15s Coroutine
         ↓
stopSelf() → cleanup & release WakeLock
```

### מבנה הקוד
```
com.example.shabbatalarm/
├── MainActivity.kt
├── ShabbatAlarmApp.kt               (Application + notification channels)
├── alarm/
│   ├── AlarmReceiver.kt             (ראשי: קולט אזעקה, מחדש שבועית)
│   ├── AlarmService.kt              (ניגון + רטט + עצירה)
│   ├── AlarmScheduler.kt            (עטיפה ל-AlarmManager)
│   ├── AlarmRepository.kt           (SharedPreferences)
│   ├── AlarmWakeLock.kt             (PARTIAL_WAKE_LOCK singleton)
│   ├── AlarmTones.kt                (רשימת צלילי המערכת)
│   ├── BootReceiver.kt              (שחזור אחרי reboot)
│   ├── ShabbatTimes.kt              (מחשבון זמני שבת + תאריך עברי)
│   ├── ShabbatReminderScheduler.kt  (תזכורת שבועית)
│   └── ShabbatReminderReceiver.kt   (התראת 30 דק׳)
└── ui/
    ├── AlarmScreen.kt               (מסך ראשי)
    ├── AnimatedCandle.kt            (ציור Canvas + flicker)
    ├── ShabbatTimesDialog.kt        (ℹ️)
    ├── SettingsDialog.kt            (⚙️ + sub-views)
    ├── BatteryOptimizationCard.kt   (כרטיסיית אזהרה)
    ├── TonePreview.kt               (MediaPlayer נגן מקדים)
    ├── ApkSharer.kt                 (FileProvider + Intent.ACTION_SEND)
    └── theme/                       (Color, Typography, Theme)
```

### הרשאות מניפסט
- `SCHEDULE_EXACT_ALARM`, `USE_EXACT_ALARM` — לתזמון מדויק
- `WAKE_LOCK` — לחיזוק מול Doze
- `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK` — לניגון
- `POST_NOTIFICATIONS` — להתראות
- `RECEIVE_BOOT_COMPLETED` — לשחזור אחרי reboot
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` — לעקיפת Doze
- `VIBRATE` — לרטט אופציונלי

---

## 🛠 בנייה מקוד מקור

### דרישות
- Android Studio **Ladybug** (2024.2) ומעלה
- JDK 17
- Android SDK API 34

### צעדים
```bash
# שיבוט הרפו
git clone <your-repo-url>
cd shabat-alarm

# פתח ב-Android Studio
# Run ▶ (Gradle Sync יקרה אוטומטית)
```

### יצירת APK לשיתוף
**Android Studio → Build → Build Bundle(s) / APK(s) → Build APK(s)**

הקובץ יופיע ב-`app/build/outputs/apk/debug/app-debug.apk`.

---

## 📄 רישיון

פרויקט אישי · לא למטרות מסחריות.

שימוש בספריית **KosherJava** תחת רישיון LGPL.

---

**נבנה ב-Cursor + Claude Code + Android Studio** 🛠️
