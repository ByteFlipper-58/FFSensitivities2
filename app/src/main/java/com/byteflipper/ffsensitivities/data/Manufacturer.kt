package com.byteflipper.ffsensitivities.data

data class Manufacturer(
    val showInProductionApp: Boolean,
    val isAvailable: Boolean,
    val name: String,
    val model: String
)

data class ManufacturerResponse(
    val manufacturers: List<Manufacturer>
)