package com.nour.app.quran

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

data class Ayah(val surah: Int, val number: Int, val arabic: String, val translation: String)

/**
 * Texte du Coran (écriture uthmani) + traduction française de Muhammad Hamidullah,
 * téléchargés depuis l'API publique alquran.cloud puis gardés en cache pour la lecture hors-ligne.
 */
class QuranRepository(context: Context) {

    private val cacheDir = File(context.filesDir, "quran").apply { mkdirs() }

    suspend fun surah(number: Int): List<Ayah> = withContext(Dispatchers.IO) {
        val json = cachedOrFetch("surah_$number.json") {
            "$API/surah/$number/editions/$ARABIC_EDITION,$FRENCH_EDITION"
        }
        val data = JSONObject(json).getJSONArray("data")
        val arabic = data.getJSONObject(0).getJSONArray("ayahs")
        val french = data.getJSONObject(1).getJSONArray("ayahs")
        List(arabic.length()) { i ->
            val a = arabic.getJSONObject(i)
            val n = a.getInt("numberInSurah")
            var text = a.getString("text")
            if (n == 1 && number != 1 && number != 9) text = stripBasmala(text)
            Ayah(number, n, text, french.getJSONObject(i).getString("text"))
        }
    }

    /** Un verset différent chaque jour, identique pour toute la journée. */
    suspend fun verseOfTheDay(date: LocalDate = LocalDate.now()): Ayah = withContext(Dispatchers.IO) {
        val global = (Math.floorMod(date.toEpochDay() * 7919L, Surahs.TOTAL_AYAHS.toLong()) + 1).toInt()
        val (surah, ayah) = Surahs.locate(global)
        val json = cachedOrFetch("ayah_$surah-$ayah.json") {
            "$API/ayah/$surah:$ayah/editions/$ARABIC_EDITION,$FRENCH_EDITION"
        }
        val data = JSONObject(json).getJSONArray("data")
        var arabic = data.getJSONObject(0).getString("text")
        if (ayah == 1 && surah != 1 && surah != 9) arabic = stripBasmala(arabic)
        Ayah(surah, ayah, arabic, data.getJSONObject(1).getString("text"))
    }

    fun isDownloaded(number: Int): Boolean = File(cacheDir, "surah_$number.json").exists()

    private fun cachedOrFetch(fileName: String, url: () -> String): String {
        val file = File(cacheDir, fileName)
        if (file.exists()) return file.readText()
        val body = httpGet(url())
        // On vérifie que la réponse est exploitable avant de la mettre en cache.
        require(JSONObject(body).optInt("code") == 200) { "Réponse inattendue du serveur" }
        file.writeText(body)
        return body
    }

    private fun httpGet(url: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 15_000
        conn.readTimeout = 30_000
        try {
            if (conn.responseCode != 200) error("Erreur réseau (${conn.responseCode})")
            return conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }

    /** L'API préfixe le 1er verset de chaque sourate par la Basmala ; on l'affiche à part. */
    private fun stripBasmala(text: String): String {
        val words = text.replace("\uFEFF", "").trim().split(Regex("\\s+"))
        val first = words.first().replace(Regex("[\u064B-\u065F\u0670\u06D6-\u06ED]"), "")
        return if (words.size > 4 && first == "بسم") words.drop(4).joinToString(" ") else text
    }

    companion object {
        private const val API = "https://api.alquran.cloud/v1"
        private const val ARABIC_EDITION = "quran-uthmani"
        private const val FRENCH_EDITION = "fr.hamidullah"
        const val BASMALA = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"
    }
}
