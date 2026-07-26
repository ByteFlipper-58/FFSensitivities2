package com.byteflipper.ffsensitivities.presentation.settings

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.byteflipper.ffsensitivities.core.monetization.MonetizationCoordinator
import com.google.android.ump.FormError
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class PrivacyViewModel @Inject constructor(
    private val monetizationCoordinator: MonetizationCoordinator
) : ViewModel() {

    private val _privacyOptionsRequired = MutableStateFlow(monetizationCoordinator.isPrivacyOptionsRequired)
    val privacyOptionsRequired: StateFlow<Boolean> = _privacyOptionsRequired.asStateFlow()

    fun refresh() {
        _privacyOptionsRequired.value = monetizationCoordinator.isPrivacyOptionsRequired
    }

    fun showPrivacyOptionsForm(
        activity: Activity,
        onDismissed: (FormError?) -> Unit
    ) {
        monetizationCoordinator.showPrivacyOptionsForm(activity) { formError ->
            refresh()
            onDismissed(formError)
        }
    }
}
