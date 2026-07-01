package ru.aiss83.comunalexpenses2.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import comunalexpenses2.shared.generated.resources.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ru.aiss83.comunalexpenses2.data.ResourceData
import ru.aiss83.comunalexpenses2.data.SettingsData
import ru.aiss83.comunalexpenses2.domain.ResourcesDataViewModel
import ru.aiss83.comunalexpenses2.utils.rememberJsonFilePicker
import ru.aiss83.comunalexpenses2.utils.saveJsonToFile
import ru.aiss83.comunalexpenses2.utils.shareText

@OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    allResourceData: List<ResourceData>,
    userSettings: SettingsData,
    viewModel: ResourcesDataViewModel,
    onNavigateToAddExpenses: () -> Unit,
    onNavigateToEditExpenses: (ResourceData) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showSwipeHint by rememberSaveable { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var searchActive by remember { mutableStateOf(false) }

    val filteredData = if (searchQuery.isBlank()) allResourceData
        else allResourceData.filter {
            it.formatDate().contains(searchQuery, ignoreCase = true) ||
            it.coldWater.toString().contains(searchQuery) ||
            it.hotWater.toString().contains(searchQuery) ||
            it.dayElectricity.toString().contains(searchQuery) ||
            it.nightElectricity.toString().contains(searchQuery)
        }

    // Delete confirmation
    var openRemoveDialog by rememberSaveable { mutableStateOf(false) }
    var boundToRemove by rememberSaveable { mutableStateOf("") }
    var recordToRemove by remember { mutableStateOf<ResourceData?>(null) }

    // Delete all confirmation
    var openDeleteAllDialog by remember { mutableStateOf(false) }

    // File picker for JSON import
    val pickJsonFile = rememberJsonFilePicker { content ->
        if (content != null) {
            viewModel.importJson(content)
        }
    }

    val shareTitle = stringResource(Res.string.home_share_title)
    val exportSavedMsg = stringResource(Res.string.home_export_saved)
    val undoLabel = stringResource(Res.string.home_undo)
    val deleteUndoneMsg = stringResource(Res.string.home_delete_undone)
    val searchHint = stringResource(Res.string.home_search_hint)
    val sharedOkMsg = stringResource(Res.string.home_shared_ok)
    val importedMsg = stringResource(Res.string.home_imported_count)

    // Observe import result
    LaunchedEffect(Unit) {
        viewModel.importedCount.collectLatest { count ->
            if (count != null) {
                snackbarHostState.showSnackbar(
                    message = importedMsg.replace("%d", count.toString()),
                    duration = SnackbarDuration.Short
                )
                viewModel.clearImportedCount()
            }
        }
    }

    // Share confirmation
    var openShareDialog by remember { mutableStateOf(false) }
    var recordToShare by remember { mutableStateOf<ResourceData?>(null) }

    val shareRecord = { id: Uuid ->
        val record = allResourceData.find { it.id == id }
        if (record != null) {
            recordToShare = record
            openShareDialog = true
        }
    }

    val confirmShare = {
        val record = recordToShare
        if (record != null) {
            val shareTextContent = SettingsData.applyShareTemplate(
                template = userSettings.shareTemplate,
                dayElectricity = record.dayElectricity,
                nightElectricity = record.nightElectricity,
                coldWater = record.coldWater,
                hotWater = record.hotWater,
                flatNumber = userSettings.flat
            )
            shareText(shareTextContent, shareTitle)
            viewModel.markAsShared(record.id)
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = sharedOkMsg,
                    duration = SnackbarDuration.Short
                )
            }
        }
        openShareDialog = false
        recordToShare = null
    }

    val dismissShare = {
        openShareDialog = false
        recordToShare = null
    }

    val removeRecord = { record: ResourceData ->
        recordToRemove = record
        boundToRemove = record.id.toString()
        openRemoveDialog = true
    }

    val dismissRemove = {
        openRemoveDialog = false
        boundToRemove = ""
        recordToRemove = null
    }

    val confirmRemove = {
        openRemoveDialog = false
        val record = recordToRemove
        if (record != null) {
            viewModel.deleteResourceData(record.id)
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = deleteUndoneMsg,
                    actionLabel = undoLabel,
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.undoDeleteResourceData(record)
                }
            }
        }
        boundToRemove = ""
        recordToRemove = null
    }

    val exportAllToJson = {
        val json = viewModel.exportJson()
        saveJsonToFile(json.encodeToByteArray(), "communal_expenses_backup.json")
        scope.launch {
            snackbarHostState.showSnackbar(
                message = exportSavedMsg,
                duration = SnackbarDuration.Short
            )
        }
        Unit
    }

    if (openRemoveDialog) {
        AlertDialog(
            onDismissRequest = dismissRemove,
            icon = { Icon(Icons.Rounded.Delete, contentDescription = null) },
            title = { Text(stringResource(Res.string.delete_dialog_title)) },
            text = { Text(stringResource(Res.string.delete_dialog_text)) },
            confirmButton = {
                TextButton(onClick = confirmRemove) {
                    Text(stringResource(Res.string.delete_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = dismissRemove) {
                    Text(stringResource(Res.string.delete_dialog_dismiss))
                }
            }
        )
    }

    if (openShareDialog) {
        AlertDialog(
            onDismissRequest = dismissShare,
            icon = { Icon(Icons.Rounded.Share, contentDescription = null) },
            title = { Text(stringResource(Res.string.share_dialog_title)) },
            text = { Text(stringResource(Res.string.share_dialog_text)) },
            confirmButton = {
                TextButton(onClick = confirmShare) {
                    Text(stringResource(Res.string.share_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = dismissShare) {
                    Text(stringResource(Res.string.share_dialog_dismiss))
                }
            }
        )
    }

    if (openDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { openDeleteAllDialog = false },
            icon = { Icon(Icons.Rounded.Delete, contentDescription = null) },
            title = { Text(stringResource(Res.string.delete_all_dialog_title)) },
            text = { Text(stringResource(Res.string.delete_all_dialog_text)) },
            confirmButton = {
                TextButton(onClick = {
                    openDeleteAllDialog = false
                    viewModel.deleteAllData()
                }) {
                    Text(stringResource(Res.string.delete_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { openDeleteAllDialog = false }) {
                    Text(stringResource(Res.string.delete_dialog_dismiss))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (searchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text(searchHint) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else {
                        Text(stringResource(Res.string.home_title))
                    }
                },
                actions = {
                    IconButton(onClick = exportAllToJson) {
                        Icon(Icons.Rounded.FileDownload, stringResource(Res.string.home_export_content_desc))
                    }
                    IconButton(onClick = pickJsonFile) {
                        Icon(Icons.Rounded.FileUpload, stringResource(Res.string.home_import_content_desc))
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Rounded.Settings, stringResource(Res.string.home_settings_content_desc))
                    }
                    if (allResourceData.isNotEmpty()) {
                        IconButton(onClick = { openDeleteAllDialog = true }) {
                            Icon(Icons.Rounded.Delete, stringResource(Res.string.home_clear_all_content_desc))
                        }
                    }
                    IconButton(onClick = onNavigateToStats) {
                        Icon(Icons.AutoMirrored.Rounded.ShowChart, stringResource(Res.string.home_stats_content_desc))
                    }
                    IconButton(onClick = { searchActive = !searchActive; searchQuery = "" }) {
                        Icon(Icons.Rounded.Search, stringResource(Res.string.home_search_content_desc))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddExpenses) {
                Icon(Icons.Rounded.Add, stringResource(Res.string.home_add_content_desc))
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { innerPadding ->
        if (allResourceData.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(Res.string.home_empty_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(Res.string.home_empty_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (showSwipeHint) {
                        item(key = "swipe_hint") {
                            AnimatedVisibility(
                                visible = showSwipeHint,
                                exit = fadeOut()
                            ) {
                                Text(
                                    text = stringResource(Res.string.home_swipe_hint),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    items(filteredData, key = { it.id }) { item ->
                        SwipeableResourcesCard(
                            record = item,
                            onEdit = {
                                if (!item.shared) onNavigateToEditExpenses(item)
                            },
                            onDataRemove = {
                                showSwipeHint = false
                                if (!item.shared) removeRecord(item)
                            },
                            onShare = shareRecord
                        )
                    }
                }
        }
    }
}

@OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableResourcesCard(
    record: ResourceData,
    onEdit: () -> Unit,
    onDataRemove: () -> Unit,
    onShare: (id: Uuid) -> Unit
) {
    val canSwipe = !record.shared

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart && canSwipe) {
                onDataRemove()
                false
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = Modifier.padding(8.dp),
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = canSwipe,
        backgroundContent = {
            if (canSwipe) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CardDefaults.shape)
                        .background(Color(0xFFE53935))
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = stringResource(Res.string.home_delete_content_desc),
                        tint = Color.White
                    )
                }
            }
        }
    ) {
        ResourcesCard(
            record = record,
            onEdit = onEdit,
            onDataRemove = onDataRemove,
            onShare = onShare
        )
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun ResourcesCard(
    record: ResourceData,
    onEdit: () -> Unit,
    onDataRemove: () -> Unit,
    onShare: (id: Uuid) -> Unit
) {
    val labelStyle = MaterialTheme.typography.labelMedium
    val valueStyle = MaterialTheme.typography.titleMedium
    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        elevation = CardDefaults.cardElevation(5.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Date + actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (record.shared) {
                        Icon(
                            Icons.Rounded.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = iconTint
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(
                        text = record.formatDate(),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                Row {
                    if (!record.shared) {
                        IconButton(onClick = { onEdit() }) {
                            Icon(Icons.Rounded.Edit, stringResource(Res.string.home_edit_content_desc))
                        }
                    }
                    IconButton(onClick = { onShare(record.id) }) {
                        Icon(Icons.Rounded.Share, stringResource(Res.string.home_share_content_desc))
                    }
                    if (!record.shared) {
                        IconButton(onClick = { onDataRemove() }) {
                            Icon(Icons.Rounded.Delete, stringResource(Res.string.home_delete_content_desc))
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = iconTint.copy(alpha = 0.15f))

            // Water section
            Row(modifier = Modifier.fillMaxWidth()) {
                MetricCell(
                    icon = Icons.Rounded.WaterDrop,
                    label = stringResource(Res.string.home_cold_water),
                    value = record.coldWater.toString(),
                    labelStyle = labelStyle,
                    valueStyle = valueStyle,
                    iconTint = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
                MetricCell(
                    icon = Icons.Rounded.WaterDrop,
                    label = stringResource(Res.string.home_hot_water),
                    value = record.hotWater.toString(),
                    labelStyle = labelStyle,
                    valueStyle = valueStyle,
                    iconTint = Color(0xFFF44336),
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = iconTint.copy(alpha = 0.15f))

            // Electricity section
            Row(modifier = Modifier.fillMaxWidth()) {
                MetricCell(
                    icon = Icons.Rounded.ElectricBolt,
                    label = stringResource(Res.string.home_kwh_day),
                    value = record.dayElectricity.toString(),
                    labelStyle = labelStyle,
                    valueStyle = valueStyle,
                    iconTint = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
                MetricCell(
                    icon = Icons.Rounded.ElectricBolt,
                    label = stringResource(Res.string.home_kwh_night),
                    value = record.nightElectricity.toString(),
                    labelStyle = labelStyle,
                    valueStyle = valueStyle,
                    iconTint = Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricCell(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    labelStyle: androidx.compose.ui.text.TextStyle,
    valueStyle: androidx.compose.ui.text.TextStyle,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(18.dp), tint = iconTint)
        Spacer(Modifier.width(4.dp))
        Column {
            Text(label, style = labelStyle, color = iconTint)
            Text(value, style = valueStyle)
        }
    }
}
