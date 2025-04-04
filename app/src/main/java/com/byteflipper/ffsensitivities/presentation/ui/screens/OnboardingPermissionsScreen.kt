package com.byteflipper.ffsensitivities.presentation.ui.screens

// Remove import for OnboardingBottomBar
// import com.byteflipper.ffsensitivities.presentation.ui.components.OnboardingBottomBar
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.byteflipper.ffsensitivities.R

@Composable
fun OnboardingPermissionsScreen(
    navController: NavController, // Keep NavController for permission request logic if needed
    paddingValues: PaddingValues, // Add padding parameter
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // Remove internal state management for hasPermission
    // var hasPermission by remember { mutableStateOf(hasNotificationPermission(context)) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            // No navigation logic here anymore
            // hasPermission = isGranted
        }
    )

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
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.onboarding_permissions_title), // TODO: Add string resource
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = stringResource(R.string.onboarding_permissions_description), // TODO: Add string resource
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            // Permissions not needed for older versions, proceed directly
                            navController.navigate("welcome")
                        }
                    },
                    // Button is always enabled, text might change based on actual permission status if needed,
                    // but for simplicity, keep it as "Grant Permission" or similar.
                    // The actual check happens before launching.
                    enabled = true
                ) {
                    // Simplified text, or check permission directly if needed for text change
                    Text(stringResource(R.string.grant_permission))
                }
            }
            // Remove OnboardingBottomBar from here
        }
    // } // End of removed Scaffold
}
