package com.nour.app.data

import android.content.Context
import android.content.SharedPreferences

/** Préférences persistantes de l'application (position, méthode de calcul, lecture du Coran…). */
class AppSettings(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("nour_prefs", Context.MODE_PRIVATE)

    var latitude: Double
        get() = java.lang.Double.longBitsToDouble(prefs.getLong(KEY_LAT, DEFAULT_LAT.toRawBits()))
        set(value) = prefs.edit().putLong(KEY_LAT, value.toRawBits()).apply()

    var longitude: Double
        get() = java.lang.Double.longBitsToDouble(prefs.getLong(KEY_LNG, DEFAULT_LNG.toRawBits()))
        set(value) = prefs.edit().putLong(KEY_LNG, value.toRawBits()).apply()

    var cityName: String
        get() = prefs.getString(KEY_CITY, DEFAULT_CITY) ?: DEFAULT_CITY
        set(value) = prefs.edit().putString(KEY_CITY, value).apply()

    val hasLocation: Boolean get() = prefs.contains(KEY_LAT)

    var method: String
        get() = prefs.getString(KEY_METHOD, "UOIF") ?: "UOIF"
        set(value) = prefs.edit().putString(KEY_METHOD, value).apply()

    var hanafiAsr: Boolean
        get() = prefs.getBoolean(KEY_HANAFI, false)
        set(value) = prefs.edit().putBoolean(KEY_HANAFI, value).apply()

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIF, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIF, value).apply()

    var arabicTextSize: Float
        get() = prefs.getFloat(KEY_AR_SIZE, 26f)
        set(value) = prefs.edit().putFloat(KEY_AR_SIZE, value).apply()

    var showTranslation: Boolean
        get() = prefs.getBoolean(KEY_TRANSLATION, true)
        set(value) = prefs.edit().putBoolean(KEY_TRANSLATION, value).apply()

    var lastReadSurah: Int
        get() = prefs.getInt(KEY_LAST_SURAH, 0)
        set(value) = prefs.edit().putInt(KEY_LAST_SURAH, value).apply()

    var lastReadAyah: Int
        get() = prefs.getInt(KEY_LAST_AYAH, 1)
        set(value) = prefs.edit().putInt(KEY_LAST_AYAH, value).apply()

    /** Favoris stockés sous la forme "sourate:verset". */
    var bookmarks: Set<String>
        get() = prefs.getStringSet(KEY_BOOKMARKS, emptySet())?.toSet() ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_BOOKMARKS, value).apply()

    fun toggleBookmark(surah: Int, ayah: Int): Boolean {
        val key = "$surah:$ayah"
        val current = bookmarks
        val added = key !in current
        bookmarks = if (added) current + key else current - key
        return added
    }

    companion object {
        // Paris par défaut tant que l'utilisateur n'a pas partagé sa position.
        const val DEFAULT_LAT = 48.8566
        const val DEFAULT_LNG = 2.3522
        const val DEFAULT_CITY = "Paris"

        private const val KEY_LAT = "lat"
        private const val KEY_LNG = "lng"
        private const val KEY_CITY = "city"
        private const val KEY_METHOD = "method"
        private const val KEY_HANAFI = "hanafi"
        private const val KEY_NOTIF = "notifications"
        private const val KEY_AR_SIZE = "arabic_size"
        private const val KEY_TRANSLATION = "translation"
        private const val KEY_LAST_SURAH = "last_surah"
        private const val KEY_LAST_AYAH = "last_ayah"
        private const val KEY_BOOKMARKS = "bookmarks"
    }
}
