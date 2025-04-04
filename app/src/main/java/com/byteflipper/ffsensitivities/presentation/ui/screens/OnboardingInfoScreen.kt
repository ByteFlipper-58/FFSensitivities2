package com.byteflipper.ffsensitivities.presentation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.byteflipper.ffsensitivities.R

// Remove import for OnboardingBottomBar
// import com.byteflipper.ffsensitivities.presentation.ui.components.OnboardingBottomBar

@Composable
fun OnboardingInfoScreen(
    navController: NavController,
    paddingValues: PaddingValues, // Add padding parameter
    modifier: Modifier = Modifier
) {
    // Remove Scaffold wrapper
    // Scaffold { paddingValues ->
        Column(
            modifier = modifier // Use passed modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from parent Scaffold
                .padding(horizontal = 16.dp), // Keep horizontal padding
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
             Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center, // Center content vertically
                modifier = Modifier.weight(1f) // Takes available space
            ) {
                // TODO: Replace with actual image resource
                Image(
                    painter = painterResource(id = R.drawable.logo), // Placeholder image
                    contentDescription = stringResource(R.string.onboarding_image_desc), // TODO: Add string resource
                    modifier = Modifier
                        .size(150.dp)
                        .padding(bottom = 32.dp)
                )
                Text(
                    text = stringResource(R.string.onboarding_info_title), // TODO: Add string resource
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = stringResource(R.string.onboarding_info_description), // TODO: Add string resource
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
            )
            // Remove OnboardingBottomBar from here
        }
    // } // End of removed Scaffold
}
}
