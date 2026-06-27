package ru.aiss83.comunalexpenses2.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import comunalexpenses2.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import ru.aiss83.comunalexpenses2.data.SettingsData
import ru.aiss83.comunalexpenses2.data.ThemeMode
import ru.aiss83.comunalexpenses2.domain.SaveSettingsResult
import ru.aiss83.comunalexpenses2.domain.SettingsViewModel

/**
 * Parses a string that may have leading zeros into an Int.
 * Returns 0 for empty or non-numeric input.
 */
fun parseSettingsInt(text: String): Int {
    val trimmed = text.trimStart('0')
    return trimmed.toIntOrNull() ?: 0
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val settings by viewModel.settingsData.collectAsState()
    val saveResult by viewModel.saveResult.collectAsState()

    // Initialize fields from settings
    var streetValue by remember { mutableStateOf(settings.street) }
    var houseValue by remember { mutableStateOf(settings.house.toString()) }
    var flatValue by remember { mutableStateOf(settings.flat.toString()) }
    var shareTemplateValue by remember { mutableStateOf(settings.shareTemplate) }

    // Sync fields when settings change (e.g. on first load)
    LaunchedEffect(settings) {
        streetValue = settings.street
        houseValue = settings.house.toString()
        flatValue = settings.flat.toString()
        shareTemplateValue = settings.shareTemplate
    }

    // Navigate back on successful save
    LaunchedEffect(saveResult) {
        if (saveResult is SaveSettingsResult.Success) {
            viewModel.clearSaveResult()
            onNavigateBack()
        }
    }

    // Template preview: raw + filled example
    val templatePreview = remember(shareTemplateValue) {
        shareTemplateValue.ifBlank { SettingsData.DEFAULT_SHARE_TEMPLATE }
    }
    val templateExample = remember(shareTemplateValue) {
        val tpl = shareTemplateValue.ifBlank { SettingsData.DEFAULT_SHARE_TEMPLATE }
        SettingsData.applyShareTemplate(tpl, 150, 80, 200, 100, 42)
    }

    // Validation error dialog
    if (saveResult is SaveSettingsResult.Error) {
        AlertDialog(
            onDismissRequest = { viewModel.clearSaveResult() },
            title = { Text(stringResource(Res.string.add_readings_validation_title)) },
            text = { Text((saveResult as SaveSettingsResult.Error).message) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearSaveResult() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(Res.string.settings_back_content_desc)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.saveSettings(
                                street = streetValue,
                                house = parseSettingsInt(houseValue),
                                flat = parseSettingsInt(flatValue),
                                shareTemplate = shareTemplateValue.ifBlank { SettingsData.DEFAULT_SHARE_TEMPLATE }
                            )
                        }
                    ) {
                        Icon(Icons.Rounded.Done, stringResource(Res.string.settings_save_content_desc))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Address section
            TextField(
                value = streetValue,
                onValueChange = { streetValue = it },
                label = { Text(stringResource(Res.string.settings_street)) },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = houseValue,
                    onValueChange = { houseValue = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(Res.string.settings_house)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                TextField(
                    value = flatValue,
                    onValueChange = { flatValue = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(Res.string.settings_flat)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            // Live address preview from current input
            val liveStreet = streetValue.trim()
            val liveHouse = parseSettingsInt(houseValue)
            val liveFlat = parseSettingsInt(flatValue)
            if (liveStreet.isNotBlank() || liveHouse > 0 || liveFlat > 0) {
                val parts = mutableListOf<String>()
                if (liveStreet.isNotBlank()) parts += liveStreet
                if (liveHouse > 0) parts += "д. $liveHouse"
                if (liveFlat > 0) parts += "кв. $liveFlat"
                Text(
                    text = "${stringResource(Res.string.settings_address_label)}: ${parts.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Theme section
            Text(
                text = stringResource(Res.string.settings_theme_label),
                style = MaterialTheme.typography.titleMedium
            )

            ThemeSelector(
                currentMode = settings.themeMode,
                onModeSelected = { viewModel.setThemeMode(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Share template section
            TextField(
                value = shareTemplateValue,
                onValueChange = { shareTemplateValue = it },
                label = { Text(stringResource(Res.string.settings_share_template)) },
                placeholder = { Text(stringResource(Res.string.settings_template_hint)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Template preview with keyword placeholders
            Text(
                text = stringResource(Res.string.settings_template_preview) + ":",
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = templatePreview,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
            // Filled example
            Text(
                text = stringResource(Res.string.settings_template_example) + ":",
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = templateExample,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
            Text(
                text = stringResource(Res.string.settings_keywords_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ThemeSelector(
    currentMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    val modes = listOf(
        ThemeMode.LIGHT to stringResource(Res.string.settings_theme_light),
        ThemeMode.DARK to stringResource(Res.string.settings_theme_dark),
        ThemeMode.SYSTEM to stringResource(Res.string.settings_theme_system)
    )

    Column {
        modes.forEach { (mode, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = currentMode == mode,
                        onClick = { onModeSelected(mode) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentMode == mode,
                    onClick = null // handled by selectable on Row
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
