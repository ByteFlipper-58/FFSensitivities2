package com.byteflipper.ffsensitivities.presentation.sensitivities.events

sealed class SensitivitiesScreenEvent {
    object NavigateBack : SensitivitiesScreenEvent()
    object CopySettings : SensitivitiesScreenEvent()
    object ThumbUp : SensitivitiesScreenEvent()
    object ThumbDown : SensitivitiesScreenEvent()
    object RetryLoadDevice : SensitivitiesScreenEvent()
} 