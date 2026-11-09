package com.jianwang.vocab9000.ui.settings

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.jianwang.vocab9000.AppContainer
import com.jianwang.vocab9000.R
import com.jianwang.vocab9000.util.NotificationHelper
import kotlinx.coroutines.launch

private val TTS_LANGS = listOf(
    "en" to R.string.lang_en,
    "ja" to R.string.lang_ja,
    "zh-CN" to R.string.lang_zh,
    "ko" to R.string.lang_ko,
    "fr" to R.string.lang_fr,
    "es" to R.string.lang_es,
)

private val UI_LANGS = listOf(
    "ja" to R.string.ui_lang_ja,
    "en" to R.string.ui_lang_en,
    "zh-Hans" to R.string.ui_lang_zh_hans,
    "zh-Hant" to R.string.ui_lang_zh_hant,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    container: AppContainer,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val prefs = container.prefs
    var notifEnabled by remember { mutableStateOf(prefs.notificationsEnabled) }
    var hour by remember { mutableIntStateOf(prefs.reminderHour) }
    var minute by remember { mutableIntStateOf(prefs.reminderMinute) }
    var ttsLang by remember { mutableStateOf(prefs.ttsLanguage) }
    var uiLang by remember { mutableStateOf(prefs.uiLanguage) }
    var status by remember { mutableStateOf<String?>(null) }
    var ttsExpanded by remember { mutableStateOf(false) }
    var uiExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            prefs.notificationsEnabled = true
            notifEnabled = true
            scope.launch {
                val due = container.stats.snapshot().dueToday
                NotificationHelper.scheduleDaily(context, hour, minute, due)
            }
            status = context.getString(R.string.settings_reminder_on)
        } else {
            notifEnabled = false
            prefs.notificationsEnabled = false
            status = context.getString(R.string.settings_reminder_denied)
        }
    }

    fun reschedule() {
        scope.launch {
            val due = try {
                container.stats.snapshot().dueToday
            } catch (_: Exception) {
                0
            }
            if (prefs.notificationsEnabled) {
                NotificationHelper.scheduleDaily(context, hour, minute, due)
            } else {
                NotificationHelper.cancel(context)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.tab_settings)) }) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                stringResource(R.string.settings_notifications),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary,
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_reminder_toggle)) },
                supportingContent = { Text(stringResource(R.string.settings_notifications_footer)) },
                trailingContent = {
                    Switch(
                        checked = notifEnabled,
                        onCheckedChange = { on ->
                            if (on) {
                                if (Build.VERSION.SDK_INT >= 33) {
                                    val granted = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS,
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (!granted) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        return@Switch
                                    }
                                }
                                prefs.notificationsEnabled = true
                                notifEnabled = true
                                reschedule()
                                status = context.getString(R.string.settings_reminder_on)
                            } else {
                                prefs.notificationsEnabled = false
                                notifEnabled = false
                                NotificationHelper.cancel(context)
                                status = context.getString(R.string.settings_reminder_off)
                            }
                        },
                    )
                },
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_reminder_time)) },
                supportingContent = {
                    Text("%02d:%02d".format(hour, minute))
                },
                modifier = Modifier.clickable(enabled = notifEnabled) {
                    TimePickerDialog(
                        context,
                        { _, h, m ->
                            hour = h
                            minute = m
                            prefs.reminderHour = h
                            prefs.reminderMinute = m
                            reschedule()
                            status = context.getString(R.string.settings_time_updated)
                        },
                        hour,
                        minute,
                        true,
                    ).show()
                },
            )

            Text(
                stringResource(R.string.settings_tts),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary,
            )
            ExposedDropdownMenuBox(
                expanded = ttsExpanded,
                onExpandedChange = { ttsExpanded = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                OutlinedTextField(
                    value = stringResource(TTS_LANGS.firstOrNull { it.first == ttsLang }?.second ?: R.string.lang_en),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.settings_tts_language)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(ttsExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(expanded = ttsExpanded, onDismissRequest = { ttsExpanded = false }) {
                    TTS_LANGS.forEach { (code, label) ->
                        DropdownMenuItem(
                            text = { Text(stringResource(label)) },
                            onClick = {
                                ttsLang = code
                                prefs.ttsLanguage = code
                                container.speech.setLanguage(code)
                                ttsExpanded = false
                            },
                        )
                    }
                }
            }

            Text(
                stringResource(R.string.settings_ui_language),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary,
            )
            ExposedDropdownMenuBox(
                expanded = uiExpanded,
                onExpandedChange = { uiExpanded = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                OutlinedTextField(
                    value = stringResource(UI_LANGS.firstOrNull { it.first == uiLang }?.second ?: R.string.ui_lang_ja),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.settings_ui_language)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(uiExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                )
                ExposedDropdownMenu(expanded = uiExpanded, onDismissRequest = { uiExpanded = false }) {
                    UI_LANGS.forEach { (code, label) ->
                        DropdownMenuItem(
                            text = { Text(stringResource(label)) },
                            onClick = {
                                uiLang = code
                                prefs.uiLanguage = code
                                uiExpanded = false
                                status = context.getString(R.string.settings_ui_lang_hint)
                            },
                        )
                    }
                }
            }

            Text(
                stringResource(R.string.settings_deeplink),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary,
            )
            ListItem(
                headlineContent = { Text("vocab9000://study") },
                supportingContent = { Text(stringResource(R.string.settings_deeplink_study)) },
            )

            status?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}
