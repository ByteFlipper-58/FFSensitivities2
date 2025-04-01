package com.byteflipper.ffsensitivities

// Добавляем импорты для Play Core
import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.byteflipper.ffsensitivities.data.local.DataStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStoreManager = DataStoreManager(application)

    // --- Существующие StateFlow ---
    val theme: StateFlow<String> = dataStoreManager.getTheme()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = "system"
        )

    val dynamicColor: StateFlow<Boolean> = dataStoreManager.getDynamicColor()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val contrastTheme: StateFlow<Boolean> = dataStoreManager.getContrastTheme()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val visitCount: StateFlow<Int> = dataStoreManager.getVisitCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = 0
        )

    val requestSent: StateFlow<Boolean> = dataStoreManager.getRequestSent()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val language: StateFlow<String> = dataStoreManager.getLanguage()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = "en"
        )

    val isReady: StateFlow<Boolean> = combine(
        theme,
        dynamicColor,
        contrastTheme
    ) { _, _, _ ->
        true
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )


    init {
        viewModelScope.launch(Dispatchers.Main) {
            val savedLanguage = language.value
            val currentLocales = AppCompatDelegate.getApplicationLocales()
            if (currentLocales.isEmpty || currentLocales.toLanguageTags() != savedLanguage) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(savedLanguage))
            }
        }
    }

    // --- Существующие методы ---
    fun setTheme(theme: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreManager.setTheme(theme)
        }
    }

    fun setDynamicColor(dynamicColor: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreManager.setDynamicColor(dynamicColor)
        }
    }

    fun setContrastTheme(contrast: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreManager.setContrastTheme(contrast)
        }
    }

    fun setVisitCount(count: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreManager.setVisitCount(count)
        }
    }

    fun setRequestSent(status: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreManager.setRequestSent(status)
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreManager.setLanguage(language)
            launch(Dispatchers.Main) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language))
            }
        }
    }

    // Фабрика остается без изменений
    class AppViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AppViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
