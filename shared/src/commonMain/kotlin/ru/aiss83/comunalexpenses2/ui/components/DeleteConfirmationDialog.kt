package ru.aiss83.comunalexpenses2.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import comunalexpenses2.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import ru.aiss83.comunalexpenses2.ui.theme.ComunalExpenses2Theme

@Composable
fun DeleteConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit
) {
    AlertDialog(
        modifier = Modifier.wrapContentSize(Alignment.Center),
        icon = {
            Icon(imageVector = Icons.Rounded.Delete, contentDescription = stringResource(Res.string.delete_dialog_icon_desc))
        },
        title = {
            Text(text = stringResource(Res.string.delete_dialog_title))
        },
        text = {
            Text(text = stringResource(Res.string.delete_dialog_text))
        },
        onDismissRequest = { onDismissRequest() },
        confirmButton = {
            TextButton(onClick = { onConfirmation() }) {
                Text(text = stringResource(Res.string.delete_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismissRequest() }) {
                Text(text = stringResource(Res.string.delete_dialog_dismiss))
            }
        }
    )
}
