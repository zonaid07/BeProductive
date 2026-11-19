package com.example.prayer

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

data class CityOption(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
) {
    val displayName: String get() = "$name, $country"
}

enum class CalculationMethod(
    val key: String,
    val displayName: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaIntervalMinutes: Int? = null
) {
    KARACHI("KARACHI", "University of Islamic Sciences, Karachi", 18.0, 18.0),
    MWL("MWL", "Muslim World League", 18.0, 17.0),
    ISNA("ISNA", "Islamic Society of North America", 15.0, 15.0),
    UMM_AL_QURA("UMM_AL_QURA", "Umm al-Qura University, Makkah", 18.5, 0.0, 90),
    EGYPT("EGYPT", "Egyptian General Authority", 19.5, 17.5);

    companion object {
        fun fromKey(key: String): CalculationMethod =
            entries.firstOrNull { it.key == key } ?: KARACHI
    }
}

enum class AsrMethod(val key: String, val displayName: String, val shadowFactor: Double) {
    STANDARD("STANDARD", "Standard (Shafi'i, Maliki, Hanbali)", 1.0),
    HANAFI("HANAFI", "Hanafi (Shadow factor 2x)", 2.0);

    companion object {
        fun fromKey(key: String): AsrMethod =
            entries.firstOrNull { it.key == key } ?: HANAFI
    }
}

data class PrayerTimeSlot(
    val name: String,
    val nameBn: String,
    val time24: String,
    val time12: String,
    val epochMillis: Long,
    val isObligatory: Boolean = true
)

data class DailyPrayerSchedule(
    val slots: List<PrayerTimeSlot>,
    val nextPrayer: PrayerTimeSlot,
    val countdownText: String
)

object PrayerTimeCalculator {

    val popularCities = listOf(
        CityOption("Dhaka", "Bangladesh", 23.8103, 90.4125),
        CityOption("Chattogram", "Bangladesh", 22.3569, 91.7832),
        CityOption("Sylhet", "Bangladesh", 24.8949, 91.8687),
        CityOption("Rajshahi", "Bangladesh", 24.3745, 88.6042),
        CityOption("Khulna", "Bangladesh", 22.8456, 89.5403),
        CityOption("Barishal", "Bangladesh", 22.7010, 90.3535),
        CityOption("Rangpur", "Bangladesh", 25.7439, 89.2752),
        CityOption("Mymensingh", "Bangladesh", 24.7471, 90.4203),
        CityOption("Makkah", "Saudi Arabia", 21.4225, 39.8262),
        CityOption("Madinah", "Saudi Arabia", 24.4672, 39.6112),
        CityOption("London", "United Kingdom", 51.5074, -0.1278),
        CityOption("New York", "USA", 40.7128, -74.0060),
        CityOption("Kuala Lumpur", "Malaysia", 3.1390, 101.6869)
    )

    fun calculateSchedule(
        latitude: Double,
        longitude: Double,
        method: CalculationMethod,
        asrMethod: AsrMethod,
        nowCalendar: Calendar = Calendar.getInstance()
    ): DailyPrayerSchedule {
        val year = nowCalendar.get(Calendar.YEAR)
        val month = nowCalendar.get(Calendar.MONTH) + 1
        val day = nowCalendar.get(Calendar.DAY_OF_MONTH)
        val tzHours = TimeZone.getDefault().getOffset(nowCalendar.timeInMillis) / 3_600_000.0

        val jd = julianDate(year, month, day) - longitude / (15.0 * 24.0)
        val sunPos = sunPosition(jd + 0.5)
        val decl = sunPos.first
        val eqTime = sunPos.second

        val dhuhrHours = fixHour(12.0 + tzHours - longitude / 15.0 - eqTime)
        val sunriseHours = dhuhrHours - sunAngleTime(0.833, latitude, decl)
        val maghribHours = dhuhrHours + sunAngleTime(0.833, latitude, decl)
        val fajrHours = dhuhrHours - sunAngleTime(method.fajrAngle, latitude, decl)
        val asrHours = dhuhrHours + asrTime(asrMethod.shadowFactor, latitude, decl)
        val ishaHours = if (method.ishaIntervalMinutes != null) {
            maghribHours + (method.ishaIntervalMinutes / 60.0)
        } else {
            dhuhrHours + sunAngleTime(method.ishaAngle, latitude, decl)
        }

        val slots = listOf(
            buildSlot("Fajr", "ফজর", fajrHours, nowCalendar, true),
            buildSlot("Sunrise", "সূর্যোদয়", sunriseHours, nowCalendar, false),
            buildSlot("Dhuhr", "যোহর", dhuhrHours, nowCalendar, true),
            buildSlot("Asr", "আসর", asrHours, nowCalendar, true),
            buildSlot("Maghrib", "মাগরিব", maghribHours, nowCalendar, true),
            buildSlot("Isha", "ইশা", ishaHours, nowCalendar, true)
        )

        val obligatory = slots.filter { it.isObligatory }
        val nowMillis = nowCalendar.timeInMillis
        val upcoming = obligatory.firstOrNull { it.epochMillis > nowMillis }
        val nextPrayer = if (upcoming != null) {
            upcoming
        } else {
            val tomorrow = (nowCalendar.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
            val tomorrowFajr = buildSlot("Fajr", "ফজর", fajrHours, tomorrow, true)
            tomorrowFajr
        }

        val diffMillis = (nextPrayer.epochMillis - nowMillis).coerceAtLeast(0L)
        val totalMinutes = diffMillis / 60_000L
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60
        val countdown = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

        return DailyPrayerSchedule(
            slots = slots,
            nextPrayer = nextPrayer,
            countdownText = countdown
        )
    }

    private fun buildSlot(
        name: String,
        nameBn: String,
        hoursFloat: Double,
        baseCal: Calendar,
        isObligatory: Boolean
    ): PrayerTimeSlot {
        val fixed = fixHour(hoursFloat)
        val totalMinutes = kotlin.math.round(fixed * 60.0).toInt()
        val hour24 = (totalMinutes / 60) % 24
        val minute = totalMinutes % 60

        val slotCal = (baseCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour24)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val time24 = "%02d:%02d".format(hour24, minute)
        val amPm = if (hour24 >= 12) "PM" else "AM"
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        val time12 = "%d:%02d %s".format(hour12, minute, amPm)

        return PrayerTimeSlot(
            name = name,
            nameBn = nameBn,
            time24 = time24,
            time12 = time12,
            epochMillis = slotCal.timeInMillis,
            isObligatory = isObligatory
        )
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun sunPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * dSin(g) + 0.020 * dSin(2 * g))
        val e = 23.439 - 0.00000036 * d
        val ra = dAtan2(dCos(e) * dSin(l), dCos(l)) / 15.0
        val eqTime = q / 15.0 - fixHour(ra)
        val decl = dAsin(dSin(e) * dSin(l))
        return decl to eqTime
    }

    private fun sunAngleTime(angle: Double, lat: Double, decl: Double): Double {
        val numerator = -dSin(angle) - dSin(lat) * dSin(decl)
        val denominator = dCos(lat) * dCos(decl)
        val ratio = (numerator / denominator).coerceIn(-1.0, 1.0)
        return dAcos(ratio) / 15.0
    }

    private fun asrTime(shadowFactor: Double, lat: Double, decl: Double): Double {
        val angle = -dAcot(shadowFactor + dTan(kotlin.math.abs(lat - decl)))
        return sunAngleTime(angle, lat, decl)
    }

    private fun dSin(d: Double) = sin(Math.toRadians(d))
    private fun dCos(d: Double) = cos(Math.toRadians(d))
    private fun dTan(d: Double) = tan(Math.toRadians(d))
    private fun dAsin(x: Double) = Math.toDegrees(asin(x))
    private fun dAcos(x: Double) = Math.toDegrees(acos(x))
    private fun dAtan2(y: Double, x: Double) = Math.toDegrees(atan2(y, x))
    private fun dAcot(x: Double) = Math.toDegrees(atan(1.0 / x))

    private fun fixAngle(a: Double): Double {
        val res = a - 360.0 * floor(a / 360.0)
        return if (res < 0) res + 360.0 else res
    }

    private fun fixHour(a: Double): Double {
        val res = a - 24.0 * floor(a / 24.0)
        return if (res < 0) res + 24.0 else res
    }
}
