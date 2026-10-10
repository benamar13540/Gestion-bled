package com.nour.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.nour.app.data.AppSettings
import com.nour.app.prayer.PrayerAlarmScheduler
import com.nour.app.ui.NourTheme
import com.nour.app.ui.PrayerScreen
import com.nour.app.ui.QiblaScreen
import com.nour.app.ui.QuranScreen
import com.nour.app.ui.SettingsScreen

private enum class Tab(val label: String, val icon: ImageVector) {
    PRAYERS("Prières", Icons.Filled.DateRange),
    QURAN("Coran", Icons.AutoMirrored.Filled.List),
    QIBLA("Qibla", Icons.Filled.Place),
    SETTINGS("Réglages", Icons.Filled.Settings),
}

class MainActivity : ComponentActivity() {

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            PrayerAlarmScheduler.scheduleNext(this)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        PrayerAlarmScheduler.createChannel(this)
        PrayerAlarmScheduler.scheduleNext(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && AppSettings(this).notificationsEnabled) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            NourTheme {
                var tab by rememberSaveable { mutableIntStateOf(0) }
                // Incrémenté à chaque changement de réglage pour rafraîchir les écrans.
                var settingsVersion by rememberSaveable { mutableIntStateOf(0) }
                val onSettingsChanged = {
                    settingsVersion++
                    PrayerAlarmScheduler.scheduleNext(this)
                }

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            Tab.entries.forEachIndexed { index, t ->
                                NavigationBarItem(
                                    selected = tab == index,
                                    onClick = { tab = index },
                                    icon = { Icon(t.icon, contentDescription = t.label) },
                                    label = { Text(t.label) },
                                )
                            }
                        }
                    },
                ) { padding ->
                    val modifier = Modifier.padding(padding)
                    when (Tab.entries[tab]) {
                        Tab.PRAYERS -> PrayerScreen(modifier, settingsVersion, onSettingsChanged)
                        Tab.QURAN -> QuranScreen(modifier, settingsVersion)
                        Tab.QIBLA -> QiblaScreen(modifier, settingsVersion)
                        Tab.SETTINGS -> SettingsScreen(modifier, onSettingsChanged)
                    }
                }
            }
        }
    }
}
