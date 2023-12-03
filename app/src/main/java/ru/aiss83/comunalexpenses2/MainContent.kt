package ru.aiss83.comunalexpenses2

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.aiss83.comunalexpenses2.data.ResourceData
import ru.aiss83.comunalexpenses2.ui.theme.ComunalExpenses2Theme
import java.util.Locale

// Creating a composable function
// to display Top Bar and options menu
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    allResourceData: List<ResourceData>,
    viewModel: ResourcesDataViewModel,
    onNavigateToAddExpenses: () -> Unit,
    onNavigateToSettings: () -> Unit) {

//    val contentPadding = PaddingValues(8.dp, 8.dp)

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
        LazyColumn(modifier = Modifier.padding(innerPadding), /*contentPadding = contentPadding,*/ verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(allResourceData) { item ->
                ResourcesCard(record = item)
            }
        }
    }
}

@Composable
fun ResourcesCard(record: ResourceData) {

    val rowsModifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp)

    Card(modifier = Modifier.fillMaxWidth().padding(8.dp), elevation = CardDefaults.cardElevation(5.dp)) {
        Column() {
            Row(modifier = rowsModifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = SimpleDateFormat("dd.MM.yyy", Locale("ru", "RU")).format(record.date),
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(onClick = { /*TODO: send to someone */ }) {
                    Icon(Icons.Filled.Share, "Share to...")
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

@Preview
@Composable
fun ResourceCardPreview() {
    ComunalExpenses2Theme {
//        ResourcesCard(
//            record = ResourcesRecord(
//            Calendar.getInstance().time,
//            0, 0, 0, 0), modifier = Modifier.fillMaxWidth())
    }
}


@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
    ComunalExpenses2Theme {
//        MainContent(modifier = Modifier, onNavigateToAddExpenses = {})
    }
}
