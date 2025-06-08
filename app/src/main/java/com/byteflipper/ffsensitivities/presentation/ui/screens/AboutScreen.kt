package com.byteflipper.ffsensitivities.presentation.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_app)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppInfoCard(
                appName = stringResource(R.string.app_name),
                developerName = "ByteFlipper",
                appIcon = painterResource(id = R.drawable.ic_launcher_foreground)
            )

            ExpandableSection(
                title = stringResource(R.string.support_feedback_category_title),
                icon = painterResource(id = R.drawable.bug_report_24px),
                expandedContentDescription = stringResource(R.string.expandable_section_expand),
                collapsedContentDescription = stringResource(R.string.expandable_section_collapse)
            ) {
                Column {
                    AnimatedActionItem(
                        title = stringResource(R.string.bug_report_title),
                        subtitle = stringResource(R.string.bug_report_subtitle),
                        icon = painterResource(id = R.drawable.bug_report_24px),
                        onClick = { navController.navigate(Screen.BugReport.route) }
                    )

                    AnimatedActionItem(
                        title = stringResource(R.string.rate_the_app_title),
                        subtitle = stringResource(R.string.rate_the_app_subtitle),
                        icon = painterResource(id = R.drawable.rate_review_24px),
                        onClick = { launchReviewFlow() }
                    )

                    AnimatedActionItem(
                        title = stringResource(R.string.other_apps_title),
                        subtitle = stringResource(R.string.other_apps_subtitle),
                        icon = painterResource(id = R.drawable.apps_24px),
                        onClick = {
                            ChromeCustomTabUtil.openUrl(
                                context = context,
                                url = context.getString(R.string.google_play_store),
                                primaryColor = primaryColorArgb
                            )
                        },
                        showDivider = false
                    )
                }
            }

            ExpandableSection(
                title = stringResource(R.string.connect_with_us_category_title),
                icon = painterResource(id = R.drawable.web_24px),
                expandedContentDescription = stringResource(R.string.expandable_section_expand),
                collapsedContentDescription = stringResource(R.string.expandable_section_collapse)
            ) {
                Column {
                    AnimatedActionItem(
                        title = stringResource(R.string.website_title),
                        subtitle = stringResource(R.string.website_subtitle),
                        icon = painterResource(id = R.drawable.web_24px),
                        onClick = {
                            ChromeCustomTabUtil.openUrl(
                                context = context,
                                url = "https://byteflipper.web.app",
                                primaryColor = primaryColorArgb
                            )
                        }
                    )

                    AnimatedActionItem(
                        title = stringResource(R.string.vk_title),
                        subtitle = stringResource(R.string.vk_subtitle),
                        icon = painterResource(id = R.drawable.vk_24),
                        onClick = {
                            ChromeCustomTabUtil.openUrl(
                                context = context,
                                url = "https://vk.com/byteflipper",
                                primaryColor = primaryColorArgb
                            )
                        }
                    )

                    AnimatedActionItem(
                        title = stringResource(R.string.telegram_title),
                        subtitle = stringResource(R.string.telegram_subtitle),
                        icon = painterResource(id = R.drawable.telegram_24),
                        onClick = {
                            ChromeCustomTabUtil.openUrl(
                                context = context,
                                url = "https://t.me/byteflipper",
                                primaryColor = primaryColorArgb
                            )
                        },
                        showDivider = false
                    )
                }
            }

            ExpandableSection(
                title = stringResource(R.string.development_category_title),
                icon = painterResource(id = R.drawable.code_24px),
                expandedContentDescription = stringResource(R.string.expandable_section_expand),
                collapsedContentDescription = stringResource(R.string.expandable_section_collapse)
            ) {
                Column {
                    AnimatedActionItem(
                        title = stringResource(R.string.github_title),
                        subtitle = stringResource(R.string.github_subtitle),
                        icon = painterResource(id = R.drawable.github_24),
                        onClick = {
                            ChromeCustomTabUtil.openUrl(
                                context = context,
                                url = "https://github.com/ByteFlipper-58",
                                primaryColor = primaryColorArgb
                            )
                        }
                    )

                    AnimatedActionItem(
                        title = stringResource(R.string.source_code_title),
                        subtitle = stringResource(R.string.source_code_subtitle),
                        icon = painterResource(id = R.drawable.code_24px),
                        onClick = {
                            ChromeCustomTabUtil.openUrl(
                                context = context,
                                url = "https://github.com/ByteFlipper-58/FFSensitivities2",
                                primaryColor = primaryColorArgb
                            )
                        },
                        showDivider = false
                    )
                }
            }

            VersionInfoCard(
                versionTitle = stringResource(R.string.version),
                versionInfo = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                icon = painterResource(id = R.drawable.update_24px)
            )

            InfoCard(
                text = stringResource(R.string.made_with_love)
            )
        }
    }
}
