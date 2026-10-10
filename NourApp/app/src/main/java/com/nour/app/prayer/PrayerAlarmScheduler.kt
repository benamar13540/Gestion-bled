package com.nour.app.prayer

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.nour.app.data.AppSettings

/**
 * Programme une alarme pour la prochaine prière uniquement ; quand elle sonne,
 * [PrayerAlarmReceiver] affiche la notification puis programme la suivante.
 */
object PrayerAlarmScheduler {

    const val CHANNEL_ID = "adhan"
    const val EXTRA_PRAYER = "prayer"
    private const val REQUEST_CODE = 4242

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Appel à la prière",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply { description = "Notification à l'heure de chaque prière" }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun scheduleNext(context: Context) {
        val settings = AppSettings(context)
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pending = pendingIntent(context, null)
        alarmManager.cancel(pending)
        if (!settings.notificationsEnabled) return

        val next = PrayerCalculator.nextPrayer(settings)
        val triggerAt = next.time.toInstant().toEpochMilli()
        val intent = pendingIntent(context, next.name)

        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, intent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, intent)
        }
    }

    fun canScheduleExact(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

    private fun pendingIntent(context: Context, prayer: PrayerName?): PendingIntent {
        val intent = Intent(context, PrayerAlarmReceiver::class.java)
        if (prayer != null) intent.putExtra(EXTRA_PRAYER, prayer.name)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
