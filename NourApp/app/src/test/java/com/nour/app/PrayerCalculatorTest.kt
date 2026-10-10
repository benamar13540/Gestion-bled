package com.nour.app

import com.nour.app.prayer.Method
import com.nour.app.prayer.PrayerCalculator
import com.nour.app.quran.Surahs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class PrayerCalculatorTest {

    private val paris = ZoneId.of("Europe/Paris")

    @Test
    fun prayersAreInChronologicalOrderForEveryMethod() {
        for (method in Method.entries) {
            // Paris en plein été : cas difficile pour Fajr / Icha (haute latitude).
            val day = PrayerCalculator.forDay(48.8566, 2.3522, LocalDate.of(2026, 6, 21), method.parameters(), paris)
            day.times.zipWithNext().forEach { (a, b) ->
                assertTrue("$method : ${a.name} doit précéder ${b.name}", a.time.isBefore(b.time))
            }
        }
    }

    @Test
    fun dhuhrInParisIsAroundSolarNoon() {
        val day = PrayerCalculator.forDay(48.8566, 2.3522, LocalDate.of(2026, 3, 20), Method.UOIF.parameters(), paris)
        // Passage au méridien vers 12h58 (heure d'hiver), Dhohr juste après.
        val minutes = day.times[2].time.let { it.hour * 60 + it.minute }
        assertTrue("Dhohr à ${day.times[2].time}", minutes in (12 * 60 + 50)..(13 * 60 + 10))
    }

    @Test
    fun qiblaFromParisPointsSouthEast() {
        val q = PrayerCalculator.qiblaDirection(48.8566, 2.3522)
        assertTrue("Qibla de Paris ≈ 119°, obtenu $q", q in 115.0..125.0)
    }

    @Test
    fun surahMetadataIsConsistent() {
        assertEquals(114, Surahs.all.size)
        assertEquals(Surahs.TOTAL_AYAHS, Surahs.all.sumOf { it.ayahs })
        assertEquals(1 to 1, Surahs.locate(1))
        assertEquals(2 to 255, Surahs.locate(7 + 255))
        assertEquals(114 to 6, Surahs.locate(Surahs.TOTAL_AYAHS))
    }
}
