package com.example.prayer

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.local.PrayerAlarmEntity
import com.example.data.local.UserProfileEntity
import java.util.Calendar

object PrayerAlarmScheduler {

    const val CHANNEL_ID_PRAYER = "dailypath_prayer_alarms"
    const val EXTRA_PRAYER_NAME = "extra_prayer_name"
    const val EXTRA_SOUND_MODE = "extra_sound_mode"
    const val EXTRA_CUSTOM_SOUND_URI = "extra_custom_sound_uri"
    const val EXTRA_CUSTOM_LABEL = "extra_custom_label"
    const val EXTRA_VIBRATE = "extra_vibrate"
    const val EXTRA_REMINDER_MINUTES = "extra_reminder_minutes"

    fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val existing = manager.getNotificationChannel(CHANNEL_ID_PRAYER)
            if (existing == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID_PRAYER,
                    "Prayer Reminders & Alarms",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Daily Islamic prayer notifications and custom Adhan alerts"
                    enableVibration(true)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    fun canScheduleExactAlarms(context: Context): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun triggerTestAlarm(context: Context, config: PrayerAlarmEntity) {
        ensureNotificationChannel(context)
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            putExtra(EXTRA_PRAYER_NAME, "${config.prayerName} (Test)")
            putExtra(EXTRA_SOUND_MODE, config.soundMode)
            putExtra(EXTRA_CUSTOM_SOUND_URI, config.customSoundUri)
            putExtra(EXTRA_CUSTOM_LABEL, config.customLabel)
            putExtra(EXTRA_VIBRATE, config.vibrationEnabled)
            putExtra(EXTRA_REMINDER_MINUTES, config.reminderMinutesBefore)
        }
        context.sendBroadcast(intent)
    }

    fun scheduleAllAlarms(
        context: Context,
        profile: UserProfileEntity,
        alarms: List<PrayerAlarmEntity>
    ) {
        ensureNotificationChannel(context)
        val schedule = PrayerTimeCalculator.calculateSchedule(
            latitude = profile.latitude,
            longitude = profile.longitude,
            method = CalculationMethod.fromKey(profile.calculationMethod),
            asrMethod = AsrMethod.fromKey(profile.asrMethod)
        )

        val alarmMap = alarms.associateBy { it.prayerName }
        val now = System.currentTimeMillis()

        schedule.slots.filter { it.isObligatory }.forEachIndexed { index, slot ->
            val config = alarmMap[slot.name]
            val requestCode = 1000 + index
            if (config != null && config.enabled) {
                val baseTriggerMillis = if (config.useCustomTime && config.customTime24.contains(":")) {
                    parseTimeTodayOrTomorrow(config.customTime24, now)
                } else {
                    slot.epochMillis
                }
                var triggerAt = baseTriggerMillis - (config.reminderMinutesBefore * 60_000L)
                if (triggerAt <= now) {
                    triggerAt += 24 * 60 * 60 * 1000L
                }
                triggerAt = adjustForRepeatDays(triggerAt, config.repeatDaysCsv)
                scheduleSingle(context, requestCode, slot.name, triggerAt, config)
            } else {
                cancelSingle(context, requestCode)
            }
        }

        // Tahajjud reminder
        val tahajjudCode = 1099
        if (profile.tahajjudEnabled) {
            val triggerAt = parseTimeTodayOrTomorrow(profile.tahajjudTime, now)
            val config = alarmMap["Tahajjud"] ?: PrayerAlarmEntity(
                prayerName = "Tahajjud",
                enabled = true,
                reminderMinutesBefore = 0
            )
            scheduleSingle(context, tahajjudCode, "Tahajjud", triggerAt, config)
        } else {
            cancelSingle(context, tahajjudCode)
        }
    }

    private fun parseTimeTodayOrTomorrow(time24: String, nowMillis: Long): Long {
        val parts = time24.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 5
        val minute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= nowMillis) add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }

    private fun adjustForRepeatDays(triggerAtMillis: Long, repeatDaysCsv: String): Long {
        val allowedDays = repeatDaysCsv.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        if (allowedDays.isEmpty() || allowedDays.size == 7) return triggerAtMillis
        val cal = Calendar.getInstance().apply { timeInMillis = triggerAtMillis }
        repeat(7) {
            if (cal.get(Calendar.DAY_OF_WEEK) in allowedDays) return cal.timeInMillis
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return triggerAtMillis
    }

    private fun scheduleSingle(
        context: Context,
        requestCode: Int,
        prayerName: String,
        triggerAtMillis: Long,
        config: PrayerAlarmEntity
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            putExtra(EXTRA_PRAYER_NAME, prayerName)
            putExtra(EXTRA_SOUND_MODE, config.soundMode)
            putExtra(EXTRA_CUSTOM_SOUND_URI, config.customSoundUri)
            putExtra(EXTRA_CUSTOM_LABEL, config.customLabel)
            putExtra(EXTRA_VIBRATE, config.vibrationEnabled)
            putExtra(EXTRA_REMINDER_MINUTES, config.reminderMinutesBefore)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (canScheduleExactAlarms(context)) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun cancelSingle(context: Context, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PrayerAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}
