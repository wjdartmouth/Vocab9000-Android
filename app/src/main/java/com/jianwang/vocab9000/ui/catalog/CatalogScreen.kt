package com.jianwang.vocab9000.ui.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.jianwang.vocab9000.domain.CatalogEntry

private val POS_FILTERS = listOf("", "noun", "verb", "adj", "adv", "prep", "pron", "conj", "interj")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    container: AppContainer,
    onOpenWord: (String) -> Unit,
) {
    LaunchedEffect(Unit) { container.catalog.loadIfNeeded() }
    val all by container.catalog.entries.collectAsState()
    var query by remember { mutableStateOf("") }
    var letter by remember { mutableStateOf<String?>(null) }
    var pos by remember { mutableStateOf("") }

    val filtered = remember(all, query, letter, pos) {
        var list: List<CatalogEntry> = all
        if (letter != null) list = list.filter { it.letter == letter }
        if (pos.isNotEmpty()) list = list.filter { (it.pos ?: "").lowercase().contains(pos) }
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter { it.word.lowercase().contains(q) }
        }
        list
    }
    val letters = remember(all) { all.map { it.letter }.distinct().sorted() }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.tab_catalog)) }) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text(stringResource(R.string.catalog_search)) },
                singleLine = true,
            )
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FilterChip(
                    selected = letter == null,
                    onClick = { letter = null },
                    label = { Text(stringResource(R.string.catalog_all_letters)) },
                )
                letters.forEach { L ->
                    FilterChip(
                        selected = letter == L,
                        onClick = { letter = if (letter == L) null else L },
                        label = { Text(L) },
                    )
                }
            }
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                POS_FILTERS.forEach { token ->
                    val label = if (token.isEmpty()) stringResource(R.string.catalog_all_pos) else token
                    FilterChip(
                        selected = pos == token,
                        onClick = { pos = token },
                        label = { Text(label) },
                    )
                }
            }
            Text(
                text = stringResource(R.string.catalog_count, filtered.size),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
            LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                items(filtered, key = { it.id }) { entry ->
                    ListItem(
                        headlineContent = { Text(entry.word) },
                        supportingContent = {
                            Text("${entry.displayPos} · ${entry.primaryBack}")
                        },
                        modifier = Modifier.clickable { onOpenWord(entry.id) },
                    )
                }
            }
        }
    }
}
