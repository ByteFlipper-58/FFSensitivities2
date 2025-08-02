package com.byteflipper.ffsensitivities.presentation.about.events

sealed class AboutScreenEvent {
    object NavigateBack : AboutScreenEvent()
    object NavigateToBugReport : AboutScreenEvent()
    object LaunchReviewFlow : AboutScreenEvent()
    object OpenPlayStore : AboutScreenEvent()
    object OpenWebsite : AboutScreenEvent()
    object OpenVK : AboutScreenEvent()
    object OpenTelegram : AboutScreenEvent()
    object OpenGitHub : AboutScreenEvent()
    object OpenSourceCode : AboutScreenEvent()
} 