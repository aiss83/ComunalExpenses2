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

    // Sync fields when settings change (e.g. after returning from another screen)
    LaunchedEffect(settings) {
        streetValue = settings.street
        houseValue = settings.house.toString()
        flatValue = settings.flat.toString()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back to home"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.saveSettings(
                                street = streetValue,
                                house = parseSettingsInt(houseValue),
                                flat = parseSettingsInt(flatValue)
                            )
                            onNavigateBack()
                        }
                    ) {
                        Icon(Icons.Rounded.Done, "Save settings")
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
            TextField(
                value = streetValue,
                onValueChange = { streetValue = it },
                label = { Text("Street") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = houseValue,
                    onValueChange = { houseValue = it.filter { c -> c.isDigit() } },
                    label = { Text("House") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                TextField(
                    value = flatValue,
                    onValueChange = { flatValue = it.filter { c -> c.isDigit() } },
                    label = { Text("Flat") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            // Display current formatted address as preview
            if (settings.isNotEmpty()) {
                Text(
                    text = "Address: ${settings.formatAddress()}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
