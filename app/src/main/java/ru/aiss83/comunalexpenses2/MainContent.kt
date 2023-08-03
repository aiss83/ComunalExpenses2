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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import androidx.navigation.NavController
import ru.aiss83.comunalexpenses2.ui.theme.ComunalExpenses2Theme
import java.util.Calendar
import java.util.Locale

// Creating a composable function
// to display Top Bar and options menu
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(modifier: Modifier = Modifier) {

    // Create a boolean variable
    // to store the display menu state
    var mDisplayMenu by remember { mutableStateOf(false) }

    // fetching local context
    val mContext = LocalContext.current

    val resources = List<ResourcesRecord> (size = 3) {
        ResourcesRecord(Calendar.getInstance().time, 0, 0, 0,0);
        ResourcesRecord(Calendar.getInstance().time, 0, 0, 0,0);
        ResourcesRecord(Calendar.getInstance().time, 0, 0, 0,0)
    }

    val contentPadding = PaddingValues(16.dp, 8.dp)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Communal Expenses") },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Filled.List, null)
                    }
                })
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(onClick = {  }) {
                Text("+")
            }
        },
//        bottomBar = { BottomAppBar() { Text("BottomAppBar") } },
        contentWindowInsets = WindowInsets(bottom = 8.dp, top = 8.dp, left = 4.dp, right = 4.dp)
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding), contentPadding = contentPadding, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(resources) {record ->
                ResourcesCard(record = record)
            }
        }
    }
}

@Composable
fun ResourcesCard(record: ResourcesRecord, modifier: Modifier = Modifier) {

    ElevatedCard(modifier = modifier, elevation = CardDefaults.cardElevation(5.dp)) {
        Column(modifier = Modifier.padding(all = 4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = SimpleDateFormat("dd.MM.yyy", Locale("ru", "RU")).format(record.date),
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Filled.Share, "Share to...")
                }
            }
            // Cold water
            Row(modifier= Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Cold water")
                Text(text = record.coldWater.toString())
                Text(text = "Hot water")
                Text(text = record.hotWater.toString())
            }
            // Electricity
            Row(modifier= Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "kWh Day")
                Text(text = record.kWhDay.toString())
                Text(text = "kWh Night")
                Text(text = record.kWhNight.toString())
            }
        }
    }
}

@Preview
@Composable
fun ResourceCardPreview() {
    ComunalExpenses2Theme {
        ResourcesCard(record = ResourcesRecord(
            Calendar.getInstance().time,
            0, 0, 0, 0), modifier = Modifier.fillMaxWidth())
    }
}


@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
    ComunalExpenses2Theme {
        MainContent(modifier = Modifier)
    }
}
