package com.nour.app.quran

data class Surah(
    val number: Int,
    val name: String,
    val arabic: String,
    val meaning: String,
    val ayahs: Int,
    val medinan: Boolean,
) {
    val revelation: String get() = if (medinan) "Médinoise" else "Mecquoise"
}

object Surahs {
    private val medinan = setOf(
        2, 3, 4, 5, 8, 9, 13, 22, 24, 33, 47, 48, 49, 55, 57, 58, 59, 60,
        61, 62, 63, 64, 65, 66, 76, 98, 99, 110,
    )

    private val raw = listOf(
        Triple("Al-Fatiha", "الفاتحة", "L'ouverture") to 7,
        Triple("Al-Baqara", "البقرة", "La vache") to 286,
        Triple("Al-Imran", "آل عمران", "La famille d'Imran") to 200,
        Triple("An-Nisa", "النساء", "Les femmes") to 176,
        Triple("Al-Ma'ida", "المائدة", "La table servie") to 120,
        Triple("Al-An'am", "الأنعام", "Les bestiaux") to 165,
        Triple("Al-A'raf", "الأعراف", "Al-A'raf") to 206,
        Triple("Al-Anfal", "الأنفال", "Le butin") to 75,
        Triple("At-Tawba", "التوبة", "Le repentir") to 129,
        Triple("Yunus", "يونس", "Jonas") to 109,
        Triple("Hud", "هود", "Hud") to 123,
        Triple("Yusuf", "يوسف", "Joseph") to 111,
        Triple("Ar-Ra'd", "الرعد", "Le tonnerre") to 43,
        Triple("Ibrahim", "إبراهيم", "Abraham") to 52,
        Triple("Al-Hijr", "الحجر", "Al-Hijr") to 99,
        Triple("An-Nahl", "النحل", "Les abeilles") to 128,
        Triple("Al-Isra", "الإسراء", "Le voyage nocturne") to 111,
        Triple("Al-Kahf", "الكهف", "La caverne") to 110,
        Triple("Maryam", "مريم", "Marie") to 98,
        Triple("Ta-Ha", "طه", "Ta-Ha") to 135,
        Triple("Al-Anbiya", "الأنبياء", "Les prophètes") to 112,
        Triple("Al-Hajj", "الحج", "Le pèlerinage") to 78,
        Triple("Al-Mu'minun", "المؤمنون", "Les croyants") to 118,
        Triple("An-Nur", "النور", "La lumière") to 64,
        Triple("Al-Furqan", "الفرقان", "Le discernement") to 77,
        Triple("Ash-Shu'ara", "الشعراء", "Les poètes") to 227,
        Triple("An-Naml", "النمل", "Les fourmis") to 93,
        Triple("Al-Qasas", "القصص", "Le récit") to 88,
        Triple("Al-Ankabut", "العنكبوت", "L'araignée") to 69,
        Triple("Ar-Rum", "الروم", "Les Romains") to 60,
        Triple("Luqman", "لقمان", "Luqman") to 34,
        Triple("As-Sajda", "السجدة", "La prosternation") to 30,
        Triple("Al-Ahzab", "الأحزاب", "Les coalisés") to 73,
        Triple("Saba", "سبأ", "Saba") to 54,
        Triple("Fatir", "فاطر", "Le Créateur") to 45,
        Triple("Ya-Sin", "يس", "Ya-Sin") to 83,
        Triple("As-Saffat", "الصافات", "Les rangés") to 182,
        Triple("Sad", "ص", "Sad") to 88,
        Triple("Az-Zumar", "الزمر", "Les groupes") to 75,
        Triple("Ghafir", "غافر", "Le Pardonneur") to 85,
        Triple("Fussilat", "فصلت", "Les versets détaillés") to 54,
        Triple("Ash-Shura", "الشورى", "La consultation") to 53,
        Triple("Az-Zukhruf", "الزخرف", "L'ornement") to 89,
        Triple("Ad-Dukhan", "الدخان", "La fumée") to 59,
        Triple("Al-Jathiya", "الجاثية", "L'agenouillée") to 37,
        Triple("Al-Ahqaf", "الأحقاف", "Al-Ahqaf") to 35,
        Triple("Muhammad", "محمد", "Muhammad") to 38,
        Triple("Al-Fath", "الفتح", "La victoire éclatante") to 29,
        Triple("Al-Hujurat", "الحجرات", "Les appartements") to 18,
        Triple("Qaf", "ق", "Qaf") to 45,
        Triple("Adh-Dhariyat", "الذاريات", "Qui éparpillent") to 60,
        Triple("At-Tur", "الطور", "Le mont") to 49,
        Triple("An-Najm", "النجم", "L'étoile") to 62,
        Triple("Al-Qamar", "القمر", "La lune") to 55,
        Triple("Ar-Rahman", "الرحمن", "Le Tout Miséricordieux") to 78,
        Triple("Al-Waqi'a", "الواقعة", "L'événement") to 96,
        Triple("Al-Hadid", "الحديد", "Le fer") to 29,
        Triple("Al-Mujadila", "المجادلة", "La discussion") to 22,
        Triple("Al-Hashr", "الحشر", "L'exode") to 24,
        Triple("Al-Mumtahana", "الممتحنة", "L'éprouvée") to 13,
        Triple("As-Saff", "الصف", "Le rang") to 14,
        Triple("Al-Jumu'a", "الجمعة", "Le vendredi") to 11,
        Triple("Al-Munafiqun", "المنافقون", "Les hypocrites") to 11,
        Triple("At-Taghabun", "التغابن", "La grande perte") to 18,
        Triple("At-Talaq", "الطلاق", "Le divorce") to 12,
        Triple("At-Tahrim", "التحريم", "L'interdiction") to 12,
        Triple("Al-Mulk", "الملك", "La royauté") to 30,
        Triple("Al-Qalam", "القلم", "La plume") to 52,
        Triple("Al-Haqqa", "الحاقة", "Celle qui montre la vérité") to 52,
        Triple("Al-Ma'arij", "المعارج", "Les voies d'ascension") to 44,
        Triple("Nuh", "نوح", "Noé") to 28,
        Triple("Al-Jinn", "الجن", "Les djinns") to 28,
        Triple("Al-Muzzammil", "المزمل", "L'enveloppé") to 20,
        Triple("Al-Muddathir", "المدثر", "Le revêtu d'un manteau") to 56,
        Triple("Al-Qiyama", "القيامة", "La résurrection") to 40,
        Triple("Al-Insan", "الإنسان", "L'homme") to 31,
        Triple("Al-Mursalat", "المرسلات", "Les envoyés") to 50,
        Triple("An-Naba", "النبأ", "La nouvelle") to 40,
        Triple("An-Nazi'at", "النازعات", "Les anges qui arrachent") to 46,
        Triple("Abasa", "عبس", "Il s'est renfrogné") to 42,
        Triple("At-Takwir", "التكوير", "L'obscurcissement") to 29,
        Triple("Al-Infitar", "الانفطار", "La rupture") to 19,
        Triple("Al-Mutaffifin", "المطففين", "Les fraudeurs") to 36,
        Triple("Al-Inshiqaq", "الانشقاق", "La déchirure") to 25,
        Triple("Al-Buruj", "البروج", "Les constellations") to 22,
        Triple("At-Tariq", "الطارق", "L'astre nocturne") to 17,
        Triple("Al-A'la", "الأعلى", "Le Très-Haut") to 19,
        Triple("Al-Ghashiya", "الغاشية", "L'enveloppante") to 26,
        Triple("Al-Fajr", "الفجر", "L'aube") to 30,
        Triple("Al-Balad", "البلد", "La cité") to 20,
        Triple("Ash-Shams", "الشمس", "Le soleil") to 15,
        Triple("Al-Layl", "الليل", "La nuit") to 21,
        Triple("Ad-Duha", "الضحى", "Le jour montant") to 11,
        Triple("Ash-Sharh", "الشرح", "L'ouverture") to 8,
        Triple("At-Tin", "التين", "Le figuier") to 8,
        Triple("Al-Alaq", "العلق", "L'adhérence") to 19,
        Triple("Al-Qadr", "القدر", "La destinée") to 5,
        Triple("Al-Bayyina", "البينة", "La preuve") to 8,
        Triple("Az-Zalzala", "الزلزلة", "La secousse") to 8,
        Triple("Al-Adiyat", "العاديات", "Les coursiers") to 11,
        Triple("Al-Qari'a", "القارعة", "Le fracas") to 11,
        Triple("At-Takathur", "التكاثر", "La course aux richesses") to 8,
        Triple("Al-Asr", "العصر", "Le temps") to 3,
        Triple("Al-Humaza", "الهمزة", "Les calomniateurs") to 9,
        Triple("Al-Fil", "الفيل", "L'éléphant") to 5,
        Triple("Quraysh", "قريش", "Quraych") to 4,
        Triple("Al-Ma'un", "الماعون", "L'ustensile") to 7,
        Triple("Al-Kawthar", "الكوثر", "L'abondance") to 3,
        Triple("Al-Kafirun", "الكافرون", "Les infidèles") to 6,
        Triple("An-Nasr", "النصر", "Les secours") to 3,
        Triple("Al-Masad", "المسد", "Les fibres") to 5,
        Triple("Al-Ikhlas", "الإخلاص", "Le monothéisme pur") to 4,
        Triple("Al-Falaq", "الفلق", "L'aube naissante") to 5,
        Triple("An-Nas", "الناس", "Les hommes") to 6,
    )

    val all: List<Surah> = raw.mapIndexed { i, (names, ayahs) ->
        Surah(i + 1, names.first, names.second, names.third, ayahs, (i + 1) in medinan)
    }

    const val TOTAL_AYAHS = 6236

    fun get(number: Int): Surah = all[number - 1]

    /** Convertit un numéro de verset global (1..6236) en (sourate, verset). */
    fun locate(globalAyah: Int): Pair<Int, Int> {
        var remaining = globalAyah
        for (s in all) {
            if (remaining <= s.ayahs) return s.number to remaining
            remaining -= s.ayahs
        }
        return 114 to 6
    }
}
