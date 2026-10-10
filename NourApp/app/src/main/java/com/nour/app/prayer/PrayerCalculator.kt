package com.nour.app.prayer

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.CalculationParameters
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.HighLatitudeRule
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.Qibla
import com.batoulapps.adhan.data.DateComponents
import com.nour.app.data.AppSettings
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/** Méthodes de calcul proposées (angles Fajr / Isha). */
enum class Method(val id: String, val label: String) {
    UOIF("UOIF", "France – UOIF (12°)"),
    FRANCE_15("FRANCE_15", "France – 15°"),
    FRANCE_18("FRANCE_18", "France – 18° (Grande Mosquée de Paris)"),
    MWL("MWL", "Ligue Islamique Mondiale"),
    EGYPT("EGYPT", "Égypte"),
    UMM_AL_QURA("UMM_AL_QURA", "Umm al-Qura (La Mecque)"),
    ISNA("ISNA", "Amérique du Nord (ISNA)"),
    KARACHI("KARACHI", "Karachi"),
    DUBAI("DUBAI", "Dubaï"),
    QATAR("QATAR", "Qatar"),
    KUWAIT("KUWAIT", "Koweït"),
    MOROCCO("MOROCCO", "Maroc (19° / 17°)"),
    ALGERIA("ALGERIA", "Algérie (18° / 17°)"),
    TUNISIA("TUNISIA", "Tunisie (18° / 18°)"),
    TURKEY("TURKEY", "Turquie (18° / 17°)");

    fun parameters(): CalculationParameters = when (this) {
        UOIF -> CalculationParameters(12.0, 12.0)
        FRANCE_15 -> CalculationParameters(15.0, 15.0)
        FRANCE_18 -> CalculationParameters(18.0, 18.0)
        MWL -> CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
        EGYPT -> CalculationMethod.EGYPTIAN.parameters
        UMM_AL_QURA -> CalculationMethod.UMM_AL_QURA.parameters
        ISNA -> CalculationMethod.NORTH_AMERICA.parameters
        KARACHI -> CalculationMethod.KARACHI.parameters
        DUBAI -> CalculationMethod.DUBAI.parameters
        QATAR -> CalculationMethod.QATAR.parameters
        KUWAIT -> CalculationMethod.KUWAIT.parameters
        MOROCCO -> CalculationParameters(19.0, 17.0)
        ALGERIA -> CalculationParameters(18.0, 17.0)
        TUNISIA -> CalculationParameters(18.0, 18.0)
        TURKEY -> CalculationParameters(18.0, 17.0)
    }

    companion object {
        fun fromId(id: String): Method = entries.firstOrNull { it.id == id } ?: UOIF
    }
}

/** Les six moments affichés dans l'écran principal. */
enum class PrayerName(val label: String, val arabic: String, val isPrayer: Boolean = true) {
    FAJR("Fajr", "الفجر"),
    SUNRISE("Chourouk", "الشروق", isPrayer = false),
    DHUHR("Dhohr", "الظهر"),
    ASR("Asr", "العصر"),
    MAGHRIB("Maghrib", "المغرب"),
    ISHA("Icha", "العشاء"),
}

data class PrayerTime(val name: PrayerName, val time: ZonedDateTime)

data class DayTimes(val date: LocalDate, val times: List<PrayerTime>)

object PrayerCalculator {

    fun paramsFrom(settings: AppSettings): CalculationParameters =
        Method.fromId(settings.method).parameters().apply {
            madhab = if (settings.hanafiAsr) Madhab.HANAFI else Madhab.SHAFI
            // Indispensable en été au-delà de ~48° de latitude (nord de la France, Belgique…).
            highLatitudeRule = HighLatitudeRule.SEVENTH_OF_THE_NIGHT
        }

    fun forDay(
        latitude: Double,
        longitude: Double,
        date: LocalDate,
        params: CalculationParameters,
        zone: ZoneId = ZoneId.systemDefault(),
    ): DayTimes {
        val pt = PrayerTimes(
            Coordinates(latitude, longitude),
            DateComponents(date.year, date.monthValue, date.dayOfMonth),
            params,
        )
        fun z(d: java.util.Date) = ZonedDateTime.ofInstant(d.toInstant(), zone)
        return DayTimes(
            date,
            listOf(
                PrayerTime(PrayerName.FAJR, z(pt.fajr)),
                PrayerTime(PrayerName.SUNRISE, z(pt.sunrise)),
                PrayerTime(PrayerName.DHUHR, z(pt.dhuhr)),
                PrayerTime(PrayerName.ASR, z(pt.asr)),
                PrayerTime(PrayerName.MAGHRIB, z(pt.maghrib)),
                PrayerTime(PrayerName.ISHA, z(pt.isha)),
            ),
        )
    }

    fun forDay(settings: AppSettings, date: LocalDate = LocalDate.now()): DayTimes =
        forDay(settings.latitude, settings.longitude, date, paramsFrom(settings))

    /** Prochaine prière (Chourouk exclu) après [now], en regardant le lendemain si besoin. */
    fun nextPrayer(settings: AppSettings, now: ZonedDateTime = ZonedDateTime.now()): PrayerTime {
        val today = forDay(settings, now.toLocalDate())
        today.times.firstOrNull { it.name.isPrayer && it.time.isAfter(now) }?.let { return it }
        val tomorrow = forDay(settings, now.toLocalDate().plusDays(1))
        return tomorrow.times.first { it.name == PrayerName.FAJR }
    }

    fun qiblaDirection(latitude: Double, longitude: Double): Double =
        Qibla(Coordinates(latitude, longitude)).direction
}
