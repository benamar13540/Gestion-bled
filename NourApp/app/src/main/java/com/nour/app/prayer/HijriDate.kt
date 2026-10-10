package com.nour.app.prayer

import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

object HijriDate {
    private val months = listOf(
        "Mouharram", "Safar", "Rabi' al-awwal", "Rabi' ath-thani",
        "Joumada al-oula", "Joumada ath-thania", "Rajab", "Cha'bane",
        "Ramadan", "Chawwal", "Dhou al-Qi'da", "Dhou al-Hijja",
    )

    fun format(date: LocalDate = LocalDate.now()): String = try {
        val h = HijrahDate.from(date)
        val day = h.get(ChronoField.DAY_OF_MONTH)
        val month = h.get(ChronoField.MONTH_OF_YEAR)
        val year = h.get(ChronoField.YEAR)
        "$day ${months[month - 1]} $year H"
    } catch (e: Exception) {
        ""
    }
}
