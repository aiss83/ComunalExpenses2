package ru.aiss83.comunalexpenses2.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ru.aiss83.comunalexpenses2.data.ResourceData
import ru.aiss83.comunalexpenses2.domain.ResourcesDataViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
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

    Column(
        modifier = Modifier.fillMaxHeight().padding(all = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.headlineSmall
            )
        }

        // Cold water
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            TextField(
                value = coldWaterValue,
                label = { Text(text = "Cold Water") },
                onValueChange = { newText -> coldWaterValue = newText.trimStart { it == '0' } },
                modifier = Modifier.weight(1.0f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.weight(0.1f))
            TextField(
                value = hotWaterValue,
                label = { Text(text = "Hot Water") },
                onValueChange = { newText -> hotWaterValue = newText.trimStart { it == '0' } },
                modifier = Modifier.weight(1.0f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        // Electricity
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            TextField(
                value = daykWhValue,
                label = { Text(text = "kWh Day") },
                onValueChange = { newText -> daykWhValue = newText.trimStart { it == '0' } },
                modifier = Modifier.weight(1.0f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.weight(0.1f))
            TextField(
                value = nightkWhValue,
                label = { Text(text = "kWh Night") },
                onValueChange = { newText -> nightkWhValue = newText.trimStart { it == '0' } },
                modifier = Modifier.weight(1.0f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(modifier = Modifier.weight(1.0f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            TextButton(
                modifier = Modifier.weight(1.0f),
                onClick = {
                    resourcesRecord.copy(
                        coldWater = coldWaterValue.toLong(),
                        hotWater = hotWaterValue.toLong(),
                        dayElectricity = daykWhValue.toLong(),
                        nightElectricity = nightkWhValue.toLong()
                    )
                    viewModel.addResourcesData(
                        ResourceData(
                            coldWater = coldWaterValue.toLong(),
                            hotWater = hotWaterValue.toLong(),
                            dayElectricity = daykWhValue.toLong(),
                            nightElectricity = nightkWhValue.toLong()
                        )
                    )
                    onNavigateBack()
                }
            ) {
                Text(text = "Save")
            }
            Spacer(modifier = Modifier.weight(0.1f))
            TextButton(
                modifier = Modifier.weight(1.0f),
                onClick = { onNavigateBack() }
            ) {
                Text(text = "Cancel")
            }
        }
    }
}
