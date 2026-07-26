package com.byteflipper.ffsensitivities.presentation.about

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AboutScreenLayout(
    navController: NavHostController,
    onOpenBugReport: () -> Unit
) {
    val context = LocalContext.current
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
            context = context,
            primaryColorArgb = primaryColorArgb,
            onOpenBugReport = onOpenBugReport,
            launchReviewFlow = { launchReviewFlow() }
        )
    }
} 
