package ru.aiss83.comunalexpenses2.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import comunalexpenses2.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import ru.aiss83.comunalexpenses2.data.SettingsData
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

    // Initialize fields from settings
    var streetValue by remember { mutableStateOf(settings.street) }
    var houseValue by remember { mutableStateOf(settings.house.toString()) }
    var flatValue by remember { mutableStateOf(settings.flat.toString()) }
    var shareTemplateValue by remember { mutableStateOf(settings.shareTemplate) }

    // Sync fields when settings change (e.g. after returning from another screen)
    LaunchedEffect(settings) {
        streetValue = settings.street
        houseValue = settings.house.toString()
        flatValue = settings.flat.toString()
        shareTemplateValue = settings.shareTemplate
    }

    // Preview the template as-is so keywords are clearly visible
    val templatePreview = remember(shareTemplateValue) {
        shareTemplateValue.ifBlank { SettingsData.DEFAULT_SHARE_TEMPLATE }
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
                            onNavigateBack()
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

            // Display current formatted address as preview
            if (settings.isNotEmpty()) {
                Text(
                    text = "${stringResource(Res.string.settings_address_label)}: ${settings.formatAddress()}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

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
            Text(
                text = stringResource(Res.string.settings_keywords_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
