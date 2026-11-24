package ru.aiss83.comunalexpenses2.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import comunalexpenses2.shared.generated.resources.*
import kotlinx.datetime.Clock
import org.jetbrains.compose.resources.stringResource
import ru.aiss83.comunalexpenses2.data.ResourceData
import ru.aiss83.comunalexpenses2.ui.theme.ResourceBlue
import ru.aiss83.comunalexpenses2.ui.theme.ResourceOrange
import ru.aiss83.comunalexpenses2.ui.theme.ResourcePurple
import ru.aiss83.comunalexpenses2.ui.theme.ResourceRed
import ru.aiss83.comunalexpenses2.domain.ResourcesDataViewModel
import ru.aiss83.comunalexpenses2.utils.PlatformBackHandler
import ru.aiss83.comunalexpenses2.utils.formatDayKey
import ru.aiss83.comunalexpenses2.utils.toDayKey
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddResourcesDataScreen(
    viewModel: ResourcesDataViewModel,
    existingRecord: ResourceData? = null,
    previousRecord: ResourceData? = null,
    nextRecord: ResourceData? = null,
    onNavigateBack: () -> Unit
) {
    val isEditing = existingRecord != null

    // rememberSaveable: entered readings survive rotation / process death.
    var coldWaterValue by rememberSaveable { mutableStateOf(existingRecord?.coldWater?.toString() ?: "") }
    var hotWaterValue by rememberSaveable { mutableStateOf(existingRecord?.hotWater?.toString() ?: "") }
    var daykWhValue by rememberSaveable { mutableStateOf(existingRecord?.dayElectricity?.toString() ?: "") }
    var nightkWhValue by rememberSaveable { mutableStateOf(existingRecord?.nightElectricity?.toString() ?: "") }

    var showDiscardDialog by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<ValidationError?>(null) }

    // The record is identified by its day key, so the header always shows that day.
    val formattedDate = if (isEditing) {
        existingRecord.date.formatDayKey()
    } else {
        Clock.System.now().toEpochMilliseconds().toDayKey().formatDayKey()
    }

    val hasInput = coldWaterValue.isNotEmpty() || hotWaterValue.isNotEmpty() ||
            daykWhValue.isNotEmpty() || nightkWhValue.isNotEmpty()

    val handleBack = {
        if (!isEditing && hasInput) {
            showDiscardDialog = true
        } else {
            onNavigateBack()
        }
    }

    // System back must go through the same discard confirmation as the toolbar arrow.
    PlatformBackHandler(enabled = true, onBack = handleBack)

    val handleSave = saveLambda@{
        val coldVal = coldWaterValue.toLongOrNull() ?: 0
        val hotVal = hotWaterValue.toLongOrNull() ?: 0
        val dayVal = daykWhValue.toLongOrNull() ?: 0
        val nightVal = nightkWhValue.toLongOrNull() ?: 0

        // Values must stay inside the window between the previous and the next record,
        // otherwise the consumption chart shows a bogus negative/positive spike.
        if (previousRecord != null &&
            (coldVal < previousRecord.coldWater || hotVal < previousRecord.hotWater ||
             dayVal < previousRecord.dayElectricity || nightVal < previousRecord.nightElectricity)
        ) {
            validationError = ValidationError.LOWER_THAN_PREVIOUS
            return@saveLambda
        }
        if (nextRecord != null &&
            (coldVal > nextRecord.coldWater || hotVal > nextRecord.hotWater ||
             dayVal > nextRecord.dayElectricity || nightVal > nextRecord.nightElectricity)
        ) {
            validationError = ValidationError.HIGHER_THAN_NEXT
            return@saveLambda
        }
        // A shared record is locked: it was already sent and must not be changed.
        if (existingRecord?.shared == true) {
            validationError = ValidationError.LOCKED
            return@saveLambda
        }

        val data = ResourceData(
            id = existingRecord?.id ?: Uuid.random(),
            date = existingRecord?.date ?: Clock.System.now().toEpochMilliseconds().toDayKey(),
            coldWater = coldWaterValue.toLongOrNull() ?: 0,
            hotWater = hotWaterValue.toLongOrNull() ?: 0,
            dayElectricity = daykWhValue.toLongOrNull() ?: 0,
            nightElectricity = nightkWhValue.toLongOrNull() ?: 0
        )
        if (isEditing) {
            viewModel.updateResourceData(data)
        } else {
            viewModel.addResourcesData(data)
        }
        // The widget is refreshed by the ViewModel from the stored data (single source of truth).
        onNavigateBack()
    }

    // Discard confirmation dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(stringResource(Res.string.add_readings_discard_title)) },
            text = { Text(stringResource(Res.string.add_readings_discard_text)) },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onNavigateBack()
                }) {
                    Text(stringResource(Res.string.add_readings_discard_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(Res.string.add_readings_cancel))
                }
            }
        )
    }

    // Validation error dialog
    val currentError = validationError
    if (currentError != null) {
        AlertDialog(
            onDismissRequest = { validationError = null },
            title = { Text(stringResource(Res.string.add_readings_validation_title)) },
            text = {
                Text(
                    stringResource(
                        when (currentError) {
                            ValidationError.LOWER_THAN_PREVIOUS -> Res.string.add_readings_validation_text
                            ValidationError.HIGHER_THAN_NEXT -> Res.string.add_readings_validation_high_text
                            ValidationError.LOCKED -> Res.string.add_readings_locked_text
                        }
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { validationError = null }) {
                    Text(stringResource(Res.string.dialog_ok))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditing) stringResource(Res.string.add_readings_edit_title)
                        else stringResource(Res.string.add_readings_title)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = handleBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(Res.string.add_readings_back_content_desc))
                    }
                },
                actions = {
                    IconButton(onClick = handleSave) {
                        Icon(Icons.Rounded.Done, stringResource(Res.string.add_readings_save))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Date header
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.headlineSmall
            )

            // Water readings
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.WaterDrop, null, Modifier.size(20.dp), tint = ResourceBlue)
                Spacer(Modifier.size(6.dp))
                Text(
                    text = stringResource(Res.string.add_readings_water_section),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextField(
                    value = coldWaterValue,
                    onValueChange = {
                 coldWaterValue = it.toDigitsNoLeadingZero()
            },
                    label = { Text(stringResource(Res.string.add_readings_cold_water)) },
                    leadingIcon = { Icon(Icons.Rounded.WaterDrop, null, tint = ResourceBlue) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                TextField(
                    value = hotWaterValue,
                    onValueChange = {
                        hotWaterValue = it.toDigitsNoLeadingZero()
                    },
                    label = { Text(stringResource(Res.string.add_readings_hot_water)) },
                    leadingIcon = { Icon(Icons.Rounded.WaterDrop, null, tint = ResourceRed) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            // Validation warnings for water
            val coldCurr = coldWaterValue.toLongOrNull() ?: 0
            val hotCurr = hotWaterValue.toLongOrNull() ?: 0
            if (previousRecord != null && (coldCurr < previousRecord.coldWater || hotCurr < previousRecord.hotWater)) {
                Text(
                    text = stringResource(Res.string.add_readings_warning_lower),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Electricity readings
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.ElectricBolt, null, Modifier.size(20.dp), tint = ResourceOrange)
                Spacer(Modifier.size(6.dp))
                Text(
                    text = stringResource(Res.string.add_readings_electricity_section),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextField(
                    value = daykWhValue,
                    onValueChange = {
                        daykWhValue = it.toDigitsNoLeadingZero()
                    },
                    label = { Text(stringResource(Res.string.add_readings_kwh_day)) },
                    leadingIcon = { Icon(Icons.Rounded.ElectricBolt, null, tint = ResourceOrange) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                TextField(
                    value = nightkWhValue,
                    onValueChange = {
                        nightkWhValue = it.toDigitsNoLeadingZero()
                    },
                    label = { Text(stringResource(Res.string.add_readings_kwh_night)) },
                    leadingIcon = { Icon(Icons.Rounded.ElectricBolt, null, tint = ResourcePurple) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            // Validation warnings for electricity
            val dayCurr = daykWhValue.toLongOrNull() ?: 0
            val nightCurr = nightkWhValue.toLongOrNull() ?: 0
            if (previousRecord != null && (dayCurr < previousRecord.dayElectricity || nightCurr < previousRecord.nightElectricity)) {
                Text(
                    text = stringResource(Res.string.add_readings_warning_lower),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

/** Which boundary the entered readings violate. */
private enum class ValidationError {
    LOWER_THAN_PREVIOUS,
    HIGHER_THAN_NEXT,
    LOCKED
}

/** Keep only digits and strip leading zeros (except single "0"). */
private fun String.toDigitsNoLeadingZero(): String {
    val digits = filter { it.isDigit() }
    return if (digits.length > 1) digits.trimStart('0') else digits
}
