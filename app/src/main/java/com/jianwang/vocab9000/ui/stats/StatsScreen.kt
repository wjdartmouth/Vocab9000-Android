package com.jianwang.vocab9000.ui.stats

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jianwang.vocab9000.AppContainer
import com.jianwang.vocab9000.R
import com.jianwang.vocab9000.data.repository.StudyStatsSnapshot
import com.jianwang.vocab9000.ui.theme.BrandIndigo
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    container: AppContainer,
    modifier: Modifier = Modifier,
) {
    var snap by remember { mutableStateOf<StudyStatsSnapshot?>(null) }

    LaunchedEffect(Unit) {
        container.catalog.loadIfNeeded()
        snap = container.stats.snapshot()
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.tab_stats)) }) },
    ) { padding ->
        val s = snap
        if (s == null) {
            Text(stringResource(R.string.loading), Modifier.padding(padding).padding(16.dp))
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
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(stringResource(R.string.stats_mastered), "${s.learnedCount}", Modifier.weight(1f))
                StatCard(stringResource(R.string.stats_due_today), "${s.dueToday}", Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(stringResource(R.string.stats_streak), "${s.streakDays}", Modifier.weight(1f))
                StatCard(
                    stringResource(R.string.stats_reviewed_today),
                    "${s.reviewedToday}",
                    Modifier.weight(1f),
                )
            }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.stats_catalog_total, s.catalogTotal))
                    Text(stringResource(R.string.stats_in_progress, s.inProgress))
                    val last = s.lastStudied?.let {
                        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(it))
                    } ?: "—"
                    Text(stringResource(R.string.stats_last_studied, last))
                }
            }

            Text(stringResource(R.string.stats_by_letter), style = MaterialTheme.typography.titleMedium)
            LetterChart(s.byLetter.map { it.letter to it.ratio })

            Text(stringResource(R.string.stats_by_deck), style = MaterialTheme.typography.titleMedium)
            s.byDeck.take(12).forEach { deck ->
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(deck.title, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            stringResource(R.string.stats_learned_of, deck.learned, deck.total),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                    LinearProgressIndicator(
                        progress = { deck.ratio.toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier) {
    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun LetterChart(data: List<Pair<String, Double>>) {
    val barColor = BrandIndigo
    Card(Modifier.fillMaxWidth()) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(12.dp)
        ) {
            if (data.isEmpty()) return@Canvas
            val barWidth = size.width / (data.size * 1.4f)
            data.forEachIndexed { i, (_, ratio) ->
                val h = (size.height * ratio.toFloat()).coerceAtLeast(2f)
                val x = i * barWidth * 1.4f
                drawRect(
                    color = barColor,
                    topLeft = Offset(x, size.height - h),
                    size = Size(barWidth, h),
                )
            }
        }
        Text(
            data.joinToString(" ") { it.first },
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
