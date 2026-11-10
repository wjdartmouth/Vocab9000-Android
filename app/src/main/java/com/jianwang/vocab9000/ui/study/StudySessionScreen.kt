package com.jianwang.vocab9000.ui.study

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jianwang.vocab9000.AppContainer
import com.jianwang.vocab9000.R
import com.jianwang.vocab9000.data.entity.DeckEntity
import com.jianwang.vocab9000.data.repository.StatsService
import com.jianwang.vocab9000.domain.SRSGrade
import com.jianwang.vocab9000.domain.SRSService
import com.jianwang.vocab9000.domain.SRSState
import com.jianwang.vocab9000.domain.StudyItem
import com.jianwang.vocab9000.ui.theme.AgainOrange
import com.jianwang.vocab9000.ui.theme.EasyBlue
import com.jianwang.vocab9000.ui.theme.HardYellow
import com.jianwang.vocab9000.ui.theme.LearnedGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudySessionScreen(
    container: AppContainer,
    deckId: String,
    onDone: () -> Unit,
) {
    var deck by remember { mutableStateOf<DeckEntity?>(null) }
    var items by remember { mutableStateOf<List<StudyItem>>(emptyList()) }
    var index by remember { mutableIntStateOf(0) }
    var showBack by remember { mutableStateOf(false) }
    var graded by remember { mutableIntStateOf(0) }
    var dueAtStart by remember { mutableIntStateOf(0) }
    var lastHint by remember { mutableStateOf<String?>(null) }
    var enExtra by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun reload() {
        scope.launch {
            container.catalog.loadIfNeeded()
            val d = container.db.deckDao().getById(deckId) ?: return@launch
            deck = d
            val queue = container.seed.studyQueue(d, capped = true)
            items = queue
            dueAtStart = queue.count { it.isDue && !it.isNew }
            index = 0
            showBack = false
            graded = 0
        }
    }

    LaunchedEffect(deckId) { reload() }

    val current = items.getOrNull(index)

    LaunchedEffect(current?.id) {
        enExtra = null
        val word = current?.front ?: return@LaunchedEffect
        val (text, _) = container.definitions.definition(word)
        if (text.isNotBlank()) enExtra = text
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(StatsService.localizedDeckTitle(deck?.title.orEmpty())) },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LinearProgressIndicator(
                progress = {
                    if (items.isEmpty()) 0f
                    else index.toFloat() / items.size.coerceAtLeast(1)
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    stringResource(R.string.study_due_label, dueAtStart),
                    color = if (dueAtStart > 0) AgainOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                )
                lastHint?.let {
                    Text(
                        stringResource(R.string.study_prev, it),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (current == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (items.isEmpty()) stringResource(R.string.study_done_none)
                            else stringResource(R.string.study_session_done),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.study_reviewed, graded))
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onDone) { Text(stringResource(R.string.action_close)) }
                    }
                }
            } else {
                Text(
                    "${index + 1} / ${items.size}",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                ) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        StatusChip(current)
                        Spacer(Modifier.height(12.dp))
                        if (!showBack) {
                            current.pos?.let {
                                Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                            }
                            Text(
                                current.front,
                                style = MaterialTheme.typography.displaySmall,
                                textAlign = TextAlign.Center,
                            )
                        } else {
                            Text(
                                current.back,
                                style = MaterialTheme.typography.headlineMedium,
                                textAlign = TextAlign.Center,
                            )
                            enExtra?.let {
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }

                if (!showBack) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Button(
                            onClick = { showBack = true },
                            modifier = Modifier.weight(1f),
                        ) { Text(stringResource(R.string.study_flip)) }
                        OutlinedButton(
                            onClick = {
                                container.speech.speak(
                                    current.front,
                                    deck?.languageCode ?: container.prefs.ttsLanguage,
                                )
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
                            Text("  " + stringResource(R.string.study_speak))
                        }
                    }
                } else {
                    GradeRow(
                        item = current,
                        onGrade = { grade ->
                            scope.launch {
                                val preview = SRSService.schedule(
                                    SRSState(
                                        easeFactor = current.easeFactor,
                                        intervalDays = current.intervalDays,
                                        repetitions = current.repetitions,
                                        nextReviewDate = current.nextReviewDate,
                                        lastReviewed = null,
                                        isLearned = current.isLearned,
                                    ),
                                    grade,
                                )
                                lastHint = SRSService.formatIntervalHintDays(preview.intervalDays, grade)
                                container.seed.applyGrade(
                                    catalogKey = current.catalogKey,
                                    word = current.front,
                                    grade = grade,
                                    flashCardId = current.flashCardId,
                                )
                                graded++
                                showBack = false
                                index++
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(item: StudyItem) {
    val label = when {
        item.isNew -> stringResource(R.string.study_status_new)
        item.isDue -> stringResource(R.string.study_status_due)
        item.isLearned -> stringResource(R.string.study_status_learned)
        else -> stringResource(R.string.study_status_learning)
    }
    Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
}

@Composable
private fun GradeRow(item: StudyItem, onGrade: (SRSGrade) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            GradeButton(
                label = stringResource(R.string.srs_again),
                hint = SRSService.formatIntervalHintDays(0.0, SRSGrade.AGAIN),
                color = AgainOrange,
                modifier = Modifier.weight(1f),
                onClick = { onGrade(SRSGrade.AGAIN) },
            )
            GradeButton(
                label = stringResource(R.string.srs_hard),
                hint = SRSService.formatIntervalHintDays(item.intervalDays, SRSGrade.HARD),
                color = HardYellow,
                modifier = Modifier.weight(1f),
                onClick = { onGrade(SRSGrade.HARD) },
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            GradeButton(
                label = stringResource(R.string.srs_good),
                hint = SRSService.formatIntervalHintDays(item.intervalDays, SRSGrade.GOOD),
                color = LearnedGreen,
                modifier = Modifier.weight(1f),
                onClick = { onGrade(SRSGrade.GOOD) },
            )
            GradeButton(
                label = stringResource(R.string.srs_easy),
                hint = SRSService.formatIntervalHintDays(item.intervalDays, SRSGrade.EASY),
                color = EasyBlue,
                modifier = Modifier.weight(1f),
                onClick = { onGrade(SRSGrade.EASY) },
            )
        }
    }
}

@Composable
private fun GradeButton(
    label: String,
    hint: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = color),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label)
            Text(hint, style = MaterialTheme.typography.labelSmall)
        }
    }
}
