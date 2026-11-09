package com.jianwang.vocab9000.ui.decks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.jianwang.vocab9000.domain.CatalogEntry
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckDetailScreen(
    container: AppContainer,
    deckId: String,
    onBack: () -> Unit,
    onStudy: () -> Unit,
    onEditCard: (String?) -> Unit,
) {
    var deck by remember { mutableStateOf<DeckEntity?>(null) }
    val cards by container.db.flashCardDao().observeForDeck(deckId).collectAsState(initial = emptyList())
    var preview by remember { mutableStateOf<List<CatalogEntry>>(emptyList()) }
    var learnedTotal by remember { mutableStateOf(0 to 0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(deckId) {
        container.catalog.loadIfNeeded()
        deck = container.db.deckDao().getById(deckId)
        val d = deck ?: return@LaunchedEffect
        if (d.isBuiltIn) {
            preview = container.seed.catalogEntriesFor(d).take(100)
            learnedTotal = container.seed.builtInStats(d)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(StatsService.localizedDeckTitle(deck?.title.orEmpty())) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (deck?.isBuiltIn == false) {
                        IconButton(onClick = { onEditCard(null) }) {
                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.card_add))
                        }
                        IconButton(onClick = {
                            scope.launch {
                                deck?.let {
                                    if (!it.isBuiltIn) {
                                        container.db.deckDao().delete(it)
                                        onBack()
                                    }
                                }
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.deck_delete))
                        }
                    }
                },
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val (learned, total) = if (deck?.isBuiltIn == true) learnedTotal else cards.count { it.isLearned } to cards.size
            Text(
                stringResource(R.string.stats_learned_of, learned, total),
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onStudy, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Text("  " + stringResource(R.string.study_start))
            }
            if (deck?.isBuiltIn == true) {
                Text(stringResource(R.string.deck_virtual_hint), style = MaterialTheme.typography.bodySmall)
                LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                    items(preview, key = { it.id }) { entry ->
                        ListItem(
                            headlineContent = { Text(entry.word) },
                            supportingContent = { Text(entry.primaryBack) },
                        )
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                    items(cards, key = { it.id }) { card ->
                        ListItem(
                            headlineContent = { Text(card.front) },
                            supportingContent = { Text(card.back) },
                            modifier = Modifier.clickable { onEditCard(card.id) },
                            trailingContent = {
                                IconButton(onClick = {
                                    scope.launch { container.db.flashCardDao().delete(card) }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = null)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
