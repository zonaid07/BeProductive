package com.example.prayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.DailyPathDatabase
import com.example.data.local.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PrayerBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = DailyPathDatabase.getInstance(context)
                    val profile = db.userProfileDao().getProfileOnce() ?: UserProfileEntity()
                    val alarms = db.prayerAlarmDao().getAlarmsOnce()
                    PrayerAlarmScheduler.scheduleAllAlarms(context, profile, alarms)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
