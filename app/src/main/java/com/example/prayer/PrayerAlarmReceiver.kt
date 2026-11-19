package com.example.prayer

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        PrayerAlarmScheduler.ensureNotificationChannel(context)

        val prayerName = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_PRAYER_NAME) ?: "Prayer"
        val soundMode = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_SOUND_MODE) ?: "NOTIFICATION"
        val customSoundUriStr = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_CUSTOM_SOUND_URI).orEmpty()
        val customLabel = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_CUSTOM_LABEL).orEmpty()
        val vibrate = intent.getBooleanExtra(PrayerAlarmScheduler.EXTRA_VIBRATE, true)
        val minutesBefore = intent.getIntExtra(PrayerAlarmScheduler.EXTRA_REMINDER_MINUTES, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            prayerName.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = when {
            customLabel.isNotBlank() -> "$prayerName — $customLabel"
            minutesBefore > 0 -> "$prayerName in $minutesBefore minutes"
            else -> "Time for $prayerName"
        }
        val body = when (soundMode) {
            "ADHAN" -> "Adhan alert active. Prepare for $prayerName prayer."
            "CUSTOM" -> "Your custom alarm sound is active for $prayerName."
            else -> "DailyPath prayer reminder for $prayerName."
        }

        val soundUri: Uri? = when {
            soundMode == "CUSTOM" && customSoundUriStr.isNotBlank() -> runCatching { Uri.parse(customSoundUriStr) }.getOrNull()
            soundMode == "ADHAN" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }

        // Play custom or alarm ringtone briefly if selected
        if (soundUri != null) {
            runCatching {
                val ringtone = RingtoneManager.getRingtone(context, soundUri)
                ringtone?.play()
            }
        }

        val builder = NotificationCompat.Builder(context, PrayerAlarmScheduler.CHANNEL_ID_PRAYER)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(contentPendingIntent)

        if (vibrate) {
            builder.setVibrate(longArrayOf(0L, 400L, 250L, 400L))
        }

        NotificationManagerCompat.from(context).notify(prayerName.hashCode(), builder.build())
    }
}
