package com.jianwang.vocab9000.ui.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jianwang.vocab9000.AppContainer
import com.jianwang.vocab9000.R
import com.jianwang.vocab9000.data.entity.FlashCardEntity
import com.jianwang.vocab9000.data.repository.StatsService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailScreen(
    container: AppContainer,
    catalogKey: String,
    onBack: () -> Unit,
) {
    val entry = remember(catalogKey) { container.catalog.entryForKey(catalogKey) }
    var learned by remember { mutableStateOf(false) }
    var defText by remember { mutableStateOf("") }
    var phonetic by remember { mutableStateOf<String?>(null) }
    var showDeckPicker by remember { mutableStateOf(false) }
    var myDecks by remember { mutableStateOf(emptyList<com.jianwang.vocab9000.data.entity.DeckEntity>()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(catalogKey) {
        container.catalog.loadIfNeeded()
        val row = container.db.progressDao().getByKey(catalogKey)
        learned = row?.isLearned == true
        if (entry != null) {
            val (text, ph) = container.definitions.definition(entry.word)
            defText = text
            phonetic = ph
        }
        myDecks = container.db.deckDao().getAll().filter { !it.isBuiltIn }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(entry?.word ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        }
    ) { padding ->
        if (entry == null) {
            Text(
                stringResource(R.string.word_not_found),
                Modifier.padding(padding).padding(16.dp),
            )
            return@Scaffold
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(entry.word, style = MaterialTheme.typography.headlineLarge)
            Text(entry.displayPos, color = MaterialTheme.colorScheme.primary)
            phonetic?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            Text(entry.primaryBack, style = MaterialTheme.typography.titleLarge)
            if (defText.isNotBlank()) {
                Text(stringResource(R.string.word_en_def), style = MaterialTheme.typography.labelLarge)
                Text(defText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilledTonalButton(onClick = {
                    container.speech.speak(entry.word, container.prefs.ttsLanguage)
                }) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
                    Text("  " + stringResource(R.string.study_speak))
                }
                Button(onClick = {
                    scope.launch {
                        val next = !learned
                        container.seed.setLearned(entry.id, entry.word, next)
                        learned = next
                    }
                }) {
                    Icon(if (learned) Icons.Default.Check else Icons.Default.Add, contentDescription = null)
                    Text("  " + stringResource(if (learned) R.string.word_mastered else R.string.word_mark_mastered))
                }
            }
            OutlinedButton(onClick = { showDeckPicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.word_add_to_deck))
            }
        }
    }

    if (showDeckPicker) {
        AlertDialog(
            onDismissRequest = { showDeckPicker = false },
            title = { Text(stringResource(R.string.word_pick_deck)) },
            text = {
                Column {
                    if (myDecks.isEmpty()) {
                        Text(stringResource(R.string.word_no_my_decks))
                    } else {
                        myDecks.forEach { deck ->
                            TextButton(onClick = {
                                scope.launch {
                                    container.db.flashCardDao().upsert(
                                        FlashCardEntity(
                                            deckId = deck.id,
                                            front = entry!!.word,
                                            back = entry.primaryBack,
                                            pos = entry.pos,
                                            catalogKey = entry.id,
                                        )
                                    )
                                    showDeckPicker = false
                                }
                            }) {
                                Text(StatsService.localizedDeckTitle(deck.title))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDeckPicker = false }) {
                    Text(stringResource(R.string.action_close))
                }
            },
        )
    }
}
