package com.byteflipper.ffsensitivities.presentation.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.byteflipper.ffsensitivities.manager.Language

@Composable
fun LanguageItem(
    language: Language,
    isSelected: Boolean,
    onClick: (Language) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(language) }
            .padding(horizontal = 16.dp, vertical = 12.dp), // Added vertical padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { onClick(language) } // RadioButton click also triggers the row click
        )
        Text(
            text = language.displayLanguage,
            modifier = Modifier.padding(start = 16.dp) // Add padding between radio and text
        )
    }
}
