package ru.aiss83.comunalexpenses2.screens

import android.widget.EditText
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ru.aiss83.comunalexpenses2.ui.theme.ComunalExpenses2Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navHostController: NavHostController) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Settings") },
                navigationIcon = {
                    IconButton(onClick = { navHostController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back to home")
                    }
                },
                actions = {
                    IconButton(onClick = { /*TODO: implement save to storage table */ }) {
                        Icon(imageVector = Icons.Default.Done, contentDescription = "Save settings")
                    }
                })
        }
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(it),
            contentAlignment = Alignment.Center) {
            Text(text = "Settings screen", style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
fun SettingsContent() {
    Card(modifier = Modifier
        .padding(4.dp)
        .wrapContentSize(Alignment.TopStart)) {
        Column {
            Row (modifier = Modifier.fillMaxWidth(1.0f)) {
                TextField(value = "",
                    onValueChange = {},
                    label = {
                        Text(text = "Street")
                    }
                )
            }
            Row {
                TextField(value = "",
                    onValueChange = {},
                    label = {
                        Text(text = "Flat")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.5f)
                )
                TextField(value = "",
                    onValueChange = {},
                    label = {
                        Text(text = "House")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.5f)
                )
            }
        }
    }
}

@Preview
@Composable
fun SettingsContentPreview() {
    ComunalExpenses2Theme {
        Surface(modifier = Modifier.fillMaxSize()) {
            SettingsContent()
        }
    }
}