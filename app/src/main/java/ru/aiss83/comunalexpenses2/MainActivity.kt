package ru.aiss83.comunalexpenses2

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.aiss83.comunalexpenses2.data.DataStoreManagerFactory
import ru.aiss83.comunalexpenses2.data.SettingsDataSource
import ru.aiss83.comunalexpenses2.screens.MainScreen
import ru.aiss83.comunalexpenses2.ui.theme.ComunalExpenses2Theme

private const val USER_PREFERENCES_NAME = "user_preferences"

private val Context.dataSource by preferencesDataStore(USER_PREFERENCES_NAME)

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
                    val owner = LocalViewModelStoreOwner.current

                    owner?.let {
                        val prefs = DataStoreManagerFactory().create(SettingsDataSource.PREFERENCES_DATA_STORE, dataSource)
                        val viewModel: ResourcesDataViewModel = viewModel(
                            it,
                            "ResourcesDataViewModel",
                            ResourcesDataViewModelFactory(LocalContext.current.applicationContext
                                    as Application)
                        )

                        MainScreen(viewModel)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    ComunalExpenses2Theme {
//        MainNavigation()
    }
}
