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
- ✅ **עד 5 שעונים מעוררים במקביל** — למשל: הדלקת נרות + הבדלה
- ✅ **חד-פעמי או שבועי** — כל שעון עצמאי, עם אפשרות לחזרה אוטומטית
- ✅ **עצירה אוטומטית** — 15 שניות כברירת מחדל (ניתן לשנות 5-60 שניות)
- ✅ **עמיד ל-Doze Mode** — פועל גם כשהטלפון ישן זמן רב
- ✅ **משתמש בערוץ ALARM** — מכבד את עוצמת האזעקות במערכת

### זמני שבת
- 🕯️ **זמני שבת לשמונה ערים מרכזיות בישראל** — ירושלים, תל אביב, חיפה, באר שבע, אילת, טבריה, נתניה, אשדוד (חישוב אוטומטי, ללא אינטרנט)
- 📅 **תאריך עברי ולועזי** — "יום שישי, 25 באפריל · י״א באייר התשפ״ו"
- 📖 **זמנים נוספים** בלשונית — סוף זמן קריאת שמע, מנחה גדולה/קטנה, פלג המנחה, שקיעה וכו׳. בחירת עיר מתוך dropdown.
- 🎉 **זיהוי חגים אוטומטי** — כשיש יום טוב קרוב (אפילו באמצע השבוע), האפליקציה מציגה "זמני יום טוב — שמחת תורה" במקום "זמני שבת"
- ⏰ **תזכורת מקדימה** — 40 דקות לפני כניסת שבת בירושלים (כ-60 דק׳ בשאר הארץ), התראה עדינה ללא צליל

### התאמה אישית
- 🔊 **בחירת צליל אזעקה** — מתוך צלילי המערכת, עם תצוגה מקדימה של 5 שניות
- 🎵 **הוספת קטעי מוזיקה מהטלפון** — בחר MP3/WAV/M4A/OGG מכל אפליקציית קבצים (עד 10 קטעים מותאמים)
- 📳 **רטט אופציונלי** — בנוסף לצליל
- 🌅 **Fade-in עוצמה** — מתחיל חלש ועולה בהדרגה

### עיצוב
- 🎨 **ממשק עברי מלא** — RTL, טיפוגרפיה סריף, ערכת צבעי זהב וכחול-לילה
- 🕯️ **נר מונפש** במסך הראשי — מרצד כשיש שעון מעורר פעיל
- ✨ **אייקון מעוצב** — וקטור עם גרדיאנטים והילה זוהרת
- 🌙 **תמיכה ב-Dark Mode** — עקב הגדרות המערכת
- 📤 **שיתוף APK** ישירות מהאפליקציה — כפתור שולח את קובץ ההתקנה ב-WhatsApp

### אבטחה ואמינות
- 🔒 **אזהרת Battery Optimization** — מתריע כשהמערכת עלולה לעכב את האזעקה
- 💪 **WakeLock** — מבטיח שה-CPU ער בזמן הניגון
- 🔄 **שחזור אחרי reboot** — כל השעונים המעוררים שורדים אתחול של הטלפון
- 🔁 **Self-healing** — אזעקה שבועית שפוספסה (טלפון כבוי שבוע) קופצת אוטומטית ליום שישי הבא
- 🔁 **Fallback לצליל מערכת** — אם קטע מותאם נמחק/נעלם, האפליקציה משמיעה את ברירת המחדל של המערכת

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
├── ShabbatAlarmApp.kt               (Application + שני ערוצי התראות)
├── alarm/
│   ├── AlarmReceiver.kt             (קולט ירי אזעקה, מחדש שבועית לפי ID)
│   ├── AlarmService.kt              (ניגון + רטט + עצירה + fallback)
│   ├── AlarmScheduler.kt            (עטיפה ל-AlarmManager, תומך ריבוי אזעקות)
│   ├── AlarmRepository.kt           (SharedPreferences: רשימת אזעקות, הגדרות, קטעים מותאמים)
│   ├── AlarmWakeLock.kt             (PARTIAL_WAKE_LOCK singleton)
│   ├── AlarmTones.kt                (צלילי מערכת + קטעים מותאמים של המשתמש)
│   ├── BootReceiver.kt              (שחזור כל האזעקות + התזכורת אחרי reboot)
│   ├── ShabbatTimes.kt              (זמני שבת + זמנים נוספים + זיהוי חגים + תאריך עברי)
│   ├── ShabbatReminderScheduler.kt  (תזכורת שבועית לירושלים)
│   └── ShabbatReminderReceiver.kt   (התראת 40 דק׳ + תזמון שבוע הבא)
└── ui/
    ├── AlarmScreen.kt               (מסך ראשי: TimePicker + רשימת אזעקות)
    ├── AnimatedCandle.kt            (ציור Canvas + flicker אנימציה)
    ├── ShabbatTimesDialog.kt        (ℹ️ לשוניות: זמני שבת + זמנים נוספים)
    ├── SettingsDialog.kt            (⚙️ + sub-views: צליל/תזכורת)
    ├── BatteryOptimizationCard.kt   (כרטיסיית אזהרה)
    ├── TonePreview.kt               (MediaPlayer נגן מקדים של 5 שניות)
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
