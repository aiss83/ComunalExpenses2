package ru.aiss83.comunalexpenses2.screens

import android.app.Application
import android.icu.text.SimpleDateFormat
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import ru.aiss83.comunalexpenses2.DeleteConfirmationDialog
import ru.aiss83.comunalexpenses2.ResourcesDataViewModel
import ru.aiss83.comunalexpenses2.data.ResourceData
import ru.aiss83.comunalexpenses2.ui.theme.ComunalExpenses2Theme
import java.util.Date
import java.util.Locale
import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    allResourceData: List<ResourceData>,
    viewModel: ResourcesDataViewModel,
    onNavigateToAddExpenses: () -> Unit,
    onNavigateToSettings: () -> Unit) {

    var openRemoveDialog by rememberSaveable { mutableStateOf(false) }
    /* Store UUID to reove as string */
    var boundToRemove by rememberSaveable { mutableStateOf("") }

    val removeRecord = { id: UUID ->
        boundToRemove = id.toString()
        openRemoveDialog = true
    }

    val localContext = LocalContext.current

    val shareRecord = { id: UUID ->
        viewModel.shareResourceData(id)
    }

    when {
        openRemoveDialog -> {
            DeleteConfirmationDialog(
                onDismissRequest = {
                    openRemoveDialog = false
                    boundToRemove = ""
                }
            ) {
                openRemoveDialog = false
                if (boundToRemove.isNotEmpty()) {
                    val id = UUID.fromString(boundToRemove)
                    viewModel.deleteResourceData(id)
                    boundToRemove = ""
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Communal Expenses") },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings
                    ) {
                        Icon(Icons.Filled.List, null)
                    }
                })
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddExpenses
            ) {
                Text("+")
            }
        },

        contentWindowInsets = WindowInsets(bottom = 8.dp, top = 8.dp, left = 4.dp, right = 4.dp)
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(allResourceData) { item ->
                ResourcesCard(record = item, removeRecord, shareRecord)
            }
        }
    }
}

@Composable
fun ResourcesCard(
    record: ResourceData,
    onDataRemove: (id: UUID) -> Unit,
    onShare: (id: UUID) -> Unit) {

    val rowsModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp, vertical = 4.dp)

    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp), elevation = CardDefaults.cardElevation(5.dp)) {
        Column() {
            Row(modifier = rowsModifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier.wrapContentWidth(Alignment.Start),
                    text = SimpleDateFormat("dd.MM.yyy", Locale("ru", "RU")).format(record.date),
                    style = MaterialTheme.typography.headlineSmall
                )

                Row {
                    IconButton(
                        onClick = { onShare(record.id) }
                    ) {
                        Icon(Icons.Filled.Share, "Share to...")
                    }
                    IconButton(
                        onClick = {
                            onDataRemove(record.id)
                        }
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete record")
                    }
                }
            }
            // Cold water
            Row(modifier= rowsModifier,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Cold water")
                Text(text = record.coldWater.toString())
                Text(text = "Hot water")
                Text(text = record.hotWater.toString())
            }
            // Electricity
            Row(modifier= rowsModifier,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "kWh Day")
                Text(text = record.dayElectricity.toString())
                Text(text = "kWh Night")
                Text(text = record.nightElectricity.toString())
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ResourceCardPreview() {
    ComunalExpenses2Theme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Card (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(5.dp)
            ) {
                Column {
                    ConstraintLayout (modifier = Modifier.fillMaxWidth()) {

                        val (text, buttons) = createRefs()
                        Text(
                            modifier = Modifier.constrainAs(text) {
                                start.linkTo(parent.start, margin = 4.dp)
                            },
                            text = SimpleDateFormat("dd.MM.yyy", Locale("ru", "RU"))
                                .format(Date(System.currentTimeMillis()))
                        )
                        Box (modifier = Modifier.constrainAs(buttons) {
                            end.linkTo(parent.end, margin = 4.dp)
                        }) {
                            Row {
                                IconButton(onClick = { /*TODO: send to someone */ }) {
                                    Icon(Icons.Filled.Share, "Share to...")
                                }
                                IconButton(onClick = { }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete record")
                                }
                            }
                        }
                    }

                    Row (
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Test")
                        Text(text = "100")
                    }
                }

            }
        }

    }
}


@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
    ComunalExpenses2Theme {
    }
}
