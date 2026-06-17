package ru.aiss83.comunalexpenses2.android

import android.app.Application
import ru.aiss83.comunalexpenses2.data.AppContextHolder

class ComunalExpensesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize the shared context holder for SQLDelight and Settings
        AppContextHolder.context = applicationContext
    }
}
