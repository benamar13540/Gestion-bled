package com.nour.app.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

/** Localisation sans Google Play Services, via le LocationManager du système. */
object LocationHelper {

    /** Nécessite que la permission de localisation ait été accordée. */
    @SuppressLint("MissingPermission")
    suspend fun currentLocation(context: Context): Location? {
        val lm = context.getSystemService(LocationManager::class.java) ?: return null
        val providers = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
            .filter { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) }

        val fresh = withTimeoutOrNull(15_000) {
            providers.firstNotNullOfOrNull { provider -> singleUpdate(lm, provider) }
        }
        if (fresh != null) return fresh

        return lm.allProviders
            .mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }
    }

    @SuppressLint("MissingPermission")
    private suspend fun singleUpdate(lm: LocationManager, provider: String): Location? =
        suspendCancellableCoroutine { cont ->
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    lm.removeUpdates(this)
                    if (cont.isActive) cont.resume(location)
                }

                override fun onProviderDisabled(provider: String) {
                    lm.removeUpdates(this)
                    if (cont.isActive) cont.resume(null)
                }

                // Méthodes abstraites avant Android 11 : à implémenter explicitement.
                override fun onProviderEnabled(provider: String) {}

                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
            }
            try {
                lm.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
            } catch (e: Exception) {
                cont.resume(null)
                return@suspendCancellableCoroutine
            }
            cont.invokeOnCancellation { lm.removeUpdates(listener) }
        }

    /** Nom de la ville (ou coordonnées si le géocodeur n'est pas disponible). */
    @Suppress("DEPRECATION")
    suspend fun cityName(context: Context, lat: Double, lng: Double): String =
        withContext(Dispatchers.IO) {
            val fallback = String.format(Locale.FRANCE, "%.3f, %.3f", lat, lng)
            if (!Geocoder.isPresent()) return@withContext fallback
            try {
                val address = Geocoder(context, Locale.FRENCH).getFromLocation(lat, lng, 1)?.firstOrNull()
                address?.locality ?: address?.subAdminArea ?: address?.adminArea ?: fallback
            } catch (e: Exception) {
                fallback
            }
        }

    /** Recherche manuelle d'une ville (utile si le GPS est désactivé). */
    @Suppress("DEPRECATION")
    suspend fun searchCity(context: Context, query: String): Triple<String, Double, Double>? =
        withContext(Dispatchers.IO) {
            if (!Geocoder.isPresent()) return@withContext null
            try {
                val a = Geocoder(context, Locale.FRENCH).getFromLocationName(query, 1)?.firstOrNull()
                    ?: return@withContext null
                Triple(a.locality ?: a.featureName ?: query, a.latitude, a.longitude)
            } catch (e: Exception) {
                null
            }
        }
}
