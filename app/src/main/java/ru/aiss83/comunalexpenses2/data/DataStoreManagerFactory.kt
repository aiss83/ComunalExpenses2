package ru.aiss83.comunalexpenses2.data

enum class SettingsDataSource {
    PREFERENCES_DATA_STORE
}
class DataStoreManagerFactory {
    fun create(source: SettingsDataSource): DataStoreManager {
        return DataStoreManagerImpl()
    }
}