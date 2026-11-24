package ru.aiss83.comunalexpenses2.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import comunalexpenses2.shared.generated.resources.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ru.aiss83.comunalexpenses2.data.SettingsData
import ru.aiss83.comunalexpenses2.data.ThemeMode
import ru.aiss83.comunalexpenses2.data.getAppVersion
import ru.aiss83.comunalexpenses2.domain.ImportResult
import ru.aiss83.comunalexpenses2.domain.ResourcesDataViewModel
import ru.aiss83.comunalexpenses2.domain.SaveSettingsResult
import ru.aiss83.comunalexpenses2.domain.SettingsViewModel
import ru.aiss83.comunalexpenses2.utils.PlatformBackHandler
import ru.aiss83.comunalexpenses2.utils.exportAndShareJson
import ru.aiss83.comunalexpenses2.utils.rememberJsonFilePicker

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
    resourcesDataViewModel: ResourcesDataViewModel,
    onNavigateBack: () -> Unit
) {
    val settings by viewModel.settingsData.collectAsState()
    val saveResult by viewModel.saveResult.collectAsState()

    // Initialise once from the persisted settings. Deliberately NOT re-synced on every
    // settings emission: saving the theme would otherwise wipe unsaved address/template input.
    // rememberSaveable keeps the draft alive across rotation.
    var streetValue by rememberSaveable { mutableStateOf(settings.street) }
    var houseValue by rememberSaveable { mutableStateOf(settings.house.toString()) }
    var flatValue by rememberSaveable { mutableStateOf(settings.flat.toString()) }
    var shareTemplateValue by rememberSaveable { mutableStateOf(settings.shareTemplate) }

    PlatformBackHandler(enabled = true, onBack = onNavigateBack)

    // Navigate back on successful save
    LaunchedEffect(saveResult) {
        if (saveResult is SaveSettingsResult.Success) {
            viewModel.clearSaveResult()
            onNavigateBack()
        }
    }

    // Snackbar and progress state
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    // Progress indicators for async operations
    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    // Pre-resolve strings used inside non-Composable lambdas
    val importedFmt = stringResource(Res.string.home_imported_count)
    val importedPartialFmt = stringResource(Res.string.settings_import_partial)
    val importInvalidFmt = stringResource(Res.string.settings_import_invalid_file)
    val importEmptyFmt = stringResource(Res.string.settings_import_empty_file)
    val importFailedFmt = stringResource(Res.string.settings_import_failed)
    val deleteAllDoneMsg = stringResource(Res.string.settings_delete_all_done)
    val exportTitle = stringResource(Res.string.home_export_title)

    // Import feedback — every attempt produces exactly one result (success or failure),
    // so the progress state can never get stuck and leave the buttons disabled.
    val importResult by resourcesDataViewModel.importResult.collectAsState()
    LaunchedEffect(importResult) {
        val result = importResult ?: return@LaunchedEffect
        isImporting = false
        val message = when (result) {
            is ImportResult.Success ->
                if (result.skipped > 0) {
                    importedPartialFmt.replaceFirst("%d", result.imported.toString())
                } else {
                    importedFmt.replaceFirst("%d", result.imported.toString())
                }

            ImportResult.InvalidFile -> importInvalidFmt
            ImportResult.NoValidRecords -> importEmptyFmt
            ImportResult.Failed -> importFailedFmt
        }
        snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
        resourcesDataViewModel.clearImportResult()
    }

    // File picker for import — sets progress on file selection
    val launchFilePicker = rememberJsonFilePicker { content ->
        if (content != null) {
            isImporting = true
            resourcesDataViewModel.importJson(content)
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
                    Text(stringResource(Res.string.dialog_ok))
                }
            }
        )
    }

    // Delete all confirmation dialog
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            icon = { Icon(Icons.Rounded.Delete, stringResource(Res.string.delete_dialog_icon_desc)) },
            title = { Text(stringResource(Res.string.delete_all_dialog_title)) },
            text = { Text(stringResource(Res.string.delete_all_dialog_text)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteAllDialog = false
                    isDeleting = true
                    resourcesDataViewModel.deleteAllData()
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = deleteAllDoneMsg,
                            duration = SnackbarDuration.Short
                        )
                        // Brief delay to let user see the progress indicator
                        delay(600)
                        isDeleting = false
                    }
                }) {
                    Text(stringResource(Res.string.delete_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text(stringResource(Res.string.delete_dialog_dismiss))
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .imePadding()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── 1. Property address ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(Res.string.settings_address_section),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(12.dp))

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

                    // Live address preview from current input — shares formatting with SettingsData
                    // instead of duplicating the "д./кв." assembly here.
                    val liveAddress = SettingsData(
                        street = streetValue.trim(),
                        house = parseSettingsInt(houseValue),
                        flat = parseSettingsInt(flatValue)
                    ).formatAddress()
                    if (liveAddress.isNotBlank()) {
                        Text(
                            text = "${stringResource(Res.string.settings_address_label)}: $liveAddress",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // ── Theme (compact, outside cards) ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(Res.string.settings_theme_label),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    ThemeSelector(
                        currentMode = settings.themeMode,
                        onModeSelected = { viewModel.setThemeMode(it) }
                    )
                }
            }

            // ── 2. Message template ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(Res.string.settings_template_section),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(12.dp))

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

            // ── 3. Data management ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(Res.string.settings_data_section),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            isExporting = true
                            val json = resourcesDataViewModel.exportJson()
                            exportAndShareJson(
                                json = json,
                                filename = "comunal_expenses_backup.json",
                                title = exportTitle
                            )
                            // Export is sync — clear immediately (share sheet takes over UI)
                            isExporting = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isExporting && !isImporting && !isDeleting
                    ) {
                        Icon(Icons.Rounded.FileDownload, contentDescription = null)
                        Text(
                            text = stringResource(Res.string.settings_export_button),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = { launchFilePicker() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isExporting && !isImporting && !isDeleting
                    ) {
                        Icon(Icons.Rounded.FileUpload, contentDescription = null)
                        Text(
                            text = stringResource(Res.string.settings_import_button),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = { showDeleteAllDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isExporting && !isImporting && !isDeleting,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Rounded.Delete, contentDescription = null)
                        Text(
                            text = stringResource(Res.string.settings_delete_all_button),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    // Progress indicators
                    if (isExporting || isImporting || isDeleting) {
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = when {
                                isExporting -> stringResource(Res.string.settings_export_button) + "…"
                                isImporting -> stringResource(Res.string.settings_import_button) + "…"
                                isDeleting -> stringResource(Res.string.settings_delete_all_button) + "…"
                                else -> ""
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── 4. About ──
            val appVersion = remember { getAppVersion() }
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(Res.string.settings_about_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(Res.string.app_name),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "${stringResource(Res.string.settings_about_version)}: $appVersion",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
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
