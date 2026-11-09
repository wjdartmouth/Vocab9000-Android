package com.jianwang.vocab9000.ui.study

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jianwang.vocab9000.AppContainer
import com.jianwang.vocab9000.R
import com.jianwang.vocab9000.data.repository.StatsService
import com.jianwang.vocab9000.ui.theme.AgainOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyHubScreen(
    container: AppContainer,
    onStart: (String) -> Unit,
) {
    val decks by container.db.deckDao().observeAll().collectAsState(initial = emptyList())
    var dueMap by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    LaunchedEffect(decks) {
        container.catalog.loadIfNeeded()
        val map = mutableMapOf<String, Int>()
        for (d in decks) {
            map[d.id] = container.seed.dueCount(d)
        }
        dueMap = map
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.tab_study)) }) }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(
                    stringResource(R.string.study_pick_deck),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(decks, key = { it.id }) { deck ->
                val due = dueMap[deck.id] ?: 0
                Card(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onStart(deck.id) }
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            StatsService.localizedDeckTitle(deck.title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            stringResource(R.string.study_due_label, due),
                            color = if (due > 0) AgainOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}
