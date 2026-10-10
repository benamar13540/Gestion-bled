package com.nour.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nour.app.data.AppSettings
import com.nour.app.quran.Ayah
import com.nour.app.quran.QuranRepository
import com.nour.app.quran.Surah
import com.nour.app.quran.Surahs

@Composable
fun QuranScreen(modifier: Modifier, settingsVersion: Int) {
    // Sourate ouverte (0 = liste) et verset sur lequel se positionner.
    var openSurah by rememberSaveable { mutableIntStateOf(0) }
    var openAyah by rememberSaveable { mutableIntStateOf(1) }

    if (openSurah == 0) {
        SurahList(modifier) { surah, ayah ->
            openAyah = ayah
            openSurah = surah
        }
    } else {
        BackHandler { openSurah = 0 }
        SurahReader(modifier, Surahs.get(openSurah), openAyah, settingsVersion) { openSurah = 0 }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SurahList(modifier: Modifier, onOpen: (surah: Int, ayah: Int) -> Unit) {
    val context = LocalContext.current
    val settings = remember { AppSettings(context) }
    var query by rememberSaveable { mutableStateOf("") }
    var favoritesOnly by rememberSaveable { mutableStateOf(false) }

    val bookmarks = remember { settings.bookmarks.mapNotNull { parseRef(it) }.sortedWith(compareBy({ it.first }, { it.second })) }
    val filtered = Surahs.all.filter { s ->
        query.isBlank() ||
            s.name.contains(query, ignoreCase = true) ||
            s.meaning.contains(query, ignoreCase = true) ||
            s.arabic.contains(query) ||
            s.number.toString() == query.trim()
    }

    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text("Le Saint Coran", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("القرآن الكريم", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
        }

        if (settings.lastReadSurah > 0) {
            item {
                val s = Surahs.get(settings.lastReadSurah)
                Card(
                    onClick = { onOpen(s.number, settings.lastReadAyah) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Reprendre la lecture", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), style = MaterialTheme.typography.labelLarge)
                        Text(
                            "${s.name} · verset ${settings.lastReadAyah}",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                placeholder = { Text("Rechercher une sourate") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = !favoritesOnly, onClick = { favoritesOnly = false }, label = { Text("Sourates") })
                FilterChip(selected = favoritesOnly, onClick = { favoritesOnly = true }, label = { Text("Favoris (${bookmarks.size})") })
            }
        }

        if (favoritesOnly) {
            if (bookmarks.isEmpty()) {
                item {
                    Text(
                        "Aucun favori. Touchez l'étoile à côté d'un verset pour l'ajouter.",
                        Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            items(bookmarks) { (surah, ayah) ->
                val s = Surahs.get(surah)
                Card(onClick = { onOpen(surah, ayah) }, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = Gold)
                        Spacer(Modifier.width(12.dp))
                        Text("${s.name} · verset $ayah", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        } else {
            items(filtered, key = { it.number }) { s -> SurahRow(s) { onOpen(s.number, 1) } }
        }
    }
}

@Composable
private fun SurahRow(s: Surah, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("${s.number}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(s.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "${s.meaning} · ${s.revelation} · ${s.ayahs} versets",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
        Text(s.arabic, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun SurahReader(modifier: Modifier, surah: Surah, startAyah: Int, settingsVersion: Int, onBack: () -> Unit) {
    val context = LocalContext.current
    val settings = remember { AppSettings(context) }
    val repo = remember { QuranRepository(context) }
    var ayahs by remember(surah.number) { mutableStateOf<List<Ayah>?>(null) }
    var error by remember(surah.number) { mutableStateOf<String?>(null) }
    var attempt by remember { mutableIntStateOf(0) }
    var bookmarks by remember { mutableStateOf(settings.bookmarks) }
    val arabicSize = remember(settingsVersion) { settings.arabicTextSize }
    val showTranslation = remember(settingsVersion) { settings.showTranslation }
    val listState = rememberLazyListState()

    LaunchedEffect(surah.number, attempt) {
        error = null
        runCatching { repo.surah(surah.number) }
            .onSuccess { ayahs = it }
            .onFailure { error = "Impossible de charger la sourate. Vérifiez votre connexion internet." }
    }
    // Se positionne sur le verset demandé (l'élément 0 est l'en-tête).
    LaunchedEffect(ayahs) {
        if (ayahs != null && startAyah > 1) listState.scrollToItem(startAyah)
    }

    Column(modifier.fillMaxSize()) {
        Row(Modifier.padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour") }
            Column(Modifier.weight(1f)) {
                Text("${surah.number}. ${surah.name}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${surah.meaning} · ${surah.ayahs} versets", style = MaterialTheme.typography.bodySmall)
            }
            Text(surah.arabic, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 12.dp))
        }
        HorizontalDivider()

        val list = ayahs
        when {
            list != null -> LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                item {
                    if (surah.number != 1 && surah.number != 9) {
                        Text(
                            QuranRepository.BASMALA,
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            textAlign = TextAlign.Center,
                            fontSize = (arabicSize + 2).sp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                items(list, key = { it.number }) { a ->
                    val ref = "${a.surah}:${a.number}"
                    AyahItem(
                        ayah = a,
                        arabicSize = arabicSize,
                        showTranslation = showTranslation,
                        bookmarked = ref in bookmarks,
                        onBookmark = {
                            settings.toggleBookmark(a.surah, a.number)
                            bookmarks = settings.bookmarks
                        },
                        onRead = {
                            settings.lastReadSurah = a.surah
                            settings.lastReadAyah = a.number
                        },
                    )
                }
            }
            error != null -> Column(
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(error ?: "", textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Button(onClick = { attempt++ }) { Text("Réessayer") }
            }
            else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }
    }

    // Mémorise automatiquement la position de lecture en faisant défiler.
    LaunchedEffect(listState, ayahs) {
        androidx.compose.runtime.snapshotFlow { listState.firstVisibleItemIndex }.collect { index ->
            val list = ayahs ?: return@collect
            if (index >= 1 && index <= list.size) {
                settings.lastReadSurah = surah.number
                settings.lastReadAyah = list[index - 1].number
            } else if (index == 0) {
                settings.lastReadSurah = surah.number
                settings.lastReadAyah = 1
            }
        }
    }
}

@Composable
private fun AyahItem(
    ayah: Ayah,
    arabicSize: Float,
    showTranslation: Boolean,
    bookmarked: Boolean,
    onBookmark: () -> Unit,
    onRead: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onRead)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text("${ayah.surah}:${ayah.number}", style = MaterialTheme.typography.labelMedium)
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onBookmark) {
                Icon(
                    if (bookmarked) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = if (bookmarked) "Retirer des favoris" else "Ajouter aux favoris",
                    tint = if (bookmarked) Gold else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                )
            }
        }
        Text(
            "${ayah.arabic} ${toArabicDigits(ayah.number)}",
            style = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Rtl),
            fontSize = arabicSize.sp,
            lineHeight = (arabicSize * 1.8f).sp,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth(),
        )
        if (showTranslation) {
            Spacer(Modifier.height(8.dp))
            Text(ayah.translation, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
        }
    }
    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant)
}

private fun parseRef(ref: String): Pair<Int, Int>? {
    val parts = ref.split(":")
    val s = parts.getOrNull(0)?.toIntOrNull() ?: return null
    val a = parts.getOrNull(1)?.toIntOrNull() ?: return null
    return s to a
}

/** Numéro de verset en chiffres arabes orientaux, encadré comme dans le Mushaf. */
private fun toArabicDigits(n: Int): String =
    "﴿" + n.toString().map { '٠' + (it - '0') }.joinToString("") + "﴾"
