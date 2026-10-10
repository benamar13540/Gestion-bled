package com.nour.app.ui

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nour.app.data.AppSettings
import com.nour.app.prayer.PrayerCalculator
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun QiblaScreen(modifier: Modifier, settingsVersion: Int) {
    val context = LocalContext.current
    val settings = remember { AppSettings(context) }
    val qibla = remember(settingsVersion) {
        PrayerCalculator.qiblaDirection(settings.latitude, settings.longitude).toFloat()
    }
    val declination = remember(settingsVersion) {
        GeomagneticField(
            settings.latitude.toFloat(),
            settings.longitude.toFloat(),
            0f,
            System.currentTimeMillis(),
        ).declination
    }

    var heading by remember { mutableFloatStateOf(0f) }
    var hasSensor by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (sensor == null) {
            hasSensor = false
            onDispose { }
        } else {
            val rotation = FloatArray(9)
            val orientation = FloatArray(3)
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    SensorManager.getRotationMatrixFromVector(rotation, event.values)
                    SensorManager.getOrientation(rotation, orientation)
                    val magnetic = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    // Cap par rapport au nord géographique (correction de la déclinaison magnétique),
                    // gardé « continu » (sans saut 359° → 0°) pour que l'animation prenne le chemin court.
                    val fresh = magnetic + declination
                    heading += ((fresh - heading) % 360f + 540f) % 360f - 180f
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
            onDispose { sm.unregisterListener(listener) }
        }
    }

    // Angle de l'aiguille relatif au haut du téléphone.
    val needle by animateFloatAsState(qibla - heading, label = "needle")
    val dialAngle by animateFloatAsState(-heading, label = "dial")

    val diff = abs(((qibla - heading) % 360f + 540f) % 360f - 180f)
    val aligned = hasSensor && diff < 5f

    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val ringColor = if (aligned) Gold else primary

    Column(
        modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Direction de la Qibla", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "${qibla.roundToInt()}° depuis le nord · ${settings.cityName}",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(24.dp))

        Canvas(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        ) {
            val r = size.minDimension / 2f
            val c = center
            drawCircle(ringColor, radius = r * 0.95f, style = Stroke(width = r * 0.04f))

            // Cadran gradué qui tourne avec le téléphone (N en rouge).
            rotate(dialAngle, c) {
                for (i in 0 until 72) {
                    val len = if (i % 18 == 0) 0.14f else if (i % 2 == 0) 0.08f else 0.04f
                    rotate(i * 5f, c) {
                        drawLine(
                            if (i == 0) androidx.compose.ui.graphics.Color.Red else onSurface.copy(alpha = 0.6f),
                            Offset(c.x, c.y - r * 0.9f),
                            Offset(c.x, c.y - r * (0.9f - len)),
                            strokeWidth = if (i % 18 == 0) 6f else 3f,
                        )
                    }
                }
            }

            // Aiguille pointant vers La Mecque, avec la Kaaba au bout.
            rotate(needle, c) {
                val path = Path().apply {
                    moveTo(c.x, c.y - r * 0.7f)
                    lineTo(c.x - r * 0.08f, c.y)
                    lineTo(c.x, c.y + r * 0.12f)
                    lineTo(c.x + r * 0.08f, c.y)
                    close()
                }
                drawPath(path, ringColor)
                val k = r * 0.09f
                drawRect(
                    androidx.compose.ui.graphics.Color(0xFF1C1B17),
                    topLeft = Offset(c.x - k, c.y - r * 0.7f - 2.2f * k),
                    size = androidx.compose.ui.geometry.Size(2 * k, 2 * k),
                )
                drawRect(
                    Gold,
                    topLeft = Offset(c.x - k, c.y - r * 0.7f - 1.7f * k),
                    size = androidx.compose.ui.geometry.Size(2 * k, k * 0.3f),
                )
            }
            drawCircle(ringColor, radius = r * 0.05f, center = c)
        }

        Spacer(Modifier.height(24.dp))
        Text(
            when {
                !hasSensor -> "Boussole indisponible sur cet appareil. Orientez-vous à ${qibla.roundToInt()}° du nord."
                aligned -> "Vous faites face à la Qibla ✓"
                else -> "Tournez-vous jusqu'à ce que l'aiguille pointe vers le haut."
            },
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            color = if (aligned) primary else onSurface,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Éloignez le téléphone des objets métalliques. Si la boussole semble fausse, dessinez un 8 dans l'air pour la calibrer.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            color = onSurface.copy(alpha = 0.6f),
        )
    }
}
