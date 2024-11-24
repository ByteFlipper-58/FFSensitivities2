package com.byteflipper.ffsensitivities.data

import kotlinx.serialization.Serializable

@Serializable
data class Manufacturer(
    val name: String,
    val model: String,
    val showInProductionApp: Boolean,
    val isAvailable: Boolean
)

@Serializable
data class ManufacturerResponse(
    val manufacturers: List<Manufacturer>
)