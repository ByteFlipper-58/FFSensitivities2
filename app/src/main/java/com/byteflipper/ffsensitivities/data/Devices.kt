package com.byteflipper.ffsensitivities.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class ManufacturerWithModels(
    val manufacturer: String,
    val models: List<DeviceModel>
)

@Parcelize
data class DeviceModel(
    val manufacturer: String,
    val name: String,
    val settings_source_url: String,
    val dpi: Int,
    val fire_button: Int,
    val sensitivities: Sensitivities
) : Parcelable

@Parcelize
data class Sensitivities(
    val review: Int,
    val collimator: Int,
    val x2_scope: Int,
    val x4_scope: Int,
    val sniper_scope: Int,
    val free_review: Int
) : Parcelable