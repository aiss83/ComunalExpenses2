package ru.aiss83.comunalexpenses2

import android.graphics.drawable.Icon
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import ru.aiss83.comunalexpenses2.ui.theme.ComunalExpenses2Theme

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

    // Creating a Top bar
    TopAppBar(
        title = { Text("Menu options", modifier= modifier) },
        modifier = modifier,
        actions = {

            // Creating Icon button favorites, on click
            // would create a Toast message
            IconButton(onClick = { Toast.makeText(mContext, "Favorite", Toast.LENGTH_SHORT).show() }) {
                Icon(Icons.Default.Favorite, "")
            }

            // Creating Icon button for dropdown menu
            IconButton(onClick = { mDisplayMenu = !mDisplayMenu }) {
                Icon(Icons.Default.MoreVert, "")
            }

            // Creating a dropdown menu
            DropdownMenu(
                expanded = mDisplayMenu,
                onDismissRequest = { mDisplayMenu = false }
            ) {

                // Creating dropdown menu item, on click
                // would create a Toast message
                DropdownMenuItem(text = { Text(text = "Settings") },
                    onClick = {
                        Toast.makeText(mContext, "Settings", Toast.LENGTH_SHORT).show()
                })

                // Creating dropdown menu item, on click
                // would create a Toast message
                DropdownMenuItem(text = {Text(text = "Logout")},
                    onClick = { Toast.makeText(mContext, "Logout", Toast.LENGTH_SHORT).show() })
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ComunalExpenses2Theme {
        MainContent(modifier = Modifier)
    }
}