package com.nour.app.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nour.app.data.AppSettings
import com.nour.app.data.LocationHelper
import com.nour.app.prayer.HijriDate
import com.nour.app.prayer.Method
import com.nour.app.prayer.PrayerCalculator
import com.nour.app.quran.Ayah
import com.nour.app.quran.QuranRepository
import com.nour.app.quran.Surahs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
private val dateFormat = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH)

@Composable
fun PrayerScreen(modifier: Modifier, settingsVersion: Int, onSettingsChanged: () -> Unit) {
    val context = LocalContext.current
    val settings = remember { AppSettings(context) }
    val scope = rememberCoroutineScope()

    var now by remember { mutableStateOf(ZonedDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = ZonedDateTime.now()
            delay(1_000)
        }
    }

    val today = now.toLocalDate()
    val day = remember(settingsVersion, today) { PrayerCalculator.forDay(settings, today) }
    val next = remember(settingsVersion, now) { PrayerCalculator.nextPrayer(settings, now) }

    var locating by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        if (result.values.none { it }) {
            locationError = "Permission refusée : choisissez votre ville dans Réglages."
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            locating = true
            locationError = null
            val loc = LocationHelper.currentLocation(context)
            if (loc == null) {
                locationError = "Position introuvable. Activez la localisation et réessayez."
            } else {
                settings.latitude = loc.latitude
                settings.longitude = loc.longitude
                settings.cityName = LocationHelper.cityName(context, loc.latitude, loc.longitude)
                onSettingsChanged()
            }
            locating = false
        }
    }
    val locate = {
        permissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
        )
    }

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // En-tête façon écran de mosquée : ville, dates et compte à rebours.
        Box(
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(Emerald, EmeraldLight)),
                    RoundedCornerShape(24.dp),
                )
                .padding(20.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(settings.cityName, color = Color.White, style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    today.format(dateFormat).replaceFirstChar { it.uppercase() },
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(HijriDate.format(today), color = Gold, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
                Text(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")), color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Light)
                Spacer(Modifier.height(8.dp))
                Text(
                    "${next.name.label} dans ${formatCountdown(Duration.between(now, next.time))}",
                    color = Gold,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }

        if (!settings.hasLocation || locationError != null || locating) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        locationError ?: "Horaires calculés pour Paris par défaut. Localisez-vous pour des horaires précis.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    if (locating) {
                        CircularProgressIndicator(Modifier.size(24.dp))
                    } else {
                        Button(onClick = locate) { Text("Me localiser") }
                    }
                }
            }
        }

        // Liste des horaires du jour.
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            Column(Modifier.padding(vertical = 8.dp)) {
                day.times.forEach { pt ->
                    val isNext = pt.name == next.name && pt.time.toLocalDate() == next.time.toLocalDate()
                    val passed = pt.time.isBefore(now) && !isNext
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                            .background(
                                if (isNext) MaterialTheme.colorScheme.primary else Color.Transparent,
                                RoundedCornerShape(14.dp),
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val color = when {
                            isNext -> MaterialTheme.colorScheme.onPrimary
                            passed -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                        Text(pt.name.label, color = color, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Text(pt.name.arabic, color = color, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                        Text(pt.time.format(timeFormat), color = color, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        if (today.dayOfWeek == DayOfWeek.FRIDAY) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Text(
                    "Jumu'a Moubaraka ! N'oubliez pas la sourate Al-Kahf aujourd'hui.",
                    Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        VerseOfTheDayCard()

        Text(
            "Méthode : ${Method.fromId(settings.method).label} · Asr ${if (settings.hanafiAsr) "hanafite" else "standard"}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        if (settings.hasLocation && !locating) {
            TextButton(onClick = locate) { Text("Mettre à jour ma position") }
        }
    }
}

@Composable
private fun VerseOfTheDayCard() {
    val context = LocalContext.current
    val repo = remember { QuranRepository(context) }
    var ayah by remember { mutableStateOf<Ayah?>(null) }
    var failed by remember { mutableStateOf(false) }
    var attempt by remember { mutableStateOf(0) }

    LaunchedEffect(attempt) {
        failed = false
        ayah = runCatching { repo.verseOfTheDay() }.getOrNull()
        failed = ayah == null
    }

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(16.dp)) {
            Text("Verset du jour", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            val a = ayah
            when {
                a != null -> {
                    Text(
                        a.arabic,
                        style = MaterialTheme.typography.headlineSmall.copy(textDirection = TextDirection.Rtl),
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth(),
                        lineHeight = 40.sp,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(a.translation, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${Surahs.get(a.surah).name}, verset ${a.number}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                failed -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Connexion nécessaire pour le verset du jour.", Modifier.weight(1f))
                    TextButton(onClick = { attempt++ }) { Text("Réessayer") }
                }
                else -> CircularProgressIndicator(Modifier.size(24.dp))
            }
        }
    }
}

private fun formatCountdown(d: Duration): String {
    val total = d.seconds.coerceAtLeast(0)
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) String.format(Locale.FRANCE, "%dh %02dmin", h, m)
    else String.format(Locale.FRANCE, "%02d:%02d", m, s)
}
