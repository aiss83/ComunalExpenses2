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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
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
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ru.aiss83.comunalexpenses2.data.ResourceData
import ru.aiss83.comunalexpenses2.data.SettingsData
import ru.aiss83.comunalexpenses2.domain.ResourcesDataViewModel
import ru.aiss83.comunalexpenses2.utils.exportAndShareJson
import ru.aiss83.comunalexpenses2.utils.rememberJsonFilePicker
import ru.aiss83.comunalexpenses2.utils.shareText

@OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    allResourceData: List<ResourceData>,
    userSettings: SettingsData,
    viewModel: ResourcesDataViewModel,
    onNavigateToAddExpenses: () -> Unit,
    onNavigateToEditExpenses: (ResourceData) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showSwipeHint by rememberSaveable { mutableStateOf(true) }

    // File picker for JSON import
    val pickJsonFile = rememberJsonFilePicker { content ->
        if (content != null) {
            viewModel.importJson(content)
        }
    }

    val shareTitle = stringResource(Res.string.home_share_title)
    val exportTitle = stringResource(Res.string.home_export_title)
    val undoLabel = stringResource(Res.string.home_undo)
    val deleteUndoneMsg = stringResource(Res.string.home_delete_undone)

    val handleDelete = { record: ResourceData ->
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

    val shareRecord = { id: Uuid ->
        val record = allResourceData.find { it.id == id }
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
        }
    }

    val exportAllToJson = {
        val json = viewModel.exportJson()
        val filename = "communal_expenses_backup.json"
        exportAndShareJson(json, filename, exportTitle)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.home_title)) },
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
            PullToRefreshBox(
                isRefreshing = false,
                onRefresh = {
                    // Refresh is automatic via Flow — just trigger a brief indicator
                },
                modifier = Modifier.padding(innerPadding)
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Swipe hint
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

                    items(allResourceData, key = { it.id }) { item ->
                        SwipeableResourcesCard(
                            record = item,
                            onEdit = { onNavigateToEditExpenses(item) },
                            onDataRemove = {
                                showSwipeHint = false
                                handleDelete(item)
                            },
                            onShare = shareRecord
                        )
                    }
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
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
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
        enableDismissFromEndToStart = true,
        backgroundContent = {
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
    val rowsModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp, vertical = 4.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        elevation = CardDefaults.cardElevation(5.dp)
    ) {
        Column {
            Row(
                modifier = rowsModifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier.wrapContentWidth(Alignment.Start),
                    text = record.formatDate(),
                    style = MaterialTheme.typography.headlineSmall
                )

                Row {
                    IconButton(onClick = { onEdit() }) {
                        Icon(Icons.Rounded.Edit, stringResource(Res.string.home_edit_content_desc))
                    }
                    IconButton(onClick = { onShare(record.id) }) {
                        Icon(Icons.Rounded.Share, stringResource(Res.string.home_share_content_desc))
                    }
                    IconButton(onClick = { onDataRemove() }) {
                        Icon(Icons.Rounded.Delete, stringResource(Res.string.home_delete_content_desc))
                    }
                }
            }
            // Cold water
            Row(
                modifier = rowsModifier,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(Res.string.home_cold_water))
                Text(text = record.coldWater.toString())
                Text(text = stringResource(Res.string.home_hot_water))
                Text(text = record.hotWater.toString())
            }
            // Electricity
            Row(
                modifier = rowsModifier,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(Res.string.home_kwh_day))
                Text(text = record.dayElectricity.toString())
                Text(text = stringResource(Res.string.home_kwh_night))
                Text(text = record.nightElectricity.toString())
            }
        }
    }
}
