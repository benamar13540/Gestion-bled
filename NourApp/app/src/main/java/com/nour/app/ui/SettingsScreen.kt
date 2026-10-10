package com.nour.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nour.app.data.AppSettings
import com.nour.app.data.LocationHelper
import com.nour.app.prayer.Method
import com.nour.app.prayer.PrayerAlarmScheduler
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(modifier: Modifier, onSettingsChanged: () -> Unit) {
    val context = LocalContext.current
    val settings = remember { AppSettings(context) }
    val scope = rememberCoroutineScope()

    var method by remember { mutableStateOf(settings.method) }
    var hanafi by remember { mutableStateOf(settings.hanafiAsr) }
    var notifications by remember { mutableStateOf(settings.notificationsEnabled) }
    var arabicSize by remember { mutableFloatStateOf(settings.arabicTextSize) }
    var translation by remember { mutableStateOf(settings.showTranslation) }
    var city by remember { mutableStateOf(settings.cityName) }
    var cityQuery by remember { mutableStateOf("") }
    var cityMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Réglages", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Section("Ville") {
            Text("Actuelle : $city", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = cityQuery,
                onValueChange = { cityQuery = it },
                placeholder = { Text("Ex : Marseille, Alger, Casablanca…") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Button(
                enabled = cityQuery.isNotBlank(),
                onClick = {
                    scope.launch {
                        val found = LocationHelper.searchCity(context, cityQuery)
                        if (found == null) {
                            cityMessage = "Ville introuvable (connexion internet requise)."
                        } else {
                            settings.cityName = found.first
                            settings.latitude = found.second
                            settings.longitude = found.third
                            city = found.first
                            cityQuery = ""
                            cityMessage = null
                            onSettingsChanged()
                        }
                    }
                },
            ) { Text("Rechercher") }
            cityMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }

        Section("Méthode de calcul") {
            Method.entries.forEach { m ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            method = m.id
                            settings.method = m.id
                            onSettingsChanged()
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = method == m.id, onClick = null)
                    Text(m.label, Modifier.padding(start = 8.dp, top = 10.dp, bottom = 10.dp))
                }
            }
        }

        Section("Asr") {
            SwitchRow("Méthode hanafite (ombre ×2)", hanafi) {
                hanafi = it
                settings.hanafiAsr = it
                onSettingsChanged()
            }
        }

        Section("Notifications") {
            SwitchRow("Notification à chaque prière", notifications) {
                notifications = it
                settings.notificationsEnabled = it
                onSettingsChanged()
            }
            if (notifications && !PrayerAlarmScheduler.canScheduleExact(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Pour être notifié à la minute près, autorisez les alarmes exactes.",
                    style = MaterialTheme.typography.bodySmall,
                )
                OutlinedButton(onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}")),
                    )
                }) { Text("Autoriser les alarmes exactes") }
            }
        }

        Section("Lecture du Coran") {
            Text("Taille du texte arabe", style = MaterialTheme.typography.bodyMedium)
            Slider(
                value = arabicSize,
                onValueChange = { arabicSize = it },
                onValueChangeFinished = {
                    settings.arabicTextSize = arabicSize
                    onSettingsChanged()
                },
                valueRange = 18f..42f,
            )
            Text("بِسْمِ ٱللَّهِ", fontSize = arabicSize.sp, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            SwitchRow("Afficher la traduction française", translation) {
                translation = it
                settings.showTranslation = it
                onSettingsChanged()
            }
        }

        Text(
            "Nour · Horaires calculés localement (bibliothèque Adhan). Texte du Coran et traduction de M. Hamidullah fournis par alquran.cloud.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
