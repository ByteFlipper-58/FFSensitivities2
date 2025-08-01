package com.byteflipper.ffsensitivities.presentation.ui.screens.about

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.byteflipper.ffsensitivities.BuildConfig
import com.byteflipper.ffsensitivities.R
import com.byteflipper.ffsensitivities.navigation.Screen
import com.byteflipper.ffsensitivities.utils.ChromeCustomTabUtil
import com.byteflipper.ui_components.components.AnimatedActionItem
import com.byteflipper.ui_components.components.AppInfoCard
import com.byteflipper.ui_components.components.ExpandableSection
import com.byteflipper.ui_components.components.InfoCard
import com.byteflipper.ui_components.components.VersionInfoCard
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun AboutScreenLayout(
    navController: NavHostController
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val primaryColorArgb = MaterialTheme.colorScheme.primary.toArgb()
    val reviewManager = remember { ReviewManagerFactory.create(context) }

    fun launchReviewFlow() {
        val activity = context as? ComponentActivity ?: return
        coroutineScope.launch {
            try {
                val reviewInfo = reviewManager.requestReviewFlow().await()
                reviewManager.launchReviewFlow(activity, reviewInfo)
            } catch (e: Exception) {
                println("In-App Review failed, opening Play Store: ${e.message}")
                try {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("market://details?id=${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (activityNotFoundException: Exception) {
                    println("Play Store not found, opening web URL: ${activityNotFoundException.message}")
                    try {
                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
                        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(webIntent)
                    } catch (webException: Exception) {
                        println("Failed to open web URL: ${webException.message}")
                    }
                }
            }
        }
    }

    AboutScreenScaffold(
        navController = navController
    ) {
        AboutScreenContent(
            navController = navController,
            context = context,
            primaryColorArgb = primaryColorArgb,
            launchReviewFlow = { launchReviewFlow() }
        )
    }
} 