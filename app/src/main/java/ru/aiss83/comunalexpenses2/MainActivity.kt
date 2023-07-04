package ru.aiss83.comunalexpenses2

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.aiss83.comunalexpenses2.ui.theme.ComunalExpenses2Theme
import java.time.LocalDate
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComunalExpenses2Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent(modifier = Modifier)
                }
            }
        }
    }
}

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
            FloatingActionButton(onClick = {}) {
                Text("+")
            }
        },
        bottomBar = { BottomAppBar() { Text("BottomAppBar") } }
    ) { contentPadding ->
        LazyColumn(modifier = Modifier.padding(contentPadding)) {
//            Text("BodyContent")
        }
    }
}

@Composable
fun ResourcesCard(record: ResourcesRecord, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column() {
            Text(text = record.date.toString())
            // Cold water
            Row {
                Text(text = "Cold water")
                Text(text = record.coldWater.toString())
                Text(text = "Hot water")
                Text(text = record.hotWater.toString())
            }
            // Electricity
            Row {
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
        ResourcesCard(record = ResourcesRecord(Dat), )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ComunalExpenses2Theme {
        MainContent(modifier = Modifier)
    }
}