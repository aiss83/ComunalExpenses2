package ru.aiss83.comunalexpenses2.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.aiss83.comunalexpenses2.data.ResourceData
import ru.aiss83.comunalexpenses2.domain.ResourcesDataViewModel
import ru.aiss83.comunalexpenses2.ui.components.DeleteConfirmationDialog
import ru.aiss83.comunalexpenses2.utils.shareText
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    allResourceData: List<ResourceData>,
    viewModel: ResourcesDataViewModel,
    onNavigateToAddExpenses: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var openRemoveDialog by rememberSaveable { mutableStateOf(false) }
    var boundToRemove by rememberSaveable { mutableStateOf("") }

    val removeRecord = { id: Uuid ->
        boundToRemove = id.toString()
        openRemoveDialog = true
    }

    val shareRecord = { id: Uuid ->
        val record = allResourceData.find { it.id == id }
        if (record != null) {
            val shareText = buildString {
                appendLine("Communal Expenses - ${record.formatDate()}")
                appendLine()
                appendLine("Cold Water: ${record.coldWater}")
                appendLine("Hot Water: ${record.hotWater}")
                appendLine("Electricity (Day): ${record.dayElectricity} kWh")
                appendLine("Electricity (Night): ${record.nightElectricity} kWh")
            }
            shareText(shareText, "Communal Expenses")
        }
    }

    if (openRemoveDialog) {
        DeleteConfirmationDialog(
            onDismissRequest = {
                openRemoveDialog = false
                boundToRemove = ""
            }
        ) {
            openRemoveDialog = false
            if (boundToRemove.isNotEmpty()) {
                val id = Uuid.parse(boundToRemove)
                viewModel.deleteResourceData(id)
                boundToRemove = ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Communal Expenses") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.AutoMirrored.Rounded.List, "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddExpenses) {
                Icon(Icons.Rounded.Add, "Add reading")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allResourceData) { item ->
                ResourcesCard(record = item, removeRecord, shareRecord)
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
fun ResourcesCard(
    record: ResourceData,
    onDataRemove: (id: Uuid) -> Unit,
    onShare: (id: Uuid) -> Unit
) {
    val rowsModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp, vertical = 4.dp)

    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
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
                    IconButton(onClick = { onShare(record.id) }) {
                        Icon(Icons.Rounded.Share, "Share to...")
                    }
                    IconButton(onClick = { onDataRemove(record.id) }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete record")
                    }
                }
            }
            // Cold water
            Row(
                modifier = rowsModifier,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Cold water")
                Text(text = record.coldWater.toString())
                Text(text = "Hot water")
                Text(text = record.hotWater.toString())
            }
            // Electricity
            Row(
                modifier = rowsModifier,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "kWh Day")
                Text(text = record.dayElectricity.toString())
                Text(text = "kWh Night")
                Text(text = record.nightElectricity.toString())
            }
        }
    }
}
