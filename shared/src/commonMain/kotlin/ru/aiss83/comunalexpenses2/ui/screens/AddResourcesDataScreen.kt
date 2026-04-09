package ru.aiss83.comunalexpenses2.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import comunalexpenses2.shared.generated.resources.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import ru.aiss83.comunalexpenses2.data.ResourceData
import ru.aiss83.comunalexpenses2.domain.ResourcesDataViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddResourcesDataScreen(
    viewModel: ResourcesDataViewModel,
    onNavigateBack: () -> Unit
) {
    var coldWaterValue by remember { mutableStateOf("0") }
    var hotWaterValue by remember { mutableStateOf("0") }
    var daykWhValue by remember { mutableStateOf("0") }
    var nightkWhValue by remember { mutableStateOf("0") }

    val resourcesRecord = remember { ResourceData() }

    // Get current date for display
    val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val formattedDate = "${currentDate.dayOfMonth.toString().padStart(2, '0')}." +
            "${currentDate.monthNumber.toString().padStart(2, '0')}." +
            "${currentDate.year}"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.add_readings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.add_readings_back_content_desc))
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.addResourcesData(
                                ResourceData(
                                    coldWater = coldWaterValue.toLongOrNull() ?: 0,
                                    hotWater = hotWaterValue.toLongOrNull() ?: 0,
                                    dayElectricity = daykWhValue.toLongOrNull() ?: 0,
                                    nightElectricity = nightkWhValue.toLongOrNull() ?: 0
                                )
                            )
                            onNavigateBack()
                        }
                    ) {
                        Text(stringResource(Res.string.add_readings_save))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date header
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.headlineSmall
            )

            // Water readings
            Text(
                text = stringResource(Res.string.add_readings_water_section),
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextField(
                    value = coldWaterValue,
                    onValueChange = { coldWaterValue = it.filter { c -> c.isDigit() }.trimStart { it == '0' } },
                    label = { Text(stringResource(Res.string.add_readings_cold_water)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                TextField(
                    value = hotWaterValue,
                    onValueChange = { hotWaterValue = it.filter { c -> c.isDigit() }.trimStart { it == '0' } },
                    label = { Text(stringResource(Res.string.add_readings_hot_water)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            // Electricity readings
            Text(
                text = stringResource(Res.string.add_readings_electricity_section),
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextField(
                    value = daykWhValue,
                    onValueChange = { daykWhValue = it.filter { c -> c.isDigit() }.trimStart { it == '0' } },
                    label = { Text(stringResource(Res.string.add_readings_kwh_day)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                TextField(
                    value = nightkWhValue,
                    onValueChange = { nightkWhValue = it.filter { c -> c.isDigit() }.trimStart { it == '0' } },
                    label = { Text(stringResource(Res.string.add_readings_kwh_night)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Cancel button at bottom
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateBack
            ) {
                Text(stringResource(Res.string.add_readings_cancel))
            }
        }
    }
}
