package com.jianwang.vocab9000.ui.decks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.jianwang.vocab9000.data.entity.DeckEntity
import com.jianwang.vocab9000.data.repository.StatsService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckListScreen(
    container: AppContainer,
    onOpenDeck: (String) -> Unit,
) {
    val decks by container.db.deckDao().observeAll().collectAsState(initial = emptyList())
    var stats by remember { mutableStateOf<Map<String, Triple<Int, Int, Int>>>(emptyMap()) }
    var showCreate by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(decks) {
        container.catalog.loadIfNeeded()
        val map = mutableMapOf<String, Triple<Int, Int, Int>>()
        for (d in decks) {
            if (d.isBuiltIn) {
                val (learned, total) = container.seed.builtInStats(d)
                val due = container.seed.dueCount(d)
                map[d.id] = Triple(learned, total, due)
            } else {
                val cards = container.db.flashCardDao().getForDeck(d.id)
                val now = System.currentTimeMillis()
                map[d.id] = Triple(
                    cards.count { it.isLearned },
                    cards.size,
                    cards.count { it.isDue(now) },
                )
            }
        }
        stats = map
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.tab_decks)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.deck_create))
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(decks, key = { it.id }) { deck ->
                val s = stats[deck.id] ?: Triple(0, 0, 0)
                val progress = if (s.second == 0) 0f else s.first.toFloat() / s.second
                Card(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onOpenDeck(deck.id) }
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                StatsService.localizedDeckTitle(deck.title),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            if (deck.isBuiltIn) {
                                Text(
                                    stringResource(R.string.deck_builtin),
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }
                        Text(
                            stringResource(R.string.stats_learned_of, s.first, s.second) +
                                if (s.third > 0) " · " + stringResource(R.string.study_due_label, s.third) else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }

    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text(stringResource(R.string.deck_create)) },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text(stringResource(R.string.deck_title)) },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val t = newTitle.trim()
                    if (t.isNotEmpty()) {
                        scope.launch {
                            container.db.deckDao().upsert(DeckEntity(title = t))
                            newTitle = ""
                            showCreate = false
                        }
                    }
                }) { Text(stringResource(R.string.action_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showCreate = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}
