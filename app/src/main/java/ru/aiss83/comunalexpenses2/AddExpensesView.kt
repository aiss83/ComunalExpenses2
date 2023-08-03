package ru.aiss83.comunalexpenses2

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ru.aiss83.comunalexpenses2.ui.theme.ComunalExpenses2Theme

@Composable
fun AddExpensesView(modifier: Modifier) {
    Text(text = "Expenses add here")
}

@Preview(showBackground = true)
@Composable
fun AddExpensesViewPreview() {
    ComunalExpenses2Theme {
        AddExpensesView(modifier = Modifier)
    }
}