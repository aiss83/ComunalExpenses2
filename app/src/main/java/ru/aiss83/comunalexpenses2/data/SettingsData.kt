package ru.aiss83.comunalexpenses2.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


enum class SettingsDataSource {
    PREFERENCES_DATA_STORE
}
@Parcelize
data class SettingsData(
    var street: String,
    var house: Int,
    var flat: Int

) : Parcelable