package com.nour.app.prayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Reprogramme l'alarme après un redémarrage ou un changement d'heure / de fuseau. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        PrayerAlarmScheduler.scheduleNext(context)
    }
}
