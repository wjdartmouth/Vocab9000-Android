package com.jianwang.vocab9000.ui.decks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardEditorScreen(
    container: AppContainer,
    deckId: String,
    cardId: String?,
    onDone: () -> Unit,
) {
    var front by remember { mutableStateOf("") }
    var back by remember { mutableStateOf("") }
    var pos by remember { mutableStateOf("") }
    var example by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(cardId) {
        if (cardId != null) {
            val card = container.db.flashCardDao().getById(cardId)
            if (card != null) {
                front = card.front
                back = card.back
                pos = card.pos.orEmpty()
                example = card.example.orEmpty()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (cardId == null) R.string.card_add else R.string.card_edit
                        )
                    )
                },
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
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = front,
                onValueChange = { front = it },
                label = { Text(stringResource(R.string.card_front)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = back,
                onValueChange = { back = it },
                label = { Text(stringResource(R.string.card_back)) },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = pos,
                onValueChange = { pos = it },
                label = { Text(stringResource(R.string.card_pos)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = example,
                onValueChange = { example = it },
                label = { Text(stringResource(R.string.card_example)) },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = {
                    if (front.isBlank() || back.isBlank()) return@Button
                    scope.launch {
                        val existing = cardId?.let { container.db.flashCardDao().getById(it) }
                        val card = (existing ?: FlashCardEntity(
                            deckId = deckId,
                            front = front.trim(),
                            back = back.trim(),
                        )).copy(
                            front = front.trim(),
                            back = back.trim(),
                            pos = pos.ifBlank { null },
                            example = example.ifBlank { null },
                            updatedAt = System.currentTimeMillis(),
                        )
                        container.db.flashCardDao().upsert(card)
                        onDone()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.action_save))
            }
        }
    }
}
