package ru.aiss83.comunalexpenses2.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.aiss83.comunalexpenses2.domain.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val settings by viewModel.settingsData.collectAsState()

    var streetValue by remember { mutableStateOf(settings.street) }
    var houseValue by remember { mutableStateOf(settings.house.toString()) }
    var flatValue by remember { mutableStateOf(settings.flat.toString()) }

    fun textLeadZerosToInt(text: String): Int {
        val value = text.trimStart('0')
        if (value.isNotEmpty() && value.isNotBlank()) {
            return value.toIntOrNull() ?: 0
        }
        return 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Settings") },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back to home"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.saveSettings(streetValue, textLeadZerosToInt(houseValue), textLeadZerosToInt(flatValue))
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Done,
                            contentDescription = "Save settings"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            SettingsContent(
                streetValue = streetValue,
                streetValueChanged = { text -> streetValue = text },
                houseValue = houseValue,
                houseValueChanged = { text -> houseValue = text },
                flatValue = flatValue,
                flatValueChanged = { text -> flatValue = text }
            )
        }
    }
}

@Composable
fun SettingsContent(
    streetValue: String,
    streetValueChanged: (String) -> Unit = {},
    houseValue: String,
    houseValueChanged: (String) -> Unit = {},
    flatValue: String,
    flatValueChanged: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier.padding(4.dp).wrapContentSize(Alignment.TopStart)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(1.0f)) {
                TextField(
                    value = streetValue,
                    onValueChange = streetValueChanged,
                    label = { Text(text = "Street") }
                )
            }
            Row {
                TextField(
                    value = houseValue,
                    onValueChange = houseValueChanged,
                    label = { Text(text = "House") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.5f)
                )
                TextField(
                    value = flatValue,
                    onValueChange = flatValueChanged,
                    label = { Text(text = "Flat") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.5f)
                )
            }
        }
    }
}
