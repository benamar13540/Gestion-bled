package com.nour.app.prayer

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.nour.app.MainActivity
import com.nour.app.R
import com.nour.app.data.AppSettings

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayer = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_PRAYER)
            ?.let { runCatching { PrayerName.valueOf(it) }.getOrNull() }

        if (prayer != null && AppSettings(context).notificationsEnabled && canNotify(context)) {
            PrayerAlarmScheduler.createChannel(context)
            val open = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE,
            )
            val notification = NotificationCompat.Builder(context, PrayerAlarmScheduler.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("C'est l'heure de ${prayer.label} · ${prayer.arabic}")
                .setContentText("حي على الصلاة — Venez à la prière")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(open)
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(context).notify(prayer.ordinal, notification)
        }
        PrayerAlarmScheduler.scheduleNext(context)
    }

    private fun canNotify(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
}
