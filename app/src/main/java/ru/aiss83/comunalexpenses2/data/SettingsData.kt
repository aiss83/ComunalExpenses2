package ru.aiss83.comunalexpenses2.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SettingsData(
    var street: String,
    var house: Int,
    var flat: Int

) : Parcelable