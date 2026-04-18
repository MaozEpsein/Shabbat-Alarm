package com.example.shabbatalarm.alarm

import android.util.Log
import com.kosherjava.zmanim.ComplexZmanimCalendar
import com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import com.kosherjava.zmanim.util.GeoLocation
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * One of the major Israeli cities plus its standard candle-lighting offset (minutes before sunset).
 * Jerusalem uses 40 min, Haifa 30 min; most cities follow the standard 18 min.
 */
data class IsraeliCity(
    val nameHe: String,
    val nameEn: String,
    val latitude: Double,
    val longitude: Double,
    val elevation: Double,
    val candleLightingOffsetMinutes: Int
)

data class ShabbatTimes(
    val city: IsraeliCity,
    val candleLighting: String, // "HH:mm"
    val havdalah: String        // "HH:mm"
)

object ShabbatTimesCalculator {

    private const val TAG = "ShabbatTimes"

    val CITIES: List<IsraeliCity> = listOf(
        IsraeliCity("ירושלים", "Jerusalem", 31.7683, 35.2137, 800.0, 40),
        IsraeliCity("תל אביב", "Tel Aviv", 32.0853, 34.7818, 34.0, 18),
        IsraeliCity("חיפה", "Haifa", 32.7940, 34.9896, 250.0, 30),
        IsraeliCity("באר שבע", "Beer Sheva", 31.2518, 34.7913, 280.0, 18),
        IsraeliCity("אילת", "Eilat", 29.5581, 34.9482, 12.0, 18),
        IsraeliCity("טבריה", "Tiberias", 32.7959, 35.5308, 40.0, 18),
        IsraeliCity("נתניה", "Netanya", 32.3328, 34.8599, 33.0, 18),
        IsraeliCity("אשדוד", "Ashdod", 31.8044, 34.6553, 50.0, 18),
    )

    private val TIMEZONE: TimeZone = TimeZone.getTimeZone("Asia/Jerusalem")

    /**
     * Returns candle-lighting and Havdalah times for the relevant Shabbat:
     * - Sunday through Thursday: the upcoming Friday.
     * - Friday: today.
     * - Saturday: yesterday's Friday (i.e. the Shabbat we're currently in).
     * The reset to the next Shabbat happens when Sunday begins.
     */
    fun calculateForUpcomingShabbat(): ShabbatResult {
        val friday = nextFriday()
        val saturday = Calendar.getInstance(TIMEZONE).apply {
            time = friday
            add(Calendar.DAY_OF_MONTH, 1)
        }.time

        val times = CITIES.map { city -> calculateForCity(city, friday, saturday) }
        return ShabbatResult(fridayDate = friday, times = times)
    }

    private fun calculateForCity(
        city: IsraeliCity,
        friday: Date,
        saturday: Date
    ): ShabbatTimes {
        return try {
            // KosherJava rejects negative elevations; clamp defensively.
            val safeElevation = city.elevation.coerceAtLeast(0.0)
            val location = GeoLocation(
                city.nameEn, city.latitude, city.longitude, safeElevation, TIMEZONE
            )

            val fridayCalc = ComplexZmanimCalendar(location).apply {
                calendar.time = friday
            }
            val saturdayCalc = ComplexZmanimCalendar(location).apply {
                calendar.time = saturday
            }

            val sunsetFriday = fridayCalc.sunset
            val candleLighting = sunsetFriday?.let {
                Date(it.time - city.candleLightingOffsetMinutes * 60_000L)
            }

            // Tzais (stars emerge) — 8.5° below horizon, standard for Havdalah in Israel
            val havdalah = saturdayCalc.tzais

            ShabbatTimes(
                city = city,
                candleLighting = formatTime(candleLighting),
                havdalah = formatTime(havdalah)
            )
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to compute Shabbat times for ${city.nameEn}", t)
            ShabbatTimes(city = city, candleLighting = "—", havdalah = "—")
        }
    }

    /**
     * Returns the candle-lighting moment for the given city and Friday, or null
     * if sunset cannot be computed.
     */
    fun computeCandleLighting(city: IsraeliCity, fridayDate: Date): Date? {
        return try {
            val safeElevation = city.elevation.coerceAtLeast(0.0)
            val location = GeoLocation(
                city.nameEn, city.latitude, city.longitude, safeElevation, TIMEZONE
            )
            val zmanim = ComplexZmanimCalendar(location).apply {
                calendar.time = fridayDate
            }
            val sunset = zmanim.sunset ?: return null
            Date(sunset.time - city.candleLightingOffsetMinutes * 60_000L)
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to compute candle lighting for ${city.nameEn}", t)
            null
        }
    }

    /**
     * Returns the next upcoming candle-lighting moment for the given city
     * (starts from today; if today is past, rolls forward by one week).
     */
    fun computeNextCandleLighting(city: IsraeliCity): Date? {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance(TIMEZONE).apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // Advance to the next Friday (or today if Friday).
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) {
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        // Try up to a year ahead to land on a future candle-lighting.
        repeat(53) {
            val lighting = computeCandleLighting(city, cal.time)
            if (lighting != null && lighting.time > now) return lighting
            cal.add(Calendar.DAY_OF_MONTH, 7)
        }
        return null
    }

    private fun nextFriday(): Date {
        val cal = Calendar.getInstance(TIMEZONE)
        val today = cal.get(Calendar.DAY_OF_WEEK)
        val daysOffset = if (today == Calendar.SATURDAY) {
            // We're currently in Shabbat — use yesterday's Friday so Havdalah shows today.
            -1
        } else {
            // Sunday–Thursday: days until next Friday. Friday: 0 (today).
            (Calendar.FRIDAY - today + 7) % 7
        }
        cal.add(Calendar.DAY_OF_MONTH, daysOffset)
        cal.set(Calendar.HOUR_OF_DAY, 12)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

    private fun formatTime(date: Date?): String {
        if (date == null) return "—"
        val formatter = SimpleDateFormat("HH:mm", Locale.US)
        formatter.timeZone = TIMEZONE
        return formatter.format(date)
    }

    data class ShabbatResult(
        val fridayDate: Date,
        val times: List<ShabbatTimes>
    ) {
        /**
         * Returns a combined Gregorian + Hebrew date string, e.g.
         * "יום שישי, 25 באפריל · י״א באייר התשפ״ו".
         */
        fun formatFridayDate(): String {
            val gregorian = formatGregorian()
            val hebrew = formatHebrew()
            return if (hebrew.isNotBlank()) "$gregorian · $hebrew" else gregorian
        }

        private fun formatGregorian(): String {
            val formatter = SimpleDateFormat("EEEE, d בMMMM", Locale("iw", "IL"))
            formatter.timeZone = TIMEZONE
            return formatter.format(fridayDate)
        }

        private fun formatHebrew(): String = try {
            val jewishCal = JewishCalendar(fridayDate)
            HebrewDateFormatter().apply { isHebrewFormat = true }.format(jewishCal)
        } catch (t: Throwable) {
            Log.e("ShabbatTimes", "Failed to format Hebrew date", t)
            ""
        }
    }
}
