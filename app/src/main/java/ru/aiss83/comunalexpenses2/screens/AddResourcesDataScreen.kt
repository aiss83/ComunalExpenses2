package ru.aiss83.comunalexpenses2.screens

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ru.aiss83.comunalexpenses2.ResourcesDataViewModel
import ru.aiss83.comunalexpenses2.data.ResourceData
import ru.aiss83.comunalexpenses2.ui.theme.ComunalExpenses2Theme
import java.util.Calendar
import java.util.Locale

@Composable
fun AddResourcesDataScreen(
    viewModel: ResourcesDataViewModel,
    navController: NavHostController
    ) {

    var coldWaterValue by remember { mutableStateOf("0") }
    var hotWaterValue by remember { mutableStateOf("0") }
    var daykWhValue by remember { mutableStateOf("0") }
    var nightkWhValue by remember { mutableStateOf("0") }

    val resourcesRecord by rememberSaveable {
            mutableStateOf(ResourceData())
    }

    Column(modifier = Modifier
        .fillMaxHeight()
        .padding(all = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Date header shows us current date
            Text(
                text = SimpleDateFormat("dd.MM.yyy", Locale("ru", "RU")).format(Calendar.getInstance().time),
                style = MaterialTheme.typography.headlineSmall
            )
        }

        // Cold water
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            TextField(value = coldWaterValue,
                label = { Text(text = "Cold Water") },
                onValueChange = { newText -> coldWaterValue = newText.trimStart {it == '0'} },
                modifier = Modifier.weight(1.0f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.weight(0.1f))
            TextField(value = hotWaterValue,
                label = { Text(text = "Hot Water") },
                onValueChange = { newText -> hotWaterValue = newText.trimStart { it == '0' } },
                modifier = Modifier.weight(1.0f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        // Electricity
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            TextField(value = daykWhValue,
                label = { Text(text = "kWh Day") },
                onValueChange = { newText -> daykWhValue = newText.trimStart { it == '0' } },
                modifier = Modifier.weight(1.0f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.weight(0.1f))
            TextField(value = nightkWhValue,
                label = { Text(text = "kWh Night") },
                onValueChange = { newText -> nightkWhValue = newText.trimStart { it == '0' } },
                modifier = Modifier.weight(1.0f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(modifier = Modifier.weight(1.0f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            TextButton(modifier = Modifier.weight(1.0f), onClick = {
                // Save changes to model and go back
                resourcesRecord.coldWater = coldWaterValue.toLong()
                resourcesRecord.hotWater = hotWaterValue.toLong()
                resourcesRecord.dayElectricity = daykWhValue.toLong()
                resourcesRecord.nightElectricity = nightkWhValue.toLong()
                viewModel.addResourcesData(resourcesRecord)
                navController.popBackStack()
            }) {
                Text(text = "Save")
            }
            Spacer(modifier = Modifier.weight(0.1f))
            TextButton(modifier = Modifier.weight(1.0f), onClick = {
                navController.popBackStack()
            }) {
                Text(text = "Cancel")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddExpensesViewPreview() {
    ComunalExpenses2Theme {
//        AddExpensesView(modifier = Modifier.fillMaxSize())
    }
}