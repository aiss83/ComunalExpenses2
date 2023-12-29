package ru.aiss83.comunalexpenses2.screens

import android.widget.EditText
import androidx.compose.compiler.plugins.kotlin.ComposeCallableIds.remember
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import ru.aiss83.comunalexpenses2.SettingsViewModel
import ru.aiss83.comunalexpenses2.data.SettingsData
import ru.aiss83.comunalexpenses2.ui.theme.ComunalExpenses2Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, navHostController: NavHostController) {

    val settings by viewModel.settingsData.collectAsStateWithLifecycle(
        initialValue = SettingsData("", 0, 0)
    )

    var streetValue by remember { mutableStateOf(settings.street) }
    var houseValue by remember { mutableIntStateOf(settings.house) }
    var flatValue by remember { mutableIntStateOf(settings.flat) }

    fun textLeadZerosToInt(text: String) : Int {
        val value = text.trimStart('0')
        if (value.isNotEmpty() && value.isNotBlank()) {
            return value.toInt()
        }

        return 0
    }

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
                    IconButton(onClick = {
                        viewModel.saveSettings(streetValue, houseValue, flatValue)
                    }) {
                        Icon(imageVector = Icons.Default.Done, contentDescription = "Save settings")
                    }
                })
        }
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(it),
            contentAlignment = Alignment.TopCenter) {
            SettingsContent(
                streetValue = streetValue,
                streetValueChanged = { text ->
                    streetValue = text
                },
                houseValue = houseValue.toString(),
                houseValueChanged = {text ->
                    houseValue = textLeadZerosToInt(text)
                },
                flatValue = flatValue.toString(),
                flatValueChanged = { text ->
                    flatValue = textLeadZerosToInt(text)
                }
            )
        }
    }
}

@Composable
fun SettingsContent(
    streetValue: String,
    streetValueChanged: (String) -> Unit = {},
    houseValue: String,
    houseValueChanged: (String) -> Unit = {},
    flatValue: String,
    flatValueChanged: (String) -> Unit = {}
) {

    Card(modifier = Modifier
        .padding(4.dp)
        .wrapContentSize(Alignment.TopStart)) {
        Column {
            Row (modifier = Modifier.fillMaxWidth(1.0f)) {
                TextField(value = streetValue,
                    onValueChange = streetValueChanged,
                    label = {
                        Text(text = "Street")
                    }
                )
            }
            Row {
                TextField(value = houseValue,
                    onValueChange = houseValueChanged,
                    label = {
                        Text(text = "House")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.5f)
                )
                TextField(value = flatValue,
                    onValueChange = flatValueChanged,
                    label = {
                        Text(text = "Flat")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.5f)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingsContentPreview() {
    ComunalExpenses2Theme {
        Surface(modifier = Modifier.fillMaxSize()) {
            SettingsContent(streetValue = "", houseValue = "", flatValue = "")
        }
    }
}