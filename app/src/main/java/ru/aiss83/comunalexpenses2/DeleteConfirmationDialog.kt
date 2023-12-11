package ru.aiss83.comunalexpenses2

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ru.aiss83.comunalexpenses2.ui.theme.ComunalExpenses2Theme

@Composable
fun DeleteConfirmationDialog(onDismissRequest: () -> Unit, onConfirmation: () -> Unit) {
    AlertDialog(
        modifier = Modifier.wrapContentSize(Alignment.Center),
        icon = {
            Icon(imageVector = Icons.Default.Delete, contentDescription ="Delete record")
        },
        title = {
                Text(text = "Delete confirmation")
        },
        text = {
               Text(text = "Are you sure to delete record permanently?")
        },
        onDismissRequest = { onDismissRequest() },
        confirmButton = {
            TextButton(onClick = { onConfirmation() }) {
                Text(text = "Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismissRequest() }) {
                Text(text = "Dismiss")
            }
        }
    )
}

@Preview (showSystemUi = true, showBackground = true)
@Composable
fun DialogPreview() {
    ComunalExpenses2Theme {
        Surface(modifier = Modifier.fillMaxSize()) {
            DeleteConfirmationDialog(onDismissRequest = { }, onConfirmation = {})
        }
    }
}